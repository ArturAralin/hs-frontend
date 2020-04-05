(ns components.patients.state
  (:require
   [reagent.core :as r]))

(def state (r/atom {:loaded false
                    :page 1
                    :total 0
                    :meta/order []
                    :parients {}}))

(def items-per-page 10)
