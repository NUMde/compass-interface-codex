# gecco-easy

Templates for building ETL pipelines to
the [FHIR GECCO profiles](https://simplifier.net/guide/germancoronaconsensusdataset-implementationguide/home).

## Usage examples

```kotlin
//Creates observation with the right meta.profile, code, value, status according to the GECCO specification. 
val observation = PregnancyStatus(patientRef, YesNoUnknown.YES, effective)

//Creates an anamnesis with the right meta.profile, category, code, verificationStatus according to the GECCO specification. 
val condition = AnaChronicLungDisease(patientRef, ChronicLungDisease.ASTHMA, YesNoUnkwown.NO, dateTimeOfDocumentation)
```

Please use your IDEs built-in autocompletion to see a list of all the available templates!