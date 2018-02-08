(ns hitting-set-test
  (:use clojure.test
        hitting-set))

(defn refer-private [ns]
  (doseq [[symbol var] (ns-interns ns)]
    (when (:private (meta var))
      (intern *ns* symbol var))))

(refer-private 'hitting-set)

(deftest test-count-vertices
  (let [a #{#{:g :r :f :j} #{:a :b :j :g} #{:r}}
        flags #{#{:red :white :blue}
                #{:black :red :yellow}
                #{:blue :yellow}}]
    (is (= (count-vertices a) 6))
    (is (= (count-vertices flags) 5))))

(deftest test-label
  (let [graph1 #{}
        graph2 #{#{:a}}
        graph3 #{#{:a :b}}
        graph4 #{#{:a :b :c}
                 #{:a :b}
                 #{:b :c}}]
    (is (= graph1 (unlabel (label graph1))))
    (is (= graph2 (unlabel (label graph2))))
    (is (= graph3 (unlabel (label graph3))))
    (is (= graph4 (unlabel (label graph4))))))

(deftest test-invert
  (let [a {:key1 #{:g :r :f :j} :key2 #{:a :b :j :g} :key3 #{:r}}]
    (is (= (invert a)
           {:r #{:key3 :key1}
            :a #{:key2}
            :b #{:key2}
            :f #{:key1}
            :g #{:key2 :key1}
            :j #{:key2 :key1}}))
    (is (= a (invert (invert a))))))

(deftest test-unlabel
  (let [a {:key1 #{:g :r :f :j}
           :key2 #{:a :b :j :g}
           :key3 #{:r}}
        unlabeled-a #{#{:g :r :f :j}
                      #{:a :b :j :g}
                      #{:r}}
        flags {:usa #{:red :white :blue}
               :germany #{:black :red :yellow}
               :sweden #{:blue :yellow}}
        unlabeled-flags #{#{:red :white :blue}
                          #{:black :red :yellow}
                          #{:blue :yellow}}]
    (is (= (unlabel a) unlabeled-a))
    (is (= (unlabel flags) unlabeled-flags))))

(deftest hitting-set-generic
  (let [graph #{#{:g :r :f :j}
                #{:a :b :j :g}
                #{:r}}]

    (is (hitting-set? graph #{:r :g}))
    (is (hitting-set? graph #{:r :j}))
    (is (hitting-set? graph #{:r :g :b}))
    (is (hitting-set? graph #{:r :b}))
    (is (false? (hitting-set? graph #{:r})))
    (is (false? (hitting-set? graph #{:a :b :g :j})))

    (let [expected-hitting-sets #{#{:r :a}
                                  #{:r :b}
                                  #{:r :g}
                                  #{:r :j}}]

      (is (= (minimum-hitting-sets graph)
             expected-hitting-sets))

      (is (= (minimal-hitting-sets graph)
             expected-hitting-sets)))

    (is (hitting-set? graph (approx-hitting-set graph)))))

(deftest minimal-different-from-minimum-cardinality
  (let [graph #{#{:orange :lime :apricot}
                #{:orange :lime :apple :pear}}]

    (is (hitting-set? graph #{:orange}))
    (is (hitting-set? graph #{:orange :apple}))
    (is (hitting-set? graph #{:orange :apricot}))
    (is (hitting-set? graph #{:orange :pear}))
    (is (hitting-set? graph #{:orange :lime}))
    (is (hitting-set? graph #{:orange :lime :pear}))
    (is (hitting-set? graph #{:lime}))
    (is (hitting-set? graph #{:lime :apple}))
    (is (hitting-set? graph #{:lime :apricot}))
    (is (hitting-set? graph #{:lime :pear}))
    (is (hitting-set? graph #{:apricot :apple}))
    (is (hitting-set? graph #{:apricot :pear}))
    (is (false? (hitting-set? graph #{:apricot})))
    (is (false? (hitting-set? graph #{:apple})))
    (is (false? (hitting-set? graph #{:apple :pear})))
    (is (false? (hitting-set? graph #{:pear})))

    (is (= (minimum-hitting-sets graph)
           #{#{:orange}
             #{:lime}}))

    (is (= (minimal-hitting-sets graph)
           #{#{:orange}
             #{:lime}
             #{:apricot :apple}
             #{:apricot :pear}}))

    (is (hitting-set? graph (approx-hitting-set graph)))))

(deftest flags
  (let [flags {:usa #{:red :white :blue}
               :germany #{:black :red :yellow}
               :sweden #{:blue :yellow}}
        graph (unlabel flags)]

    (is (hitting-set? graph #{:red :blue}))
    (is (hitting-set? graph #{:red :yellow}))
    (is (hitting-set? graph #{:blue :black}))
    (is (hitting-set? graph #{:blue :yellow}))
    (is (false? (hitting-set? graph #{:red :white})))
    (is (false? (hitting-set? graph #{:red :black})))

    (let [expected-hitting-sets #{#{:red :yellow}
                                  #{:blue :yellow}
                                  #{:black :blue}
                                  #{:red :blue}
                                  #{:white :yellow}}]

      (is (= (minimum-hitting-sets graph)
             expected-hitting-sets))

      (is (= (minimal-hitting-sets graph)
             expected-hitting-sets)))

    (is (hitting-set? graph (approx-hitting-set graph)))

    (is (cover? flags #{:usa :germany}))
    (is (false? (cover? flags #{:usa})))
    (is (false? (cover? flags #{:germany})))
    (is (false? (cover? flags #{:sweden})))

    (is (= (greedy-cover flags)
           #{:usa :germany}))))
