# fhir-ts

A modular FHIR terminology server decomposing the API from the provider services.

This is a library designed to be re-used. The client application can
create a FHIR server by composing different providers together either in code
or at the API gateway level.

### Design document

We decouple the mechanics of running the FHIR server itself and how each
resource is handled.

It should be possible to start a FHIR terminology server with a few lines
of code to publish any value-set. For example, that might be a small value set
defined in EDN, or in JSON, a parochial local set of codes within a namespace,
or a value set derived from the latest FHIR valueset json dataset, or driven by
a 'proper' terminology such as SNOMED CT or LOINC. The decoupling remains
the same, even if the implementations will be very different. For example,
it is entirely reasonable to serve the built-in FHIR valuesets from an in-memory
store.

The lines of responsibility are:

*server.clj* : Supports running a server and marshalling request data into 
simpler clojure immutable structures (maps, vectors etc)

*core.clj*  : Defines the protocols that decouple server from terminology 
provider.

*registry.clj* : Defines a function to build a handler that can be used to
provide arbitrary terminology services based on pattern matching.

*simple.clj* : A built-in provider that can serve a static value set
