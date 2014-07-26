# Reca

Reca is a wrapper for Mahout single-machine recommendation algorithms.

## Usage

### Create data model

``` clj

;; file data model
(file-model "./data/ratings.csv")

;; PostgreSQL data model
(make-pg-connection {:db-name "db"
                     :host "localhost"
                     :port 5432
                     :user "user"
                     :pass "pass"})

(reca/pg-db-model conn {:table "ratings"
                        :user-id "user_id"
                        :item-id "item_id"
                        :preference "rating"
                        :timestamp "created_at"})

;; MySQL
;; no there yet

;; MongoDB
;; no there yet
```

### Create recommender

``` clj
;; user based recommender
(user-recommender model)
;; specify similiarity algorithm (pearson correlation by default)
(user-recommender model euclidean)

;; item based recommender
(item-recommender model)
;; specify similiarity algorithm (log-likelihood by default)
(item-recommender model tanimoto)
```

### Get recommendations

``` clj
(recommend recommender user-id)

;; specify number of recommendation you want to receive (10 by default)
(recommend recommender user-id 20)
```

### Find similiar items

``` clj
(similar-items recommender item-id)

;; specify number of items you want to get (10 by default)
(similar-items recommender item-id 20)
```

### Find similar users

``` clj
(similar-users recommender user-id)

;; specify number of user ids you want to get (10 by default)
(similar-users recommender user-id 20)
```

### Rescoring

Using rescorer you could add your application logic to recommender engine.
You could increase scores for particular items and decrease for others based on your application logic.

``` clj
(build-rescorer (fn [id original-score]
				   (if ...
				     (* original-score 1.2)
				     original-score)))

;; usage
(recommend recommender user-id 20 rescorer)
```

### Evaluation

``` clj
(evaluate #(user-recommender % pearson 10) model)
```

## Similarity algorithms

* Euclidean distance
* Pearson correlation
* Tanimoto coefficient
* Log-likelihood


## To Do

* Add drivers for MySQL, Mongo
* Add wrapper for update mechanism
* Add experimental algorithms
* Improve evaluation

Inspired by https://github.com/pingles/mahout-sample

## Links

* [Mahout in Action](http://www.manning.com/owen/)
* [Collective Intelligence](http://shop.oreilly.com/product/9780596529321.do)
* [Mahout](https://mahout.apache.org/)

## License

MIT License
