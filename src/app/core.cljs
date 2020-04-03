(ns ^:figwheel-hooks app.core
  (:require
   [app.router :refer [router]]
   [app.root-router :refer [mount-router]]
   [reagent.dom :as rdom]
   [components.patients.core]))

(defn mount []
  (mount-router)
  (components.patients.core/mount)
  (rdom/render [router] (js/document.getElementById "root")))

(defonce main (mount))
