# metrics-clojure-cloudwatch

A reporter for [metrics-clojure](https://github.com/metrics-clojure/metrics-clojure)
that sends metrics to [AWS CloudWatch](https://aws.amazon.com/cloudwatch/).

[![clojars badge](https://img.shields.io/clojars/v/tessellator/metrics-clojure-cloudwatch.svg)](https://clojars.org/tessellator/metrics-clojure-cloudwatch)


## Quick Start

```clojure
(require '[metrics.reporters.cloudwatch :as cloudwatch])

(def CWR (cloudwatch/reporter reg {:namespace "MyNamespace"}))
(cloudwatch/start CWR 60)
```

This will tell the `metrics` library to submit the most recent value of each
metric to CloudWatch (every 60 seconds) in the MyNamespace custom namespace.
Please consult the docstring for `reporter` to see the complete list of options
and information about default values.

For more information about how to use the metrics library as well as
information about other reporters, please refer to the
[metrics-clojure docs](https://metrics-clojure.readthedocs.io/en/latest/index.html).


## License

Copyright © 2020 Thomas C. Taylor and contributors.

Distributed under the Eclipse Public License version 2.0.
