(ns com.eldrix.fhir.ts.simple
  (:require [com.eldrix.fhir.ts.core :as ts]
            [clojure.tools.logging.readable :as log]))

(deftype SimpleCodeSystem [data]
  ts/CodeSystemService
  (lookup [_ request]
    (log/info "lookup" request)
    (get-in data []))
  (validate-code [this request])
  (subsumes [this request])
  (find-matches [this request])

  )