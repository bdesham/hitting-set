(ns hitting-set-test
  (:require [clojure.set :refer [union]])
  (:use clojure.test
        hitting-set))

(defn refer-private [ns] 
  (doseq [[symbol var] (ns-interns ns)] 
    (when (:private (meta var)) 
      (intern *ns* symbol var)))) 

(refer-private 'hitting-set)

(deftest utility-functions
  (let [a {:key1 #{:g :r :f :j} :key2 #{:a :b :j :g} :key3 #{:r}}
        b #{:a :b :c :d :e}
        flags {:usa #{:red :white :blue}
               :germany #{:black :red :yellow}
               :sweden #{:blue :yellow}}]
    (is (= (count-vertices a) 6))
    (is (= (count-vertices flags) 5))

    (is (= (reverse-map a)
           {:r #{:key3 :key1}
            :a #{:key2}
            :b #{:key2}
            :f #{:key1}
            :g #{:key2 :key1}
            :j #{:key2 :key1}}))
    (is (= a (reverse-map (reverse-map a))))

    (is (= b (apply union (drop-elements b))))
    (is (= (count b) (count (drop-elements b))))
    (is (= (drop-elements b)
           #{#{:a :c :b :d} #{:a :c :b :e} #{:a :c :d :e}
             #{:a :b :d :e} #{:c :b :d :e}}))))

(deftest hitting-set-generic
  (let [a {:key1 #{:g :r :f :j}
           :key2 #{:a :b :j :g}
           :key3 #{:r}}]

    (is (hitting-set? a #{:r :g}))
    (is (hitting-set? a #{:r :j}))
    (is (hitting-set? a #{:r :g :b}))
    (is (hitting-set? a #{:r :b}))

    (is (false? (hitting-set? a #{:r})))
    (is (false? (hitting-set? a #{:a :b :g :j})))
    (is (false? (hitting-set-exists? a 0)))
    (is (false? (hitting-set-exists? a 1)))
    (is (hitting-set-exists? a 2))

    (is (= (minimal-hitting-sets a)
           #{#{:r :a} #{:r :b} #{:r :g} #{:r :j}}))))

(deftest hitting-set-flags
  (let [flags {:usa #{:red :white :blue}
               :germany #{:black :red :yellow}
               :sweden #{:blue :yellow}}]

    (is (hitting-set? flags #{:red :blue}))
    (is (hitting-set? flags #{:red :yellow}))
    (is (hitting-set? flags #{:blue :black}))
    (is (hitting-set? flags #{:blue :yellow}))
    (is (false? (hitting-set? flags #{:red :white})))
    (is (false? (hitting-set? flags #{:red :black})))

    (is (false? (hitting-set-exists? flags 0)))
    (is (false? (hitting-set-exists? flags 1)))
    (is (hitting-set-exists? flags 2))

    (is (= (minimal-hitting-sets flags)
           #{#{:red :yellow}
             #{:blue :yellow}
             #{:black :blue}
             #{:red :blue}
             #{:white :yellow}}))))

(deftest set-cover-flags
  (let [flags {:usa #{:red :white :blue}
               :germany #{:black :red :yellow}
               :sweden #{:blue :yellow}}]

    (is (cover? flags #{:usa :germany}))
    (is (false? (cover? flags #{:usa})))
    (is (false? (cover? flags #{:germany})))
    (is (false? (cover? flags #{:sweden})))

    (is (= (greedy-cover flags)
           #{:usa :germany}))))
