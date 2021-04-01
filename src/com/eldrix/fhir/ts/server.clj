(ns com.eldrix.fhir.ts.server
  "Implementation of a FHIR terminology server.
  See https://hl7.org/fhir/terminology-service.html

  This code's responsibility is limited to marshalling data to and from the
  HAPI server implementation."
  (:gen-class)
  (:require [clojure.tools.logging.readable :as log]
            [com.eldrix.fhir.ts.core :as ts])
  (:import (ca.uhn.fhir.context FhirContext)
           (org.eclipse.jetty.servlet ServletContextHandler ServletHolder)
           (ca.uhn.fhir.rest.server RestfulServer IResourceProvider)
           (javax.servlet Servlet)
           (ca.uhn.fhir.rest.server.interceptor ResponseHighlighterInterceptor)
           (org.eclipse.jetty.server Server ServerConnector)
           (ca.uhn.fhir.rest.annotation OperationParam)
           (org.hl7.fhir.r4.model CodeSystem ValueSet ValueSet$ValueSetExpansionComponent Parameters$ParametersParameterComponent StringType BooleanType CodeType Coding Parameters)))


(defn- make-parameter-components
  [k v]
  (let [pc (Parameters$ParametersParameterComponent. (StringType. (name k)))]
    (cond
      (string? v)
      (.setValue pc (StringType. v))
      (number? v)
      (.setValue pc (StringType. (str v)))
      (boolean? v)
      (.setValue pc (BooleanType. ^Boolean v))
      (keyword? v)
      (.setValue pc (CodeType. (name v)))
      (and (map? v) (contains? v :code) (contains? v :system))
      (.setValue pc (Coding. (name (:system v)) (name (:code v)) (:display v)))
      (map? v)
      (let [parts (map (fn [[k2 v2]] (make-parameter-components k2 v2)) v)]
        (.setPart pc parts))
      (seqable? v)
      (let [parts (map (fn [m] (make-parameter-components k m)) v)]
        (.setPart pc parts)))))

(defn make-parameters
  "Turn a map into FHIR properties."
  [m]
  (when m
    (let [params (Parameters.)]
      (doseq [pc (map (fn [[k v]] (make-parameter-components k v)) m)]
        (.addParameter params pc))
      params)))

(definterface LookupCodeSystemOperation
  (^org.hl7.fhir.r4.model.Parameters lookup [^ca.uhn.fhir.rest.param.StringParam code
                                             ^ca.uhn.fhir.rest.param.UriParam system
                                             ^ca.uhn.fhir.rest.param.StringParam version
                                             ^ca.uhn.fhir.rest.param.TokenParam coding
                                             ^ca.uhn.fhir.rest.param.StringParam displayLanguage
                                             ^ca.uhn.fhir.rest.param.StringAndListParam property]))

(definterface SubsumesCodeSystemOperation
  (^org.hl7.fhir.r4.model.Parameters subsumes [^ca.uhn.fhir.rest.param.StringParam codeA
                                               ^ca.uhn.fhir.rest.param.StringParam codeB
                                               ^ca.uhn.fhir.rest.param.UriParam system
                                               ^ca.uhn.fhir.rest.param.StringParam version
                                               ^ca.uhn.fhir.rest.param.TokenParam codingA
                                               ^ca.uhn.fhir.rest.param.TokenParam codingB]))

(definterface ExpandValueSetOperation
  (^org.hl7.fhir.r4.model.ValueSet expand [^ca.uhn.fhir.rest.param.UriParam url
                                           ^ca.uhn.fhir.rest.param.UriParam context
                                           ^ca.uhn.fhir.rest.param.TokenParam contextDirection
                                           ^ca.uhn.fhir.rest.param.StringParam filter
                                           ^ca.uhn.fhir.rest.param.DateParam date
                                           ^ca.uhn.fhir.rest.param.NumberParam offset
                                           ^ca.uhn.fhir.rest.param.NumberParam count
                                           ^ca.uhn.fhir.rest.param.StringParam includeDesignations
                                           ^ca.uhn.fhir.rest.param.StringParam designation
                                           ^ca.uhn.fhir.rest.param.StringParam includeDefinition
                                           ^ca.uhn.fhir.rest.param.StringParam activeOnly
                                           ^ca.uhn.fhir.rest.param.StringParam excludeNested
                                           ^ca.uhn.fhir.rest.param.StringParam excludeNotForUI
                                           ^ca.uhn.fhir.rest.param.StringParam excludePostCoordinated
                                           ^ca.uhn.fhir.rest.param.TokenParam displayLanguage
                                           ]))

