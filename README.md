Clj-money
---------

[![CircleCI](https://circleci.com/gh/vane-tech/clj-money/tree/main.svg?style=svg)](https://circleci.com/gh/vane-tech/clj-money/tree/main)

A small Clojure & ClojureScript library to format and compute with money amounts.

Each value is represented as a simple map

```clojure
(require '[clj-money.main :as money])

(money/format (money/plus {:cents 432199, :currency "EUR"}
                          {:cents 5000000, :currency "EUR"})) # => "54,321.99 EUR"
```

It was extracted from a long existing internal usage, and is just tailored for our own use cases and probably needs some polishing. Feel free to fork, extend, adjust etc.

Releasing a new version
=======================

First, open [build.clj](./build.clj) and update the `version`. Then run these commands:

```sh
export MONEY_VERSION=$(grep -Po 'def version "\K.*?(?=")' build.clj)
git commit -pm "Release $MONEY_VERSION"
git tag v$MONEY_VERSION
git push
git push --tags
bin/in_docker clj -T:build jar
 CLOJARS_PASSWORD=<Clojars Deploy token> bin/in_docker clj -T:build deploy
```

MIT License
===========

Copyright 2022 Vane GmbH

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
