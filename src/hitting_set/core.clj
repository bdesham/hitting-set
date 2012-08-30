(ns hitting-set.core
  (:use [clojure.set :only [difference intersection union]]))

; Utility functions

(defn- dissoc-elements-containing
  "Given a map in which the keys are sets, remove all keys whose sets contain
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

(defn- not-empty?
  [s]
  (> (count s) 0))

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

(defn hitting-set?
  "Returns true if for each set in s, that set contains at least one element of
  h. Does not check whether h is minimal."
  [h s]
  (not-any? empty? (map #(intersection % h) s)))

(defn hitting-set-exists?
  "Returns true iff a hitting set of size k exists for the hypergraph h."
  [h k]
  (cond
    (< (count (apply union (vals h))) k) false
    (empty? h) true
    (zero? k) false
    :else (let [hvs (map #(dissoc-elements-containing % h)
                         (first (vals h)))]
            (boolean (some #(hitting-set-exists? % (dec k))
                           hvs)))))

(defn enumerate-hitting-sets
  ([h k]
   (enumerate-hitting-sets h k #{}))
  ([h k x]
   (cond
     (empty? h) #{x}
     (zero? k) #{}
     :else (let [hvs (map-old-new #(dissoc-elements-containing % h)
                                  (first (vals h)))]
             (apply union (map #(enumerate-hitting-sets (second %)
                                                        (dec k)
                                                        (union x #{(first %)}))
                               hvs))))))

(defn smallest-hitting-sets
  "Returns a set containing the smallest hitting sets of the map m."
  [m]
  (first (filter not-empty?
                 (map #(enumerate-hitting-sets m %)
                      (range 1 (inc (count (apply union (vals m)))))))))
