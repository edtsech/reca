(ns reca.core
  (:import [java.io File]
           [java.util List]
           [org.apache.mahout.cf.taste.recommender RecommendedItem]
           [org.apache.mahout.cf.taste.impl.model.file FileDataModel]
           [org.apache.mahout.cf.taste.impl.similarity EuclideanDistanceSimilarity PearsonCorrelationSimilarity LogLikelihoodSimilarity TanimotoCoefficientSimilarity]
           [org.apache.mahout.cf.taste.impl.neighborhood NearestNUserNeighborhood ThresholdUserNeighborhood]
           [org.apache.mahout.cf.taste.recommender Recommender ItemBasedRecommender UserBasedRecommender IDRescorer]
           [org.apache.mahout.cf.taste.impl.recommender CachingRecommender]
           [org.apache.mahout.cf.taste.impl.recommender GenericUserBasedRecommender GenericItemBasedRecommender]
           [org.apache.mahout.cf.taste.eval RecommenderEvaluator RecommenderBuilder]
           [org.apache.mahout.cf.taste.impl.eval AverageAbsoluteDifferenceRecommenderEvaluator RMSRecommenderEvaluator]
           [org.apache.mahout.cf.taste.impl.model.jdbc ReloadFromJDBCDataModel PostgreSQLJDBCDataModel]))

(defprotocol ToClojure
  (->clj [x]))

(extend-protocol ToClojure
  List (->clj [xs] (map ->clj xs))
  RecommendedItem (->clj [^RecommendedItem x] {:item (.getItemID x)
                                               :value (.getValue x)}))

(defn make-pg-connection
  [options]
  (doto (org.postgresql.ds.PGPoolingDataSource.)
    (.setDataSourceName (:db-name options))
    (.setServerName (:host options))
    (.setPortNumber (:port options))
    (.setDatabaseName (:db-name options))
    (.setUser (:user options))
    (.setPassword (:pass options))
    (.setMaxConnections 100)))

(defn pg-db-model
  [connection options]
  (ReloadFromJDBCDataModel.
      (PostgreSQLJDBCDataModel. connection
                                (:table options)
                                (:user-id options)
                                (:item-id options)
                                (:preference options)
                                (:timestamp options))))

(defn file-model
  [path]
  (FileDataModel. (File. path)))

;; Algorithms
(defn log-likelihood
  [model]
  (LogLikelihoodSimilarity. model))

(defn tanimoto
  [model]
  (TanimotoCoefficientSimilarity. model))

(defn pearson
  [model]
  (PearsonCorrelationSimilarity. model))

(defn euclidean
  [model]
  (EuclideanDistanceSimilarity. model))

;; API

(defn- neighborhood
  ([sim-fn model]
   (neighborhood sim-fn model 5))
  ([sim-fn model n]
   (NearestNUserNeighborhood. n (sim-fn model) model)))

(defn user-based-recommender
  ([model]
    (user-based-recommender model pearson))
  ([model sim-fn]
     (CachingRecommender. (GenericUserBasedRecommender. model
                                                        (neighborhood sim-fn model 10)
                                                        (sim-fn model))))
  ([model sim-fn n]
     (CachingRecommender. (GenericUserBasedRecommender. model
                                                        (neighborhood sim-fn model n)
                                                        (sim-fn model)))))

(defn item-based-recommender
  ([model]
     (item-based-recommender model log-likelihood))
  ([model sim-fn]
     (CachingRecommender. (GenericItemBasedRecommender. model (sim-fn model)))))

(defn similar-items
  ([^ItemBasedRecommender r item-id]
     (similar-items r item-id 10))
  ([^ItemBasedRecommender r item-id n]
     (->clj (.mostSimilarItems r item-id n))))

(defn similar-users
  ([^Recommender r user-id]
    (similar-users r user-id 10))
  ([^Recommender r user-id n]
     (->clj (.mostSimilarUserIDs r user-id n))))

(defn recommend
  "Using recommender r, generates a sequence of n recommended item id's and their value."
  ([^Recommender r user-id]
     (recommend r user-id 10))
  ([^Recommender r user-id n]
     (->clj (.recommend r user-id n)))
  ([^Recommender r user-id n rescorer]
     (->clj (.recommend r user-id n rescorer))))

(defn build-rescorer
  ([rescore-fn]
   (reify IDRescorer
     (rescore [_ id original-score]
       (rescore-fn id original-score))
     (isFiltered [_ id]
       false)))
  ([rescore-fn filter-fn]
   (reify IDRescorer
     (rescore [_ id original-score]
       (rescore-fn id original-score))
     (isFiltered [_ id]
       (filter-fn id)))))

(defn estimate-user-preference
  "Using recommender r, estimates user u's preference for item i."
  [^Recommender r u i]
  (.estimatePreference r u i))

(def avg-diff-evaluator
  (AverageAbsoluteDifferenceRecommenderEvaluator. ))

(def rms-evaluator (RMSRecommenderEvaluator. ))

(defn evaluate
  ([r-fn model]
     (evaluate r-fn model avg-diff-evaluator))
  ([r-fn model e]
     (let [b (proxy [RecommenderBuilder] []
               (buildRecommender [data-model]
                                 (r-fn data-model)))]
       (.evaluate e b nil model 0.7 0.02))))
