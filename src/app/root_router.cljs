(ns app.root-router
  (:require-macros [secretary.core :refer [defroute]])
  (:require
   [secretary.core :as secretary]
   [app.router :refer [state hook-browser-navigation!]]))

(defn mount-router []
  (secretary/set-config! :prefix "#")

  (defroute "/" []
    (set! (.-hash js/window.location) "/patients/list")
    (swap! state assoc :page :patients-list))

  (defroute "/patients/list" []
    (swap! state assoc :page :patients-list))

  (defroute "/patients/create" []
    (swap! state assoc :page :patients-create))

  (defroute "*" []
    (set! (.-hash js/window.location) "/patients/list"))

  (hook-browser-navigation!))
