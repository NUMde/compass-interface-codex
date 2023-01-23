# GECCO Questionnaire

This project converts the internal metadata model (*Logical Model*) into a FHIR Questionnaire with specific extensions.
These extensions can be used to map the data back to the logical model and create a different set of resources of the
FHIR standard.

![](../docs/Overview%20LogicalModel.png)

## Logical Model

The LogicalModel ist defined as a set of Kotlin classes:

```kotlin
data class LogicalModel(
    @Text("Anamnese / Risikofaktoren")
    var anamnesis: Anamnesis = Anamnesis(),
    @Text("Bildgebung") @Ignore
    var imaging: Imaging = Imaging(),
    //[...]
)

data class Anamnesis(
    @Text("Leidet der/die Patient*in unter einer chronischen Lungenerkrankung?")
    var hasChronicLungDiseases: YesNoUnknown? = null,
    @Text("An welcher chronischen Lungenerkrankung leidet der/die Patient*in?") @EnableWhenYes("anamnesis.hasChronicLungDiseases")
    var chronicLungDiseases: AnamnesisChronicLungDiseases = AnamnesisChronicLungDiseases()
    //[...]
)

data class Demographics(
    @FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/sex-assigned-at-birth")
    @Text("Biologisches Geschlecht")
    var biologicalSex: BirthSex? = null,
    @FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/pregnancy-status")
    @Text("Liegt eine Schwangerschaft vor?") @EnableWhen(
        "demographics.biologicalSex",
        "http://hl7.org/fhir/administrative-gender",
        "female"
    )
    var pregnancyStatus: PregnancyStatus? = null,
)

enum class YesNoUnknown(override val coding: Coding) : CodeableEnum<YesNoUnknown> {
    YES(Coding("http://terminology.hl7.org/CodeSystem/v2-0136", "Y", "Ja")),
    NO(Coding("http://terminology.hl7.org/CodeSystem/v2-0136", "N", "Nein")),
    UNKNOWN(Coding("http://terminology.hl7.org/CodeSystem/data-absent-reason", "asked-unknown", "Unbekannt"))
}

enum class PregnancyStatus(override val codeableConcept: CodeableConcept) : ConceptEnum<PregnancyStatus> {
    NOT_PREGNANT(CodeableConcept().apply {
        coding = listOf(
            loinc("LA26683-5", "Not pregnant"),
            snomed("60001007", "Not pregnant (finding)")
        )
    }),
    // [...]
}
```

The model will be converted recursively to `Questionnaire.item` resources, complex items will be converted
to `item.type = "group"` elements.

* The `@Text()` annotation controls the `Questionnaire.item.text` attribute.
* The `@Ignore` attribute omits the resulting sub-part from the resulting Questionnaire
* The `@EnableWhenYes` annotation emits a `Questionnaire.item.enableWhen` component
  with `answerCoding = {system: http://terminology.hl7.org/CodeSystem/v2-0136, code: Y}`
* The `YesNoUnknown` enum entries will be rendered as `Questionnaire.item.answerOption`
* The `PregnancyStatus` enum will be rendered `Questionnaire.item.answerOption` with `answerOption.valueCoding` as the
  first Coding of the CodeableConcept
* The `@ComboBox` annotation will emit a `questionnaire-itemControl`=`drop-down` extension
* The `@FhirProfile` annotation will control the `GeccoTargetProfile` extension, which is also inherited to child
  elmenents.
* The recursive behaviour can be overwritten for specific datatypes by overwriting the `ClassToItemRenderer<T>`
  interface

```kotlin 
interface ClassToItemRenderer<T> {
    fun render(compassId: String, item: QItem)
    fun parse(geccoId: String, item: Map<String, QRAnswer>): T
}
```

## Usage

```kotlin
val renderers = listOf(/* [...] */)

val questionnaire = LogicalModel.toQuestionnaire(renderers)

val questionnaireResponse = generateResponse(questionnaire)

val logicalModel = toLogicalModel(questionnaire, questionnaireResponse, listOf(renderers))

val geccoBundle = logicalModelToGeccoProfile(logicalModel, patiendId, recordedDate, bundleBuilder)
```
