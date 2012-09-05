(ns hitting-set-test
  (:use clojure.test
        hitting-set))

(defn refer-private [ns] 
  (doseq [[symbol var] (ns-interns ns)] 
    (when (:private (meta var)) 
      (intern *ns* symbol var)))) 

(refer-private 'hitting-set)

(deftest utility-functions
         (let [a {:key1 #{:g :r :f :j} :key2 #{:a :b :j :g} :key3 #{:r}},
               b #{:a :b :c :d :e},
               flags {:usa #{:red :white :blue}
                      :germany #{:black :red :yellow}
                      :sweden #{:blue :yellow}}]
           (is (= (vertices a) 6))
           (is (= (vertices flags) 5))
           (is (= (reverse-map a)
                  {:r #{:key3 :key1}, :a #{:key2}, :b #{:key2}, :f #{:key1},
                   :g #{:key2 :key1}, :j #{:key2 :key1}}))
           (is (= a (reverse-map (reverse-map a))))
           (is (= b (apply clojure.set/union (drop-elements b))))
           (is (= (count b) (count (drop-elements b))))
           (is (= (drop-elements b)
                  #{#{:a :c :b :d} #{:a :c :b :e} #{:a :c :d :e}
                    #{:a :b :d :e} #{:c :b :d :e}}))
           (is (hitting-set? #{:r :g} a))
           (is (hitting-set? #{:r :j} a))
           (is (hitting-set? #{:r :g :b} a))
           (is (hitting-set? #{:r :b} a))
           (is (false? (hitting-set? #{:r} a)))
           (is (false? (hitting-set? #{:a :b :g :j} a)))))
