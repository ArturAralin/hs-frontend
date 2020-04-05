(ns components.patients.create
  (:require
   [cljs.pprint :refer [pprint]]
   [reagent.core :as r]
   [cljs.core.async :refer [take!]]
   [components.patients.rest :as rest]))

(def empty-fields {:fio nil
                   :birthday nil
                   :gender nil
                   :oms_policy nil
                   :address nil})

(def form-state (r/atom empty-fields))

(defn- update-from-state! [field val]
  (swap! form-state assoc field val))

(defn- create-patient! []
  (take!
   (rest/create-patient! @form-state)
   (fn [res]
     (let [bad-request? (= (:status res) "bad-request")]
       (if bad-request?
         (js/alert "bad request")
         (reset! form-state empty-fields))))))

(defn- form-field [label type field & {:keys [input-mods]
                                       :or {input-mods {}}}]
  (let [form @form-state]
    [:div.form_field
     [:label label]
     [:input
      (merge {:type type
              :on-change #(update-from-state! field (-> % .-target .-value))
              :value (field form)}
             input-mods)]]))

(defn create-patient []
  [:div.form
   (form-field "FIO" "text" :fio)
   (form-field "Birthday" "date" :birthday)
   (form-field "Gender" "text" :gender)
   (form-field "Policy" "text" :oms_policy :input-mods {:min-length "16"})
   (form-field "Address" "text" :address)
   [:div.controls
    [:button {:on-click #(create-patient!)} "save"]]])
