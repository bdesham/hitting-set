# hitting-set

A Clojure library to find minimal hitting sets.

## What’s this?

“Hitting set” is a problem in graph theory. It’s explained pretty well by [the Wikipedia article](https://secure.wikimedia.org/wikipedia/en/wiki/Set_cover_problem). To summarize, suppose we have a hypergraph *H* that consists of a set of vertices *V* and a set of edges *E*. Each edge must be nonempty (it must contain at least one vertex) but edges are not required to be unique—two edges can contain identical sets of vertices. Our goal is to find a subset (the “hitting set”) that has a nonempty intersection with each edge. We can further ask for the minimal hitting set, which is the hitting set of minimum cardinality. (There may be more than one hitting set with the minimum possible cardinality.)

By way of example, suppose that the hypergraph *H* is the set of national flags, the vertices are colors, and each edge is one country’s flag. The “United States” edge would contain the vertices red, white, and blue; the “Sweden” edge would contain blue and yellow; and so on. The [Olympic symbol](https://secure.wikimedia.org/wikipedia/en/wiki/Olympic_flag#Symbol) of five interlocking rings has the special property that at least one of its five colors is used by each national flag: the Olympic-ring colors are a hitting set for the hypergraph *H* of flags. (They’re actually not a minimal hitting set, as I demonstrate in [this article](http://www.bdesham.info/2012/09/olympic-colors).)

## Input format

In this library hypergraphs are implemented by maps. The keys of the maps are the edges of the hypergraph and the corresponding values are sets containing vertices. By way of example, here’s a (truncated) hypergraph of the flag example from the previous section:

```clj
{"Australia" #{:white :red :blue},
 "Tanzania" #{:black :blue :green :yellow},
 "Norway" #{:white :red :blue},
 "Uruguay" #{:white :blue :yellow},
 "Saint Vincent and the Grenadines" #{:blue :green :yellow},
 "Ivory Coast" #{:white :orange :green},
 "Sierra Leone" #{:white :blue :green},
 "United States" #{:white :red :blue}}
```

Here I have used strings for the keys and keywords for the elements of the sets, but you can use strings, keywords, or pretty much any other non-sequence data type for the edges or for the vertices.

## Usage

The `project.clj` specifies a minimum Leiningen version of 2.0.0, although there probably isn’t anything in this library that actually requires that version.

1. Add `[hitting-set "0.8.0"]` to the `:dependencies` vector in your `project.clj`.
2. `(:use hitting-set :only [minimal-hitting-sets])`, or whichever of the functions below you need.

Following are descriptions of the four “end-user” functions in the library.

* `hitting-set? [h s]`

    Returns true if `s` is a hitting set for `h` (i.e. if `s` has a nonempty intersection with each edge in `h`). This function does not check to ensure that `s` is of minimal size.

* `hitting-set-exists? [h k]`

    Returns true if a hitting set of size `k` exists for the hypergraph `h`. See the caveat below.

* `enumerate-hitting-sets [h] [h k]`

    Returns a set containing the minimal hitting sets of `h` and possibly (but not necessarily! see the caveat below) some *non*-minimal hitting sets. If `k` is passed then only sets of size `k` or smaller will be returned; if `k` is not included then sets will be returned without regard to their size.

* `minimal-hitting-sets [h]`

    Returns a set containing the minimal hitting sets of `h`. For example, if the minimal possible hitting set has size two and there are three hitting sets of size two, all three will be returned. If you want just one hitting set and don’t care about uniqueness, use `(first (minimal-hitting-sets h))`.

## Caveats

* Due to limitations in the algorithms in this library, you may experience the following oddities:

    1. `hitting-set-exists?` will correctly return `false` if you pass it a size `k` below that of the minimal hitting set, and it will correctly return `true` when you pass it a `k` that is equal to the size of the minimal hitting set. However, it may return `false` for larger values of `k`, even when the values are less than the number of vertices in the hypergraph. (Recall that if the minimal hitting set has size *a* and there are a total of *c* vertices in the hypergraph, then we can form a hitting set of any size *k* between *a* and *c*, inclusive, by adding elements to the minimal hitting set. The resulting set will still have the hitting set property that it has a nonempty intersection with each hyperedge, and so it will still be a hitting set.)

    2. `enumerate-hitting-sets` has a similar problem: It will always (correctly) return all possible *minimal* hitting sets, but it may not return hitting sets that are larger than minimal.

    I intend to fix both of these issues in a future version of the library. Note that if you’re only looking for *minimal* hitting sets then neither of these oddities is an issue.

* This library does not currently support approximate solutions. That’s on my list too.

* The functions `hitting-set-exists?` and `enumerate-hitting-sets` (and by extension `minimal-hitting-sets`) are recursive but do not make use of Clojure’s `recur` and friends. This opens the possibility of a stack overflow. In practice, though, the recursion goes no deeper than `k` levels, where `k` is the maximum edge size. Since the hitting set problem is NP-complete, the computation for small edge sizes will take so long that you’ll give up (or run out of heap) long before the stack fills up.

## Further reading

* [An article by the author of this library](http://www.bdesham.info/2012/09/olympic-colors)
* [The first chapter of *Parameterized Complexity Theory* by J. Flum and M. Grohe](http://www2.informatik.hu-berlin.de/~grohe/pub/pkbuch-chap1.pdf), which contains a discussion of the hitting set (among other problems) and which provided the algorithms which were adapted for this library

## History

Version numbers are assigned to this project according to version 1.0.0 of the [Semantic Versioning](http://semver.org/) specification.

* Version 0.8.0 (2012-09-05): Initial release.

## License

Copyright © 2012 Benjamin D. Esham (www.bdesham.info).

This project is distributed under the Eclipse Public License, the same as that used by Clojure. A copy of the license is included as “epl-v10.html” in this distribution.
