(ns com.eldrix.fhir.ts.core
  (:import (com.eldrix.fhir.ts.core OperationValueSet)))


(defprotocol CodeSystemService
  (lookup [this request]
    "Given a code/system, or a Coding, get additional details about the concept,
     including definition, status, designations, and properties. One of the
     products of this operation is a full decomposition of a code from a
     structured terminology.

     See https://www.hl7.org/fhir/codesystem-operation-lookup.html")

  (validate-code [this request]
    "Validate that a coded value is in the code system. If the operation is not
    called at the instance level, one of the parameters \"url\" or
    \"codeSystem\" must be provided. The operation returns a result (true /
    false), an error message, and the recommended display for the code.

    When invoking this operation, a client SHALL provide one (and only one) of
    the parameters (code+system, coding, or codeableConcept). Other parameters
    (including version and display) are optional.

    See https://www.hl7.org/fhir/codesystem-operation-validate-code.html")

  (subsumes [this request]
    "Test the subsumption relationship between code/Coding A and code/Coding B
    given the semantics of subsumption in the underlying code system (see
    hierarchyMeaning). When invoking this operation, a client SHALL provide
    both a and codes, either as code or Coding parameters.

    The system parameter is required unless the operation is invoked on an
    instance of a code system resource. Other parameters are optional.

    See https://www.hl7.org/fhir/codesystem-operation-subsumes.html")

  (find-matches [this request]
    "Given a set of properties (and text), return one or more possible matching
    codes. This operation takes a set of properties, and examines the code system
    looking for codes in the code system that match a set of known properties.

    See https://www.hl7.org/fhir/codesystem-operation-find-matches.html")
  )

(defprotocol ConceptMapService
  (translate [this request]
    "Translate a code from one value set to another, based on the existing value
    set and concept maps resources, and/or other additional knowledge available
    to the server.

    See https://www.hl7.org/fhir/conceptmap-operation-translate.html"))

(defprotocol ValueSetService
  (expand [this request]
    "Expand a value set; the definition of a value set is used to create a
    simple collection of codes suitable for use for data entry or validation.

    See https://www.hl7.org/fhir/valueset-operation-expand.html")

  (validate-code [this request]
    "Validate that a coded value is in the set of codes allowed by a value set.

    See https://www.hl7.org/fhir/valueset-operation-validate-code.html"))