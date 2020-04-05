(ns components.patients.list
  (:require
   [components.patients.state :refer [state items-per-page]]
   [components.patients.rest :as rest]
   [cljs.pprint :refer [pprint]]
   [cljs.core.async :refer [<! go take!]]))

(defn- edit-patient [tag tmp-val-field patient-id model-field]
  (swap! state assoc-in [:patients patient-id :meta/editable-el] tag)
  (swap! state assoc-in [:patients patient-id tmp-val-field]
         (get-in @state [:patients patient-id model-field])))

(defn- cancel-editing [patient-id]
  (swap! state assoc-in [:patients patient-id :meta/editable-el] nil))

(defn- save-changes [tmp-val-field model-field patient-id]
  (let [_state @state
        new-val (get-in _state [:patients patient-id tmp-val-field])]
    (take!
     (rest/update-patient! patient-id model-field new-val)
     (fn [res]
       (let [bad-request? (= (:status res) "bad-request")
             curr-val (get-in _state [:patients patient-id model-field])]
         (when bad-request? (js/alert "bad value"))
         (swap! state update-in [:patients patient-id]
                (fn [v] (merge v {:meta/editable-el nil
                                  tmp-val-field nil
                                  model-field (if bad-request? curr-val new-val)}))))))))

(defn- patient-field-change [tmp-val-field patient-id val]
  (swap! state assoc-in [:patients patient-id tmp-val-field] val))

(defn- update-list [ch]
  (take! ch (fn [{total :total
                  patients :patients
                  order :ids-order}]
              (reset! state (merge @state {:total total
                                           :patients patients
                                           :meta/order order})))))

(defn- delete-patient [patient-id]
  (->
   (go
     (<! (rest/delete-patient! patient-id))
     (<! (rest/load-patients! items-per-page (:page @state))))
   update-list))

(defn- editable [patient-id]
  (fn [model-field el]
    (let [[tag & other] el
          state @state
          tmp-val-field (keyword "tmp" (str model-field))
          editable? (= tag (get-in state [:patients patient-id :meta/editable-el]))]
      (if editable?
        [tag
         [:input {:type "text"
                  :value (get-in state [:patients patient-id tmp-val-field])
                  :on-change #(patient-field-change tmp-val-field patient-id (-> % .-target .-value))}]
         [:div.edit_controls
          [:button {:on-click #(cancel-editing patient-id)} "cancel"]
          [:button {:on-click #(save-changes tmp-val-field model-field patient-id)} "save"]]]
        ;; rewrite to to assoc-in
        (into [] (concat [tag {:on-double-click #(edit-patient tag tmp-val-field patient-id model-field)}] other))))))

(defn- pagination-set-page [page]
  (-> (rest/load-patients! 10 page)
      update-list)
  (swap! state assoc :page page))

(defn- pagination [items-per-page]
  (let [state @state
        pages (Math/ceil (/ (:total state) items-per-page))
        links (map
               (fn [idx]
                 (let [page (inc idx)
                       current-page? (= (:page state) page)]
                   (identity
                    ^{:key idx}
                    [:a
                     {:on-click #(pagination-set-page page)
                      :class [(when current-page? "current")]}
                     (str page)])))
               (range pages))]
    [:div.pagination "Pagination " (doall links)]))

(defn- patient-item [patient]
  (let [editable (editable (:id patient))]
    ^{:key (:id patient)}
    [:div.patient
     [:div.column.id (:id patient)]
     (editable :fio [:div.column.fio (:fio patient)])
     (editable :birthday [:div.column.birthday (:birthday patient)])
     (editable :gender [:div.column.gender (:gender patient)])
     [:div.column.controls
      [:button {:on-click #(delete-patient (:id patient))} "remove user"]]]))

(defn list-all []
  [:div.container
   [:div.header
    [:div.column.id "ID"]
    [:div.column.fio "Patient name"]
    [:div.column.gender "Gender"]
    [:div.column.birthday "Birthday"]
    [:div.column.controls "Actions"]]
   [:div.patients
    (if (true? (:loaded @state))
      (let [state @state
            patients (:patients state)]
        (doall (map #(patient-item (get patients %)) (:meta/order state))))
      [:div "loading patients"])]
   (pagination items-per-page)])
