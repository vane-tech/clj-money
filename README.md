Clj-money
---------

A small Clojure & ClojureScript library to format and compute with money amounts.

Each value is represented as a simple map

```clojure
(require '[clj-money.main :as money])

(money/format (money/plus {:cents 432199, :currency "EUR"}
                          {:cents 5000000, :currency "EUR"})) # => "54,321.99 EUR"
```

It was extracted from a long existing internal usage, and is just tailored for our own use cases and probably needs some polishing. Feel free to fork, extend, adjust etc.

MIT License
===========

Copyright 2022 Vane GmbH

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
