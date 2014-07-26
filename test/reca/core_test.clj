(ns reca.core-test
  (:use expectations
        reca.core))

(def model (file-model "data/ratings.csv"))

(expect (log-likelihood model))
(expect (tanimoto model))
(expect (euclidean model))
(expect (pearson model))
