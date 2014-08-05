(ns reca.core-test
  (:use expectations
        reca.core))

(def model (file-model "data/ratings.csv"))
(def recommender (user-recommender model))

;; Check sim algos
(expect (log-likelihood model))
(expect (tanimoto model))
(expect (euclidean model))
(expect (pearson model))

;; RESCORING
(def rescorer (build-rescorer (fn [id original-score]
                                (if (= id 104)
                                  (* 1.2 original-score)
                                  original-score))))

(expect [{:item 104, :value 6.0} {:item 106, :value 4.0}]
        (vec (recommend recommender 1 5 rescorer)))

;; filtering
(def filtering (build-rescorer (fn [_ original-score] original-score)
                               (fn [id] (= id 104))))

(expect [{:item 106, :value 4.0}]
        (vec (recommend recommender 1 5 filtering)))



