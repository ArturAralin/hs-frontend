(ns components.patients.rest
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [cljs.core.async :refer [<!]]
   [cljs-http.client :as http]
   [cljs.pprint :refer [pprint]]))

(defn load-patients! [items-per-page page]
  (go
    (let [limit items-per-page
          offset (* items-per-page (- page 1))
          res (<! (http/get "http://localhost:8080/api/patients"
                            {:query-params {"limit" limit "offset" offset}
                             :with-credentials? false}))]
      {:total (get-in res [:body :total])
       :ids-order (map #(:id %) (get-in res [:body :response]))
       :patients (->> (get-in res [:body :response])
                      (map #(hash-map (:id %) %))
                      (apply merge))})))

(defn update-patient! [patient-id field val]
  (go
    (let [payload {:update_patients [{:id patient-id
                                      field val}]}
          res (<! (http/put "http://localhost:8080/api/patients"
                            {:json-params payload
                             :with-credentials? false}))]
      (:body res))))

(defn delete-patient! [patient-id]
  (http/delete "http://localhost:8080/api/patients"
                            {:json-params {:ids [patient-id]}
                             :with-credentials? false}))

(defn create-patient! [data]
  (go
    (:body (<! (http/post "http://localhost:8080/api/patients"
                          {:json-params {:patients [data]}
                           :with-credentials? false})))))
