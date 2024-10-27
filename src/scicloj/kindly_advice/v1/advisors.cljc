(ns scicloj.kindly-advice.v1.advisors)

(defn advice->recommended-kind [advice]
  (-> advice
      first
      first))

(defn update-context [context advisor]
  (if-let [advice (advisor context)]
    (-> context
        (update :kind
                (fn [kind]
                  (or kind
                      (advice->recommended-kind advice))))
        (update :advice
                concat advice))
    context))

(defn meta-kind-advisor [{:keys [meta-kind]
                          :as context}]
  (when meta-kind
    [[meta-kind {:reason :metadata}]]))

(defn predicate-based-advisor [{:keys [predicate-kinds]}]
  (fn [{:keys [value]
        :as context}]
    (->> predicate-kinds
         (map (fn [[predicate kind]]
                (when (predicate value)
                  [kind {:reason :predicate}])))
         (remove nil?)
         seq)))

(def default-predicate-kinds-v1
  [[(fn [v]
      (-> v
          type
          pr-str
          (= "smile.regression.LinearModel")))
    :kind/smile-model]
   [(fn [v]
      (-> v
          type
          pr-str
          (= "tech.v3.dataset.impl.dataset.Dataset")))
    :kind/dataset]
   [(fn [v]
      (-> v
          type
          pr-str
          (= "java.awt.image.BufferedImage")))
    :kind/image]
   [(fn [v] (let [m (meta v)]
              (and (:portal.viewer/reagent? m)
                   (-> m :portal.viewer/default keyword?)
                   (-> m :portal.viewer/default namespace (= "emmy.portal")))))
    :kind/emmy-viewers]
   [(fn [v]
      (some-> v
              meta
              :test
              fn?))
    :kind/test]
   [var? :kind/var]
   [map? :kind/map]
   [set? :kind/set]
   [vector? :kind/vector]
   [sequential? :kind/seq]])

(def default-advisors
  [meta-kind-advisor
   (predicate-based-advisor
    {:predicate-kinds default-predicate-kinds-v1})])
