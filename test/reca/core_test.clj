(ns reca.core-test
  (:use expectations
        reca.core))

(def model (file-model "data/ratings.csv"))
(def recommender (user-based-recommender model))

;; Check sim algos
(expect (log-likelihood model))
(expect (tanimoto model))
(expect (euclidean model))
(expect (pearson model))

;; user based recommender
(expect (user-based-recommender model))

;; item based recommender
(expect (item-based-recommender model))
(expect (item-based-recommender model log-likelihood))


;; RESCORING
(def rescorer (build-rescorer {:rescore (fn [id original-score]
                                          (if (= id 104)
                                            (* 1.2 original-score)
                                            original-score))}))

(expect [{:item 104, :value 6.0} {:item 106, :value 4.0}]
        (vec (recommend recommender 1 5 rescorer)))

;; filtering
(let [filtering (build-rescorer {:rescore (fn [_ original-score] original-score)
                                 :ignore  (fn [id] (= id 104))})]
  
  (expect [{:item 106, :value 4.0}]
          (vec (recommend recommender 1 5 filtering))))

;; cluster based recommender




