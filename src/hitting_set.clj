(ns hitting-set
  (:use [clojure.set :only [difference intersection union]]))

; Utility functions

(defn- dissoc-elements-containing
  "Given a map in which the keys are sets, removes all keys whose sets contain
  the element el. Adapted from http://stackoverflow.com/a/2753997/371228"
  [el m]
  (apply dissoc m (keep #(-> % val
                           (not-any? #{el})
                           (if nil (key %)))
                        m)))

(defn- map-old-new
  "Returns a sequence of vectors. Each first item is an element of coll and the
  second item is the result of calling f with that item."
  [f coll]
  (map #(vector % (f %)) coll))

(defn- count-vertices
  "Returns the number of vertices in the hypergraph h."
  [h]
  (count (apply union (vals h))))

(defn- sorted-hypergraph
  "Returns a version of the hypergraph h that is sorted so that the edges with
  the fewest vertices come first."
  [h]
  (into (sorted-map-by (fn [key1 key2]
                         (compare [(count (get h key1)) key1]
                                  [(count (get h key2)) key2])))
        h))

(defn- remove-dupes
  "Given a map m, remove all but one of the keys that map to any given value."
  [m]
  (loop [sm (sorted-map),
         m m,
         seen #{}]
    (if-let [head (first m)]
      (if (contains? seen (second head))
        (recur sm
               (rest m)
               seen)
        (recur (assoc sm (first head) (second head))
               (rest m)
               (conj seen (second head))))
      sm)))

(defn- efficient-hypergraph
  "Given a hypergraph h, returns an equivalent hypergraph that will go through
  the hitting set algorithm more quickly. Specifically, redundant edges are
  discarded and then the map is sorted so that the smallest edges come first."
  [h]
  (-> h remove-dupes sorted-hypergraph))

(defn- largest-edge
  "Returns the name of the edge of h that has the greatest number of vertices."
  [h]
  (first (last (sorted-hypergraph h))))

(defn- remove-vertices
  "Given a hypergraph h and a set vv of vertices, remove the vertices from h
  (i.e. remove all of the vertices of vv from each edge in h). If this would
  result in an edge becoming empty, remove that edge entirely."
  [h vv]
  (loop [h h,
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

; Auxiliary functions
;
; These functions might be useful if you're working with hitting sets, although
; they're not actually invoked anywhere else in this project.

(defn reverse-map
  "Takes a map from keys to sets of values. Produces a map in which the values
  are mapped to the set of keys in whose sets they originally appeared."
  [m]
  (apply merge-with into
         (for [[k vs] m]
           (apply hash-map (flatten (for [v vs]
                                      [v #{k}]))))))

(defn drop-elements
  "Given a set of N elements, return a set of N sets, each of which is the
  result of removing a different item from the original set."
  [s]
  (set (for [e s] (difference s #{e}))))

; The main functions
;
; These are the functions that users are probably going to be interested in.

; Hitting set

(defn hitting-set?
  "Returns true if t is a hitting set of h. Does not check whether s is
  minimal."
  [h t]
  (not-any? empty? (map #(intersection % t)
                        (vals h))))

(defn hitting-set-exists?
  "Returns true if a hitting set of size k exists for the hypergraph h. See the
  caveat in README.md for odd behavior of this function."
  [h k]
  (cond
    (< (count-vertices h) k) false
    (empty? h) true
    (zero? k) false
    :else (let [hvs (map #(dissoc-elements-containing % h)
                         (first (vals h)))]
            (boolean (some #(hitting-set-exists? % (dec k))
                           hvs)))))

(defn- enumerate-algorithm
  [h k x]
  (cond
     (empty? h) #{x}
     (zero? k) #{}
     :else (let [hvs (map-old-new #(dissoc-elements-containing % h)
                                  (first (vals h)))]
             (apply union (map #(enumerate-algorithm (second %)
                                                     (dec k)
                                                     (union x #{(first %)}))
                               hvs)))))

(defn enumerate-hitting-sets
  "Return a set containing the hitting sets of h. See the caveat in README.md
  for odd behavior of this function. If the parameter k is passed then the
  function will return all hitting sets of size less than or equal to k."
  ([h]
   (enumerate-algorithm (efficient-hypergraph h) (count-vertices h) #{}))
  ([h k]
   (enumerate-algorithm (efficient-hypergraph h) k #{})))

(defn minimal-hitting-sets
  "Returns a set containing the minimal hitting sets of the hypergraph h. If
  you just want one hitting set and don't care whether there are multiple
  minimal hitting sets, use (first (minimal-hitting-sets h))."
  [h]
  (first (filter #(> (count %) 0)
                 (map #(enumerate-hitting-sets h %)
                      (range 1 (inc (count-vertices h)))))))

; Set cover

(defn cover?
  "Returns true if the elements of s form a set cover for the hypergraph h."
  [h s]
  (= (apply union (vals h))
     (apply union (map #(get h %) s))))

(defn greedy-cover
  "Returns a set cover of h using the 'greedy' algorithm."
  [h]
  (loop [hh h,
         edges #{}]
    (if (cover? h edges)
      edges
      (let [e (largest-edge hh)]
        (recur (remove-vertices hh (get hh e))
               (conj edges e))))))

(defn approx-hitting-set
  "Returns a hitting set of h. The set is guaranteed to be a hitting set, but
  may not be minimal."
  [h]
  (greedy-cover (reverse-map h)))
