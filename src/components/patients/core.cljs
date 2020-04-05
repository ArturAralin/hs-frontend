(ns components.patients.core
  (:require
  ;;  [cljs.pprint :refer [pprint]]
   [cljs.core.async :refer [take!]]
   [app.router :refer [router]]
   [components.patients.state :refer [state items-per-page]]
   [components.patients.rest :as rest]
   [components.patients.list :refer [list-all]]
   [components.patients.create :refer [create-patient]]))

(defn mount []
  (defmethod router :patients-list [] [list-all])
  (defmethod router :patients-create [] [create-patient])
  ;; load first page
  (take! (rest/load-patients! items-per-page 1)
         (fn [res]
           (reset! state {:loaded true
                          :page 1
                          :total (:total res)
                          :meta/order (:ids-order res)
                          :patients (:patients res)}))))
