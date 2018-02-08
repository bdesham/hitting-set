(ns hitting-set
  (:require [clojure.set :refer [difference intersection subset? union]]))

; Private utility functions

(defn- dissoc-elements-containing
  "Given a seq of sets, removes all items that contain the element el."
  [el m]
  (remove #(contains? % el) m))

(defn- map-old-new
  "Returns a sequence of vectors. Each first item is an element of coll and the
  second item is the result of calling f with that item."
  [f coll]
  (map #(vector % (f %)) coll))

(defn- count-vertices
  "Returns the number of vertices in the unlabeled hypergraph h."
  [h]
  (count (apply union h)))

(defn- sorted-hypergraph
  "Returns a version of the unlabeled hypergraph h that is sorted so that the
  edges with the fewest vertices come first."
  [h]
  (sort-by count h))

(defn- efficient-hypergraph
  "Given an unlabeled hypergraph h, returns an equivalent hypergraph that will
  go through the hitting set algorithm more quickly. Specifically, redundant
  edges are discarded and then the hypergraph is sorted so that the smallest
  edges come first."
  [h]
  (-> h set sorted-hypergraph))

(defn- largest-edge
  "Returns the name of the edge of the labeled hypergraph h that has the
  greatest number of vertices."
  [h]
  (first (last (sorted-hypergraph h))))

(defn- remove-vertices
  "Given a labeled hypergraph h and a set vv of vertices, remove the vertices
  from h (i.e. remove all of the vertices of vv from each edge in h). If this
  would result in an edge becoming empty, remove that edge entirely."
  [h vv]
  (loop [h h
         res {}]
    (if (first h)
      (let [edge (difference (second (first h))
                             vv)]
        (if (< 0 (count edge))
          (recur (rest h)
                 (assoc res (first (first h)) edge))
          (recur (rest h)
                 res)))
      res)))

(defn- label
  "Given an unlabeled hypergraph h, convert it to a labeled hypergraph by
  assigning a dummy name to each edge."
  [h]
  (apply hash-map (interleave (iterate inc 0)
                              h)))

; Public utility functions

(defn invert
  "Takes a map from keys to sets of values. Produces a map in which the values
  are mapped to the set of keys in whose sets they originally appeared."
  [m]
  (apply merge-with into
         (for [[k vs] m]
           (apply hash-map (flatten (for [v vs]
                                      [v #{k}]))))))

(defn unlabel
  "Takes a labeled hypergraph and returns the equivalent unlabeled hypergraph."
  [h]
  (set (vals h)))

; Hitting set

(defn hitting-set?
  "Returns true if t is a hitting set of the unlabeled hypergraph h. Does not
  check whether t is minimal or whether it has the minimum possible cardinality
  for a hitting set of h."
  [h t]
  (not-any? empty? (map #(intersection % t) h)))

(defn- enumerate-algorithm
  [h k x]
  (cond
     (empty? h) #{x}
     (zero? k) #{}
     :else (let [hvs (map-old-new #(dissoc-elements-containing % h)
                                  (first h))]
             (apply union (map #(enumerate-algorithm (second %)
                                                     (dec k)
                                                     (union x #{(first %)}))
                               hvs)))))

(defn enumerate-hitting-sets
  "Returns a set containing all of the minimal hitting sets, and possibly some
  non-minimal hitting sets, of the unlabeled hypergraph h.

  If k is passed then only sets with cardinality k or smaller will be
  returned."
  ([h]
   (enumerate-algorithm (efficient-hypergraph h) (count-vertices h) #{}))
  ([h k]
   (enumerate-algorithm (efficient-hypergraph h) k #{})))

(defn minimum-hitting-sets
  "Returns a set containing the minimum hitting sets of the unlabeled
  hypergraph h."
  [h]
  (first (filter seq
                 (map #(enumerate-hitting-sets h %)
                      (range 1 (inc (count-vertices h)))))))

(defn minimal-hitting-sets
  "Returns a set containing the minimal hitting sets of the unlabeled
  hypergraph h."
  [h]
  (let [hitting-sets (enumerate-hitting-sets h)]
    (set (filter
           (fn [hs]
             (let [others (disj hitting-sets hs)]
               (not-any? #(subset? % hs) others)))
           hitting-sets))))

(declare greedy-cover)

(defn approx-hitting-set
  "Returns a hitting set of the unlabeled hypergraph h. The hitting set is not
  guaranteed to be minimum or minimal."
  [h]
  (greedy-cover (invert (label h))))

; Set cover

(defn cover?
  "Returns true if the elements of s form a set cover for the labeled
  hypergraph h."
  [h s]
  (= (apply union (vals h))
     (apply union (map #(get h %) s))))

(defn greedy-cover
  "Returns a set cover of the labeled hypergraph h using the \"greedy\"
  algorithm."
  [h]
  (loop [hh h
         edges #{}]
    (if (cover? h edges)
      edges
      (let [e (largest-edge hh)]
        (recur (remove-vertices hh (get hh e))
               (conj edges e))))))
