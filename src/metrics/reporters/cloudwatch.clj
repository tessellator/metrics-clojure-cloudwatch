(ns metrics.reporters.cloudwatch
  "Functions for creating and executing CloudWatch reporters."
  (:require [clojure.string :as str]
            [metrics.core :refer [default-registry]]
            [metrics.reporters :as mrep])
  (:import [com.amazonaws.services.cloudwatch
            AmazonCloudWatchAsync]
           [com.blacklocus.metrics
            CloudWatchReporter
            CloudWatchReporterBuilder]
           [com.codahale.metrics
            MetricFilter
            MetricRegistry]
           [java.util.function
            Predicate]))

(defn- ^CloudWatchReporterBuilder builder-for-registry
  [^MetricRegistry reg]
  (-> (CloudWatchReporterBuilder.)
      (.withRegistry reg)))

(defn- ^String dim-str [m]
  (str/join " " (map (fn [[k v]] (format "%s=%s" (name k) (name v))) m)))

(defn ^CloudWatchReporter reporter
  "Builds a CloudWatch reporter.

  `reg` is a metrics registry. If a registry is not provided, the reporter will
  use the default registry from the metrics-clojure library.

  `opts` is a map containing configuration data for the reporter. `:namespace`
  is a required key, but all others are optional. The options include:

  * `:namespace` - the name of the CloudWatch custom namespace

  * `:client` - an `AmazonCloudWatchAsync` object
      If one is not provided, a client is created using a credentials provider
      chain that searches for credentials in the following order:
        1. Environment vars AWS_ACCESS_KEY and AWS_SECRET_KEY
        2. Java system properties aws.accessKeyId and aws.secretKey
        3. Credential profiles file at the default location (~/.aws/credentials)
        4. Instance profile credentials delivered through the EC2 metadata service

  * `:filter` - a `com.codahale.metrics.MetricFilter`
      Only the metrics that pass the filter (i.e., the filter returns true) will
      be sent to CloudWatch. Defaults to always return true.

  * `:dimensions` - a map of name/value pairs of dims appended to all metrics

  * `:timestamp-local` - a bool indicating whether metrics are timestamped locally
      If true metric data is timestamped locally, else CloudWatch will timestamp
      the metric upon receipt. Defaults to false.

  * `:reporter-filter` - a predicate that indicates whether to submit a metric
      The function will be called with a `com.amazonaws.services.cloudwatch.model.MetricDatum`
      just before submitting to CloudWatch. If the predicate returns true, then
      the metric will be submitted, otherwise the metric is excluded from the
      submission. Defaults to always return true.

  * Metric type dimension names
      The following key names describe the name of the 'metric type' dimension
      for a few different cases, described by the key name. Each type has a
      distinct default value based on its type (e.g., `:type-dim-val-gauge`
      defaults to 'gauge'.

      - `:type-dim-name`
      - `:type-dim-val-gauge`
      - `:type-dim-val-counter-count`
      - `:type-dim-val-meter-count`
      - `:type-dim-val-histo-samples`
      - `:type-dim-val-histo-stats`
      - `:type-dim-val-timer-samples`
      - `:type-dim-val-timer-stats`"
  ([opts]
   (reporter default-registry opts))
  ([^MetricRegistry reg {:keys [namespace client filter dimensions timestamp-local
                                type-dim-name type-dim-val-gauge type-dim-val-counter-count
                                type-dim-val-meter-count type-dim-val-histo-samples
                                type-dim-val-histo-stats type-dim-val-timer-samples
                                type-dim-val-timer-stats reporter-filter] :as opts}]
   (let [b (builder-for-registry reg)]
     (when-let [^String n namespace]
       (.withNamespace b n))
     (when-let [^AmazonCloudWatchAsync c client]
       (.withClient b c))
     (when-let [^MetricFilter f filter]
       (.withFilter b f))
     (when dimensions
       (let [^String s (dim-str dimensions)]
         (.withDimensions b s)))
     (when-let [^Boolean tl timestamp-local]
       (.withTimestampLocal b tl))
     (when-let [^String s type-dim-name]
       (.withTypeDimName b s))
     (when-let [^String s type-dim-val-gauge]
       (.withTypeDimValGauge b s))
     (when-let [^String s type-dim-val-counter-count]
       (.withTypeDimValCounterCount b s))
     (when-let [^String s type-dim-val-meter-count]
       (.withTypeDimValMeterCount b s))
     (when-let [^String s type-dim-val-histo-samples]
       (.withTypeDimValHistoSamples b s))
     (when-let [^String s type-dim-val-histo-stats]
       (.withTypeDimValHistoStats b s))
     (when-let [^String s type-dim-val-timer-samples]
       (.withTypeDimValTimerSamples b s))
     (when-let [^String s type-dim-val-timer-stats]
       (.withTypeDimValTimerStats b s))
     (when reporter-filter
       (let [^Predicate p (reify Predicate
                            (test [_ metric-datum]
                              (reporter-filter metric-datum)))]
         (.withReporterFilter b p)))
     (.build b))))

(defn start
  "Report all metrics to CloudWatch periodically."
  [^CloudWatchReporter r ^long seconds]
  (mrep/start r seconds))

(defn stop
  "Stops reporting."
  [^CloudWatchReporter r]
  (mrep/stop r))
