(ns proclodo-reagent-spike.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [reagent-forms.core :as forms]
              [goog.events :as events]
              [goog.history.EventType :as EventType])
    (:import goog.History))

(def state (atom {:event {:name "event"} :saved? false}))
(def server-state (atom {}))
(def click-count (atom 0))
;;--------------------------
;; Forms
(defn row [label input]
  [:div.row
   [:div.col-md-2 [:label label]]
   [:div.col-md-5 input]])

(defn input [label type id]
  (row label [:input.form-control {:field type :id id}]))

(defn set-value! [id value]
  (swap! state assoc :saved? false)
  (swap! state assoc-in [:event id] value))

(defn get-value [id]
  (get-in @state [:event id]))

(defn text-input [id label]
  [row label
   [:input
     {:type "text"
       :class "form-control"
       :value (get-value id)
       :on-change #(set-value! id (-> % .-target .-value))}]])

(defn new-event-form []
  [:div
   (text-input :name "Event name")
   [:button.btn.btn-default
    {:on-click
     #(swap! server-state assoc-in [:event :name] (get-in @state [:event :name]))}
    "Create"]
   [:div
    [:label (get-in @server-state [:event :name])]]])

(defn counting-component []
  [:div
   "The atom " [:code "click-count"] " has value: "
   @click-count ", "
   [:input {:type "button" :value "Click me!"
            :on-click #(swap! click-count inc)}]])
  ;; -------------------------
  ;; Views

(defn home-page []
  [:div [:h2 "Welcome to proclodo-reagent-spike"]
   (counting-component)
   [:div [:a {:href "#/new-event"} "create event"]]])

(defn new-event []
  [:div [:h2 "About proclodo-reagent-spike"]
   (new-event-form)
   [:div [:a {:href "#/"} "go to the home page"]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/new-event" []
  (session/put! :current-page #'new-event))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (mount-root))