(deftype CodeSystemResourceProvider [handler]
  IResourceProvider
  (getResourceType [_this] CodeSystem)
  ;;;;;;;;;;;;;;;;;;;;;;;;;
  LookupCodeSystemOperation
  (^{:tag                                  org.hl7.fhir.r4.model.Parameters
     ca.uhn.fhir.rest.annotation.Operation {:name "lookup" :idempotent true}}
    lookup [_this
            ^{:tag ca.uhn.fhir.rest.param.StringParam OperationParam {:name "code"}} code
            ^{:tag ca.uhn.fhir.rest.param.UriParam OperationParam {:name "system"}} system
            ^{:tag ca.uhn.fhir.rest.param.StringParam OperationParam {:name "version"}} version
            ^{:tag ca.uhn.fhir.rest.param.TokenParam OperationParam {:name "coding"}} coding
            ^{:tag ca.uhn.fhir.rest.param.StringParam OperationParam {:name "displayLanguage"}} displayLanguage
            ^{:tag ca.uhn.fhir.rest.param.StringAndListParam OperationParam {:name "property"}} property]
    (log/debug "codesystem/$lookup: " {:code code :system system :version version :coding coding :display-language displayLanguage :properties property})
    (handler {:operation  :org.hl7.fhir.OperationDefinition/CodeSystem-lookup
              :parameters {:code code :system system :version version :coding coding :display-language displayLanguage :properties property}}))
  ;;;;;;;;;;;;;;;;;;;;;;;;;;;
  SubsumesCodeSystemOperation
  (^{:tag                                  org.hl7.fhir.r4.model.Parameters
     ca.uhn.fhir.rest.annotation.Operation {:name "subsumes" :idempotent true}}
    subsumes [_this
              ^{:tag ca.uhn.fhir.rest.param.StringParam OperationParam {:name "codeA"}} codeA
              ^{:tag ca.uhn.fhir.rest.param.StringParam OperationParam {:name "codeB"}} codeB
              ^{:tag ca.uhn.fhir.rest.param.UriParam OperationParam {:name "system"}} system
              ^{:tag ca.uhn.fhir.rest.param.StringParam OperationParam {:name "version"}} version
              ^{:tag ca.uhn.fhir.rest.param.TokenParam OperationParam {:name "codingA"}} codingA
              ^{:tag ca.uhn.fhir.rest.param.TokenParam OperationParam {:name "codingB"}} codingB]
    (log/debug "codesystem/$subsumes: " {:codeA codeA :codeB codeB :system system :version version :codingA codingA :codingB codingB})
    (handler {:operation  :org.hl7.fhir.OperationDefinition/CodeSystem-subsumes
              :parameters {:codeA codeA :codeB codeB :system system :version version :codingA codingA :codingB codingB}})))

