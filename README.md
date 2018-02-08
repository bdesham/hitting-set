# hitting-set [![Build Status](https://travis-ci.org/bdesham/hitting-set.svg?branch=master)](https://travis-ci.org/bdesham/hitting-set)

A Clojure library to find hitting sets and set covers.

## Installation

This library is available from [Clojars]. Just add

    [hitting-set "1.0.0"]

to your dependencies.

[Clojars]: https://clojars.org/

## Overview

This library deals with two closely related problems in graph theory: the hitting set problem and the set cover problem. These are explained pretty well [by Wikipedia][Wikipedia]. To summarize, if we have a hypergraph *H* consisting of a set of vertices and a set of edges, then

* a *hitting set* of *H* is a set of vertices that has a nonempty intersection with each edge, and
* a *set cover* of *H* is a set of edges which, taken together, contain all of the vertices of *H*.

This library is mostly concerned with the hitting-set formulation. If you’re interested in working with set covers you may be able to call `invert` on your hypergraph and then use the hitting-set functions to obtain your results.

People are generally interested in finding the *minimum* hitting sets, i.e. those with the minimum possible cardinality. It is possible for there to exist multiple hitting sets of that minimum cardinality.

There is also a function to find *minimal* hitting sets. A minimal hitting set has the property that if any element is removed from it then it is no longer a hitting set. A hitting set can be minimal without being minimum, but every minimum hitting set is necessarily minimal.

[Wikipedia]: https://en.wikipedia.org/wiki/Set_cover_problem

## Data structures

This library uses two different data structures to represent hypergraphs:

* A **labeled hypergraph** is one in which each edge has a name. It is represented by a map. Each key–value pair is an edge; the key is the edge name and the value is a set of vertices. For example, this hypergraph contains two edges and five vertices:

      {:germany #{:black :red :yellow}
       :united-states #{:blue :red :white}}

* An **unlabeled hypergraph** is one in which the edges are not named. It is represented by a set of sets. Each set is an edge and each value in the set is a vertex. This hypergraph is equivalent to the one in the previous example:

      #{#{:black :red :yellow}
        #{:blue :red :white}}

You can easily convert a labeled hypergraph to an unlabeled one by passing it through the built-in `vals` and `set` functions, in that order. The function `unlabel` is provided to do this for you.

The functions in the library don’t care whether the vertex values and edge names are strings, keywords, numbers, UUIDs, etc., but the automated tests use keywords exclusively.

## API

### Utility functions

* `(invert h)`

    Given a labeled hypergraph (i.e. a map from edge names to sets of vertices), “inverts” it by returning a map from vertices to the sets of the edges in which those vertices appeared.

* `(unlabel h)`

    Given a labeled hypergraph, returns the equivalent unlabeled hypergraph.

### Hitting set

* `(hitting-set? h t)`

    Returns true if `t` is a hitting set of the unlabeled hypergraph `h`.

* `(enumerate-hitting-sets h)`

  `(enumerate-hitting-sets h k)`

    Returns a set containing all of the minimal hitting sets, and possibly some non-minimal hitting sets, of the unlabeled hypergraph `h`. If `k` is passed then only sets with cardinality `k` or smaller will be returned.

* `(minimum-hitting-sets h)`

    Returns a set containing the minimum hitting sets of the unlabeled hypergraph `h`.

* `(minimal-hitting-sets h)`

    Returns a set containing the minimal hitting sets of the unlabeled hypergraph `h`.

* `(approx-hitting-set h)`

    Returns a hitting set of the unlabeled hypergraph `h` generated by inverting `h` and using the greedy cover algorithm on it. The returned set is guaranteed to be a hitting set, but it is not guaranteed to have the minimum possible cardinality.

### Set cover

* `(cover? h s)`

    Returns true if the set `s` is a cover for the labeled hypergraph `h`.

* `(greedy-cover h)`

    Uses the “greedy” algorithm (described [at Wikipedia](https://en.wikipedia.org/wiki/Set_cover_problem#Greedy_algorithm)) to generate a set cover for the labeled hypergraph `h`. The set cover does not necessarily have the minimum possible cardinality.

## A note on recursion

The functions `enumerate-hitting-sets`, `minimum-hitting-sets`, and `minimal-hitting-sets` are recursive but do not make use of Clojure’s tail recursion. This opens the possibility of a stack overflow. However, the recursion goes no deeper than `k` levels, where `k` is the number of vertices in the largest edge. Since the hitting set problem is NP-complete, the computation for small edge sizes will take so long that in practice you will give up (or run out of heap) long before the stack fills up.

## Further reading

* [An article (containing a hitting-set example) by the author of this library](https://esham.io/2012/09/olympic-colors)
* [The first chapter of *Parameterized Complexity Theory* by J. Flum and M. Grohe](http://www2.informatik.hu-berlin.de/~grohe/pub/pkbuch-chap1.pdf), which contains a discussion of the hitting set (among other problems) and which provided the algorithms which were adapted for this library
* Thanks go to [A.Schulz](http://cs.stackexchange.com/a/3281/2601), who pointed me in the right direction when I [asked this question](http://cs.stackexchange.com/q/3276/2601) on the Computer Science Stack Exchange

## Author

This program was created by [Benjamin Esham](https://esham.io).

This project is [hosted on GitHub](https://github.com/bdesham/hitting-set). Please feel free to submit pull requests.

## Version history

Version numbers are assigned to this project according to version 1.0.0 of the [Semantic Versioning](http://semver.org/) specification.

* 0.9.0 (2012-09-10)
    - Added support for the set-cover formulation of the problem via `cover?` and `greedy-cover`
    - Used the new set-cover functions to implement `approx-hitting-set` to find approximate hitting sets
    - Modified `hitting-set?` so that the hypergraph is the first argument to every function in the library
* 0.8.0 (2012-09-05)
    - Initial release.

## License

Copyright © 2012 Benjamin D. Esham. This project is distributed under the Eclipse Public License, the same as that used by Clojure. A copy of the license is included as “epl-v10.html” in this distribution.
