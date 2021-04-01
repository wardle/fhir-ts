(ns com.eldrix.fhir.ts.test)



(def fake-value-set
  {
   "fullUrl"  "http //hl7.org/fhir/CodeSystem/address-use",
   "resource" {
               "resourceType"  "CodeSystem",
               "id"            "address-use",
               "meta"          {
                                "lastUpdated" "2019-11-01T09 29 23.356+11 00"
                                },
               "extension"     [{
                                 "url"       "http //hl7.org/fhir/StructureDefinition/structuredefinition-wg",
                                 "valueCode" "fhir"
                                 },
                                {
                                 "url"       "http //hl7.org/fhir/StructureDefinition/structuredefinition-standards-status",
                                 "valueCode" "normative"
                                 },
                                {
                                 "url"       "http //hl7.org/fhir/StructureDefinition/structuredefinition-normative-version",
                                 "valueCode" "4.0.0"
                                 },
                                {
                                 "url"          "http //hl7.org/fhir/StructureDefinition/structuredefinition-fmm",
                                 "valueInteger" 5
                                 }],
               "url"           "http //hl7.org/fhir/address-use",
               "identifier"    [{
                                 "system" "urn:ietf:rfc:3986",
                                 "value"  "urn:oid:2.16.840.1.113883.4.642.4.68"
                                 }],
               "version"       "4.0.1",
               "name"          "AddressUse",
               "title"         "AddressUse",
               "status"        "active",
               "experimental"  false,
               "date"          "2019-11-01T09 29 23+11 00",
               "publisher"     "HL7 (FHIR Project)",
               "contact"       [{
                                 "telecom" [{
                                             "system" "url",
                                             "value"  "http //hl7.org/fhir"
                                             },
                                            {
                                             "system" "email",
                                             "value"  "fhir@lists.hl7.org"
                                             }]
                                 }],
               "description"   "The use of an address.",
               "caseSensitive" true,
               "valueSet"      "http //hl7.org/fhir/ValueSet/address-use",
               "content"       "complete",
               "concept"       [{
                                 "code"       "home",
                                 "display"    "Home",
                                 "definition" "A communication address at a home."
                                 },
                                {
                                 "code"       "work",
                                 "display"    "Work",
                                 "definition" "An office address. First choice for business related contacts during business hours."
                                 },
                                {
                                 "code"       "temp",
                                 "display"    "Temporary",
                                 "definition" "A temporary address. The period can provide more detailed information."
                                 },
                                {
                                 "code"       "old",
                                 "display"    "Old / Incorrect",
                                 "definition" "This address is no longer in use (or was never correct but retained for records)."
                                 },
                                {
                                 "code"       "billing",
                                 "display"    "Billing",
                                 "definition" "An address to be used to send bills, invoices, receipts etc."
                                 }]}})

