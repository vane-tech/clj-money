(ns clj-money.main-test
  (:require
   [clojure.test :refer [deftest testing is are]]
   [clj-money.main :as money]))

(defn- money [cents currency]
  {:cents cents, :currency currency})
(defn- dkk [cents]
  (money cents "DKK"))
(defn- eur [cents]
  (money cents "EUR"))
(defn- gbp [cents]
  (money cents "GBP"))
(defn- krw [cents]
  (money cents "KRW"))
(defn- usd [cents]
  (money cents "USD"))

(deftest plus-test
  (testing "same currency"
    (is (= (eur 100)
           (money/plus (eur 30)
                       (eur 24)
                       (eur 46)))))
  (testing "same currency for all non zero values"
    (is (= (eur 100)
           (money/plus (usd 0)
                       (eur 40)
                       (gbp 0)
                       (eur 0)
                       (eur 60)))))
  (testing "different currencies"
    (print "Expecting error message: ")
    (is (thrown? js/Error.
                 (money/plus (usd 50)
                             (eur 20))))))

(deftest minus-test
  (testing "same currency"
    (are [result amounts] (= result (apply money/minus amounts))
      (eur 0) [] ; expected [given amounts]
      (eur -100) [(eur 100)]
      (usd 50) [(usd 100) (usd 50)]
      (usd 45) [(usd 100) (usd 50) (usd 5)]
      (usd -100) [(usd -50) (usd 50)]
      (krw -50) [(krw -100) (krw -50)]))
  (testing "same currency for all non zero values"
    (is (= (krw 5000)
           (money/minus (krw 10000)
                        (eur 0)
                        (krw 5000)
                        (usd 0)))))
  (testing "different currencies"
    (print "Expecting error message: ")
    (is (thrown? js/Error.
                 (money/minus (usd 50)
                              (eur 20))))))

(deftest multiply-test
  (are [result amounts] (= result (apply money/multiply amounts))
    (eur 100) [(eur 100)]
    (usd 200) [(usd 100) 2]
    (eur 2000) [(eur 100) 2 10]
    (dkk -500) [(dkk -125) 4]
    (usd -500) [(usd 125) -4]
    (eur 100) [(eur -20) -5]
    (eur 119) [(eur 20) 5.99]
    (usd 73) [(usd 100) 0.7392]
    (krw -120) [(krw 20) -5.99]))

(deftest minimum-test
  (testing "Same currency"
    (are [result amounts] (= result (apply money/minimum amounts))
      (eur 0) []
      (eur 0) [(eur 100) (usd 0) (eur 50)]
      (eur 50) [(eur 50)]
      (eur -50) [(eur 50) (eur -50)]
      (usd 100) [(usd 100) (usd 101)]))
  (testing "different currencies but with zero amounts"
    (are [result amounts] (= result (apply money/minimum amounts))
      (usd 0) [(usd 0) (gbp 0)]
      (eur 0) [(eur 50) (gbp 0)]))
  (testing "different currencies"
    (print "Expecting 2 error messages: ")
    (is (thrown? js/Error.
                 (money/minimum (usd -100) (eur 100))))
    (is (thrown? js/Error.
                 (money/minimum (usd 100) (eur 100))))))

(deftest maximum-test
  (testing "Same currency"
    (are [result amounts] (= result (apply money/maximum amounts))
      (eur 0) []
      (eur 100) [(eur 100) (usd 0) (eur 50)]
      (eur 50) [(eur 50)]
      (eur 50) [(eur 50) (eur -50)]
      (usd 101) [(usd 100) (usd 101)]))
  (testing "different currencies but with zero amounts"
    (are [result amounts] (= result (apply money/maximum amounts))
      (usd 0) [(usd 0) (gbp 0)]
      (eur 50) [(eur 50) (gbp 0)]))
  (testing "different currencies"
    (print "Expecting 2 error messages:")
    (is (thrown? js/Error.
                 (money/maximum (usd -100) (eur 100))))
    (is (thrown? js/Error.
                 (money/maximum (usd 100) (eur 100))))))

(deftest positive?-test
  (are [result money] (= result (money/positive? money))
    true (eur 100)
    true (krw 1)
    false (usd 0)
    false (gbp -1)
    false (eur -100)))

(deftest negative?-test
  (are [result money] (= result (money/negative? money))
    false (eur 100)
    false (krw 1)
    false (usd 0)
    true (gbp -1)
    true (eur -100)))

(deftest zero?-test
  (are [result money] (= result (money/zero? money))
    true (eur 0)
    true (krw 0)
    false (usd 1)
    false (eur -1000000)))

(deftest format-test
  (are [str amount] (= str (money/format amount))
    "123.45 EUR" (eur 12345)
    "9,876,543.21 USD" (usd 987654321)
    "987,654,321 KRW" (krw 987654321)
    "10,000.00 DKK" (dkk 1000000)
    "-1.45 EUR" (eur -145)
    "-987.00 EUR" (eur -98700)
    "-100.45 USD" (usd -10045)
    "-1.01 DKK" (dkk -101)
    "-10,000 KRW" (krw -10000)
    "0.00 EUR" (eur js/NaN))
  (testing "cents"
    (is (= "123 EUR" (money/format (eur 12345) {:display-cents false})))
    (is (= "123.45 EUR" (money/format (eur 12345) {:display-cents true})))
    (is (= "10,000 DKK" (money/format (dkk 1000000) {:display-cents false})))))
