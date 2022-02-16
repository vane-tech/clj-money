(ns clj-money.main
  (:refer-clojure :exclude [divide zero?])
  (:require
   [clj-money.currencies :as currencies]
   [clojure.string :as str]
   [goog.string :as gstring]
   [goog.string.format]))

(def zero {:cents 0})

(defn- same-currency [moneys]
  (let [all-same? (apply = (->> moneys
                                (filter identity)
                                (filter #(not= 0 (:cents %)))
                                (map :currency)))]
    (when-not all-same?
      (.error js/console (str "Cannot compare amounts in different currencies unless the amount is zero: "
                              (pr-str moneys))))
    all-same?))

(defn- first-non-zero-currency [moneys]
  (or (->> moneys
           (filter #(not= 0 (:cents %)))
           first
           :currency)
      (-> moneys first :currency)
      currencies/default-currency))

(defn plus [& moneys]
  {:pre [(same-currency moneys)]}
  {:currency (first-non-zero-currency moneys)
   :cents (->> moneys (map :cents) (reduce +))})

(defn minus [& moneys]
  {:pre [(same-currency moneys)]}
  {:cents (if (empty? moneys)
            0
            (->> moneys (map :cents) ((partial apply -))))
   :currency (first-non-zero-currency moneys)})

(defn multiply [money & multipliers]
  {:currency (:currency money)
   :cents (Math/floor (apply * (conj multipliers (:cents money))))})

(defn divide [money & divisors]
  {:currency (:currency money)
   :cents (Math/floor (apply / (conj divisors (:cents money))))})

(defn round-down-to-multiple-of [round-to money]
  {:pre [(same-currency [money round-to])]}
  (if (nil? round-to)
    money
    {:currency (:currency money)
     :cents (-> money
                :cents
                (/ (:cents round-to))
                Math/floor
                (* (:cents round-to)))}))

(defn eq [& moneys]
  {:pre [(same-currency moneys)]}
  (->> moneys (map :cents) (apply =)))

(defn not-eq [& moneys]
  (not (apply eq moneys)))

(defn gt [& moneys]
  {:pre [(same-currency moneys)]}
  (->> moneys (map :cents) (apply >)))

(defn lt [& moneys]
  {:pre [(same-currency moneys)]}
  (->> moneys (map :cents) (apply <)))

(defn ge [& moneys]
  {:pre [(same-currency moneys)]}
  (->> moneys (map :cents) (apply >=)))

(defn le [& moneys]
  {:pre [(same-currency moneys)]}
  (->> moneys (map :cents) (apply <=)))

(defn zero? [money]
  (clojure.core/zero? (:cents money)))

(defn positive? [money]
  (pos? (:cents money)))

(defn negative? [money]
  (clojure.core/neg? (:cents money)))

(defn minimum
  ([]
   {:cents 0 :currency currencies/default-currency})
  ([& moneys]
   {:pre [(same-currency moneys)]}
   {:currency (first-non-zero-currency moneys)
    :cents (->> moneys (map :cents) (apply min))}))

(defn maximum
  ([]
   {:cents 0 :currency currencies/default-currency})
  ([& moneys]
   {:pre [(same-currency moneys)]}
   {:currency (first-non-zero-currency moneys)
    :cents (->> moneys (map :cents) (apply max))}))

(defn format-amount [{:keys [cents currency]}
                     {:keys [display-cents group-separator]}]
  (let [group-separator (or group-separator ",")
        currency-rules (currencies/currency->currency-rules currency)
        subunit-to-unit (:subunit-to-unit currency-rules)
        unit-amt (quot cents subunit-to-unit)
        subunit-amt (if (clojure.core/neg? cents)
                      (- (mod cents (- subunit-to-unit)))
                      (mod cents subunit-to-unit))
        format-str (cond (> subunit-to-unit 1000) ".%04d"
                         (> subunit-to-unit 100) ".%03d"
                         (> subunit-to-unit 10) ".%02d"
                         (> subunit-to-unit 1) ".%01d"
                         :else "")
        before-decimal (if (zero? unit-amt)
                         "0"
                         (->> unit-amt
                              Math/abs
                              str
                              reverse
                              (partition-all 3)
                              (map str/join)
                              (str/join group-separator)
                              reverse
                              str/join))
        minus-if-negative (when (clojure.core/neg? unit-amt) "-")]
    (str minus-if-negative
         before-decimal
         (when display-cents
           (gstring/format format-str subunit-amt)))))

(defn format
  ([amount]
   (format amount {:display-cents true}))
  ([{:keys [cents currency] :as amount} options]
   (if (js/isNaN cents)
     (recur {:cents 0 :currency currency} options)
     (str (format-amount amount options) " " currency))))

(defn percent-ratio [a b]
  {:pre [(same-currency [a b])]}
  (* (/ (:cents a) (:cents b)) 100))

(defn with-amount->with-cents [{:keys [:amount :currency]}
                               & [{:keys [:multiplier]}]]
  (let [multiplier (or multiplier 1)
        currency-rules (currencies/currency->currency-rules currency)
        parsed-amount (.parseFloat js/window amount)
        cents (if (.isNaN js/window parsed-amount)
                nil
                (.parseInt js/window (* parsed-amount
                                        (:subunit-to-unit currency-rules)
                                        multiplier)))]
    {:cents cents
     :currency currency}))