(deftype ValueSetResourceProvider [handler]
  IResourceProvider
  (getResourceType [_this] ValueSet)
  ;;;;;;;;;;;;;;;;;;;;;;;
  ExpandValueSetOperation
  (^{:tag                                  org.hl7.fhir.r4.model.ValueSet
     ca.uhn.fhir.rest.annotation.Operation {:name "expand" :idempotent true}}
    expand [_this
            ^{:tag ca.uhn.fhir.rest.param.UriParam OperationParam {:name "url"}} url
            ^{:tag ca.uhn.fhir.rest.param.UriParam OperationParam {:name "context"}} context
            ^{:tag ca.uhn.fhir.rest.param.TokenParam OperationParam {:name "contextDirection"}} contextDirection
            ^{:tag ca.uhn.fhir.rest.param.StringParam OperationParam {:name "filter"}} param-filter
            ^{:tag ca.uhn.fhir.rest.param.DateParam OperationParam {:name "date"}} date
            ^{:tag ca.uhn.fhir.rest.param.NumberParam OperationParam {:name "offset"}} offset
            ^{:tag ca.uhn.fhir.rest.param.NumberParam OperationParam {:name "count"}} param-count
            ^{:tag ca.uhn.fhir.rest.param.StringParam OperationParam {:name "includeDesignations"}} includeDesignations
            ^{:tag ca.uhn.fhir.rest.param.StringParam OperationParam {:name "designation"}} designation
            ^{:tag ca.uhn.fhir.rest.param.StringParam OperationParam {:name "includeDefinition"}} includeDefinition
            ^{:tag ca.uhn.fhir.rest.param.StringParam OperationParam {:name "activeOnly"}} activeOnly
            ^{:tag ca.uhn.fhir.rest.param.StringParam OperationParam {:name "excludeNested"}} excludeNested
            ^{:tag ca.uhn.fhir.rest.param.StringParam OperationParam {:name "excludeNotForUI"}} excludeNotForUI
            ^{:tag ca.uhn.fhir.rest.param.StringParam OperationParam {:name "excludePostCoordinated"}} excludePostCoordinated
            ^{:tag ca.uhn.fhir.rest.param.TokenParam OperationParam {:name "displayLanguage"}} displayLanguage]
    (log/debug "valueset/$expand:" {:url url :filter param-filter :activeOnly activeOnly :displayLanguage displayLanguage})
    (handler {:operation  :org.hl7.fhir.OperationDefinition/ValueSet-expand
              :parameters {:url                     url
                           :context                 context :context-direction contextDirection
                           :filter                  param-filter :date date :offset offset
                           :count                   param-count
                           :include-designations    includeDesignations :designation designation
                           :include-definition      includeDefinition
                           :active-only             activeOnly :exclude-nested excludeNested
                           :exclude-not-for-ui      excludeNotForUI
                           :exclude-postcoordinated excludePostCoordinated
                           :display-language        displayLanguage}})))

(defn ^Servlet make-r4-servlet [handler]
  (proxy [RestfulServer] [(FhirContext/forR4)]
    (initialize []
      (log/info "Initialising HL7 FHIR R4 server; providers: CodeSystem ValueSet")
      (.setResourceProviders this [(CodeSystemResourceProvider. handler)
                                   (ValueSetResourceProvider. handler)])
      (log/debug "Resource providers:" (seq (.getResourceProviders this)))
      (.registerInterceptor this (ResponseHighlighterInterceptor.)))))

(defn ^Server make-server
  "Creates a FHIR terminology server.
  Parameters:
  - handler : a function that takes an operation and 'in' parameters and returns
              'out' parameters when possible.
  - port    : port on which to run server, default 8080"
  [{:keys [handler port]}]
  (let [servlet-holder (ServletHolder. ^Servlet (make-r4-servlet handler))
        handler (doto (ServletContextHandler. ServletContextHandler/SESSIONS)
                  (.setContextPath "/")
                  (.addServlet servlet-holder "/fhir/*"))
        server (doto (Server.)
                 (.setHandler handler))
        connector (doto (ServerConnector. server)
                    (.setPort (or port 8080)))]
    (.addConnector server connector)
    server))

(defn make-handler [])

(comment

  (def server (make-server {:handler handler :port 8080}))
  (.start server)
  (.stop server)

  (do
    (.stop server)
    (def server (make-server svc {:port 8080}))
    (.start server))

  (svc/search svc {:s "mnd"})
  (svc/getConcept svc 24700007)

  (svc/getPreferredSynonym svc 233753001 "en")
  (svc/getReleaseInformation svc)
  (keys (svc/getExtendedConcept svc 138875005))
  (get-in (svc/getExtendedConcept svc 24700007) [:parent-relationships com.eldrix.hermes.snomed/IsA])
  )
