(ns master.spam_helper)

(defn make-classifier [& categories]
  {:counts 0
   :categories (reduce
             (fn [accum k]
               (assoc accum k { :counts 0 :features {} }))
             {}
             categories)})

(defprotocol Classifier
  (get-state [this]  this))

(extend-type java.lang.Object
             Classifier3
             (get-state [this] this))


(defn- nbase-train [st feature category]
  (merge  (update-in (update-in st [:categories category :features]
                                (fn [m c] (assoc m feature (inc c)))
                                (get-in st [:categories category :features feature] 0))
                     [:categories category]
                     (fn [m c] (assoc m :counts (inc c)))
                     (get-in st [:categories category :counts]))
          {:counts (inc (get st :counts))}))

(defn ntrain! [st feature category]
  (if-not (some #(= category %1) (keys (get st :categories)))
    (throw (RuntimeException. (format "Class/Concept %s not found in classifer"
                                      category)))
    (nbase-train st feature category)))

;P(A1), P(A2)...
(defn np-of-category [st category]
  (let [total-count (:counts (get-state st))]
    (if (zero? total-count)
      0
      (double (/ (get-in (get-state st) [:categories category :counts])
                total-count)))))

;P(B1|A1), P(B2|A1)...
(defn np-of-feature-given-category [st feature class]
  (let [feature-count        (get-in (get-state st) [:categories class :features feature] 0)
        class-count (get-in (get-state st) [:categories class :counts]) ]
    (if (zero? class-count)
      0
      (double (/ feature-count class-count)))))


;P(A1 && B1 ), P(A1 && B2), ...
(defn np-of-category-and-feature [st feature category]
  (* (np-of-category st category)
     (np-of-feature-given-category st feature category)))


;P(B)
(defn ntotal-p-of-feature [st feature]
     (reduce (fn [accum category]
               (+ accum
                  (np-of-category-and-feature st feature category)))
             0
             (keys (:categories (get-state st)))))

;P(A|B) = [ P(B|A) * P(A) ]/P(B)
(defn np-of-category-given-feature [st feature]
  (loop [res {}
         [category & categories] (keys (:categories (get-state st)))]
    (if-not category
      res
      (let [numer (np-of-category-and-feature st feature category)
            denom (ntotal-p-of-feature st feature)]
        (recur (merge res {category
                           (if (zero? denom)
                             0.0
                             (/ numer denom))})
                  categories)))))
