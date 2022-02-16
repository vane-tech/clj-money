(ns clj-money.main
  (:refer-clojure :exclude [divide zero?])
  (:require
   [clj-money.currencies :as currencies]
   [clojure.string :as str]
   #?(:cljs [goog.string :as gstring])
   #?(:cljs [goog.string.format])))

(def zero {:cents 0})

(defn- NaN? [number]
  #?(:cljs (js/isNaN number)
     :clj (and (number? number) (Double/isNaN number))))

(defn- print-error [msg]
  #?(:cljs (.error js/console msg)
     :clj (binding [*out* *err*]
            (println msg))))

(defn- parse-float [s]
  #?(:cljs (.parseFloat js/window s)
     :clj (Float/parseFloat s)))

(defn- parse-int [s]
  #?(:cljs (.parseInt js/window s)
     :clj (java.lang.Integer/parseInt s)))

(defn- floor [f]
  (int (Math/floor f)))

(defn- same-currency [moneys]
  (let [currencies (->> moneys
                        (filter identity)
                        (filter #(not= 0 (:cents %)))
                        (map :currency))
        all-same? (or (empty? currencies)
                      (apply = currencies))]
    (when-not all-same?
      (print-error (str "Cannot compare amounts in different currencies unless the amount is zero: "
                        (pr-str moneys))))
    all-same?))

(defn- first-non-zero-currency [moneys]
  (or (->> moneys
           (filter #(not= 0 (:cents %)))
           first
           :currency)
      (-> moneys first :currency)
      currencies/default-currency))

(defn plus
  "Sums up all given money structures"
  [& moneys]
  {:pre [(same-currency moneys)]}
  {:currency (first-non-zero-currency moneys)
   :cents (->> moneys (map :cents) (reduce +))})

(defn minus
  "Substracts the given money structures from the first"
  [& moneys]
  {:pre [(same-currency moneys)]}
  {:cents (if (empty? moneys)
            0
            (->> moneys (map :cents) ((partial apply -))))
   :currency (first-non-zero-currency moneys)})

(defn multiply
  "Multipies the given money structure by the multipliers"
  [money & multipliers]
  {:currency (:currency money)
   :cents (floor (apply * (conj multipliers (:cents money))))})

(defn divide
  "Divides the given money structure by the divisors"
  [money & divisors]
  {:currency (:currency money)
   :cents (floor (apply / (conj divisors (:cents money))))})

(defn round-down-to-multiple-of
  "Rounds the second money structure to a multiple of the first
   E.g. (round-down-to-multiple-of {:cents 10000, :currency \"EUR\"} {:cents 39912, currency \"EUR\"})
        # => {:cents 30000, :currency \"EUR\"}"
  [round-to money]
  {:pre [(same-currency [money round-to])]}
  (if (nil? round-to)
    money
    {:currency (:currency money)
     :cents (-> money
                :cents
                (/ (:cents round-to))
                floor
                (* (:cents round-to)))}))

(defn eq
  "Returns true if all money structures are equal"
  [& moneys]
  {:pre [(same-currency moneys)]}
  (->> moneys (map :cents) (apply =)))

(defn not-eq
  "Returns true if the given money structures are not equal"
  [& moneys]
  (not (apply eq moneys)))

(defn gt
  "Returns true if the given money structures are >"
  [& moneys]
  {:pre [(same-currency moneys)]}
  (->> moneys (map :cents) (apply >)))

(defn lt
  "Returns true if the given money structures are <"
  [& moneys]
  {:pre [(same-currency moneys)]}
  (->> moneys (map :cents) (apply <)))

(defn ge
  "Returns true if the given money structures are >="
  [& moneys]
  {:pre [(same-currency moneys)]}
  (->> moneys (map :cents) (apply >=)))

(defn le
  "Returns true if the given money structures are <="
  [& moneys]
  {:pre [(same-currency moneys)]}
  (->> moneys (map :cents) (apply <=)))

(defn zero?
  "Returns true if the given money structure is zero"
  [money]
  (clojure.core/zero? (:cents money)))

(defn positive?
  "Returns true if the given money structure is positive"
  [money]
  (pos? (:cents money)))

(defn negative?
  "Returns true if the given money structure is negative"
  [money]
  (clojure.core/neg? (:cents money)))

(defn minimum
  "Returns the smallest of the given money structures"
  ([]
   {:cents 0 :currency currencies/default-currency})
  ([& moneys]
   {:pre [(same-currency moneys)]}
   {:currency (first-non-zero-currency moneys)
    :cents (->> moneys (map :cents) (apply min))}))

(defn maximum
  "Returns the biggest of the given money structure"
  ([]
   {:cents 0 :currency currencies/default-currency})
  ([& moneys]
   {:pre [(same-currency moneys)]}
   {:currency (first-non-zero-currency moneys)
    :cents (->> moneys (map :cents) (apply max))}))

(defn format-amount
  "Returns the formatted amount of the given money structure
   E.g. (format-amount {:cents 432199, :currency \"EUR\"})
        # => \"4,321\"
   :display-cents true - print cents (\"4,321.99\" in the example above\") - false per default
   :group-separator - use the given separator instead of the comma"
  [{:keys [cents currency]}
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
        before-decimal (if (clojure.core/zero? unit-amt)
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
           #?(:clj (clojure.core/format format-str subunit-amt)
              :cljs (gstring/format format-str subunit-amt))))))

(defn format
  "Returns the formatted amount and currency
  E.g. (format {:cents 432199, :currency \"EUR\"})
       # => \"4,321.99 EUR\"
  :display-cents false - hide the cents (\"4,321 EUR\" in the example above) - true per default"
  ([amount]
   (format amount {:display-cents true}))
  ([{:keys [cents currency] :as amount} options]
   (if (NaN? cents)
     (recur {:cents 0 :currency currency} options)
     (str (format-amount amount options) " " currency))))

(defn percent-ratio
  "Returns the ratio between the given money structures as a percent value
  E.g. (percent-ratio {:cents 218, :currency \"EUR\"} {:cents 1000, :currency \"EUR\"})
       # => {:cents 109/5, :currency \"EUR\"}"
  [a b]
  {:pre [(same-currency [a b])]}
  (* (/ (:cents a) (:cents b)) 100))

(defn with-amount->with-cents [{:keys [:amount :currency]}
                               & [{:keys [:multiplier]}]]
  (let [multiplier (or multiplier 1)
        currency-rules (currencies/currency->currency-rules currency)
        parsed-amount (parse-float amount)
        cents (if (NaN? parsed-amount)
                nil
                (parse-int (* parsed-amount
                              (:subunit-to-unit currency-rules)
                              multiplier)))]
    {:cents cents
     :currency currency}))
