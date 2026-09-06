(ns houshi.app
  "houshi appview — reagent + re-frame, view built from jp-go-dds
  (デジタル庁デザインシステム) hiccup.

  This is a faithful port of the previous SvelteKit scaffold
  (`svelte/src/routes/+page.svelte`, retired during the cljs migration): a
  static info panel describing this Cloudflare surface — title, project,
  declared route count, XRPC flag, declared public routes, declared runtime
  binding vars, and its own source path. It does not add functionality the
  Svelte scaffold did not have: `routes` and `vars` were empty lists there
  and remain empty lists here. The live XRPC dispatch logic this app
  fronts lives in `src/app.ts` / `xrpc-adapter/` / `kotoba/` — see
  `test/houshi/contract_test.cljs` at the repo root for the cross-surface
  contract that ties those together. This view does not touch that
  contract; it only displays a copy of the same declared shape."
  (:require [reagent.dom :as rdom]
            [re-frame.core :as rf]
            [jp-go-dds.core :as dds]))

;; -- db ----------------------------------------------------------------
;;
;; The Svelte scaffold held this as a single inline `const app = {...}`
;; object with no reactivity at all. It is kept as re-frame db + subs here
;; so the migration exercises the event/sub plumbing the workspace standard
;; calls for, without inventing state the original page did not have.

(def default-db
  {:title "Ai etzhayyim Project Houshi"
   :project "etzhayyim-project-houshi"
   :name "etzhayyim-project-houshi"
   :kind "cloudflare surface"
   :route-count 0
   :routes []
   :vars []
   :xrpc? true
   :relative-path "60-apps/etzhayyim-project-houshi/cljs/src/houshi/app.cljs"})

(rf/reg-event-db
 :initialize-db
 (fn [_ _] default-db))

(rf/reg-sub :title (fn [db _] (:title db)))
(rf/reg-sub :project (fn [db _] (:project db)))
(rf/reg-sub :app-name (fn [db _] (:name db)))
(rf/reg-sub :kind (fn [db _] (:kind db)))
(rf/reg-sub :route-count (fn [db _] (:route-count db)))
(rf/reg-sub :routes (fn [db _] (:routes db)))
(rf/reg-sub :vars (fn [db _] (:vars db)))
(rf/reg-sub :xrpc? (fn [db _] (:xrpc? db)))
(rf/reg-sub :relative-path (fn [db _] (:relative-path db)))

;; -- view ----------------------------------------------------------------

(defn- routes-panel [routes]
  (dds/card
   (dds/heading 2 "Public Routes" {:size "24"})
   (if (seq routes)
     (dds/table {:headers ["route"] :rows (mapv vector routes)})
     [:p {:class "dds-ext-lead"} "No public route is declared next to this app surface."])))

(defn- vars-panel [vars]
  (dds/card
   (dds/heading 2 "Runtime Bindings" {:size "24"})
   (if (seq vars)
     (apply dds/row (map #(dds/chip-label %) vars))
     [:p {:class "dds-ext-lead"} "No public vars are declared in the nearest wrangler config."])))

(defn app-view []
  (let [title @(rf/subscribe [:title])
        project @(rf/subscribe [:project])
        app-name @(rf/subscribe [:app-name])
        kind @(rf/subscribe [:kind])
        route-count @(rf/subscribe [:route-count])
        routes @(rf/subscribe [:routes])
        vars @(rf/subscribe [:vars])
        xrpc? @(rf/subscribe [:xrpc?])
        relative-path @(rf/subscribe [:relative-path])]
    (dds/container
     (dds/section {}
       [:p {:class "dds-ext-lead"} (str "Cloudflare " kind)]
       (dds/heading 1 title)
       [:p {:class "dds-ext-lead"} app-name])
     (dds/section {}
       (dds/grid {}
         (dds/card (dds/heading 3 "Project" {:size "18"}) [:p {} project])
         (dds/card (dds/heading 3 "Routes" {:size "18"}) [:p {} (str route-count)])
         (dds/card (dds/heading 3 "XRPC" {:size "18"}) [:p {} (if xrpc? "enabled" "not configured")])))
     (dds/divider)
     (routes-panel routes)
     (vars-panel vars)
     (dds/card
      (dds/heading 2 "Source" {:size "24"})
      [:p {} relative-path]))))

;; -- init --------------------------------------------------------------------

(defn ^:export main []
  (rf/dispatch-sync [:initialize-db])
  (rdom/render [app-view] (js/document.getElementById "app")))
