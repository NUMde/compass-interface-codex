/**
 * Set the 'GeccoTargetProfile' extension value of the generated Questionnaire item.
 * Is inherited to child items
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
annotation class FhirProfile(val url: String)

/**
 * Sets the item.text value of the generated Questionnaire item
 */
@Target(AnnotationTarget.PROPERTY)
annotation class Text(val text: String)

/**
 * Add enableWhen-Condition to item, value must be 'Yes' of the 'YesNoUnkown' value set
 */
@Target(AnnotationTarget.PROPERTY)
annotation class EnableWhenYes(val geccoId: String)

/**
 * Add enableWhen-Condition to item
 */
@Target(AnnotationTarget.PROPERTY)
annotation class EnableWhen(val geccoId: String, val system: String, val code: String)

/**
 * Add item control drop-down extension to generated questionnaire item (for long lists of answerOptions)
 */
@Target(AnnotationTarget.PROPERTY)
annotation class ComboBox

/**
 * Do not output element in generated questionnaire
 */
@Target(AnnotationTarget.PROPERTY)
annotation class Ignore

@Target(AnnotationTarget.PROPERTY)
annotation class SymptomEnum(val enum: Symptom)

@Target(AnnotationTarget.PROPERTY)
annotation class LabCRPEnum(val enum: LabCodes.CRP)

@Target(AnnotationTarget.PROPERTY)
annotation class AnaChronicNeurologicalOrMentalDisease(val enum: ChronicNeurologicalMentalDisease)

@Target(AnnotationTarget.PROPERTY)
annotation class LabFerritin(val enum: LabCodes.Ferritin)

@Target(AnnotationTarget.PROPERTY)
annotation class LabBilirubin(val enum: LabCodes.Bilirubin)

@Target(AnnotationTarget.PROPERTY)
annotation class LabLactateDehydrogenase(val enum: LabCodes.LactateDehydrogenase)

@Target(AnnotationTarget.PROPERTY)
annotation class LabCreatineMassPerVolume(val enum: LabCodes.Creatinine.MassPerVolume)

@Target(AnnotationTarget.PROPERTY)
annotation class LabCreatineMolesPerVolume(val enum: LabCodes.Creatinine.MolesPerVolume)

@Target(AnnotationTarget.PROPERTY)
annotation class LabLactateMassPerVolume(val enum: LabCodes.Lactate.MassPerVolume)

@Target(AnnotationTarget.PROPERTY)
annotation class LabLactateMolesPerVolume(val enum: LabCodes.Lactate.MolesPerVolume)

@Target(AnnotationTarget.PROPERTY)
annotation class LabLeukocytes(val enum: LabCodes.Leukocytes.CountPerVolume)

@Target(AnnotationTarget.PROPERTY)
annotation class LabLymphocytes(val enum: LabCodes.Lymphocytes.CountPerVolume)

@Target(AnnotationTarget.PROPERTY)
annotation class LabPartialThromboplastinTime(val enum: LabCodes.PartialThromboplastinTime)

@Target(AnnotationTarget.PROPERTY)
annotation class LabPlateletsCountPerVolume(val enum: LabCodes.Platelets.CountPerVolume)

@Target(AnnotationTarget.PROPERTY)
annotation class LabTroponinTCardiacMassPerVolume(val enum: LabCodes.Troponin.TCardiac.MassPerVolume)

@Target(AnnotationTarget.PROPERTY)
annotation class LabTroponinICardiacMassPerVolume(val enum: LabCodes.Troponin.ICardiac.MassPerVolume)

@Target(AnnotationTarget.PROPERTY)
annotation class LabHemoglobinMassPerVolume(val enum: LabCodes.Hemoglobin.MassPerVolume)

@Target(AnnotationTarget.PROPERTY)
annotation class LabHemoglobinMolesPerVolume(val enum: LabCodes.Hemoglobin.MolesPerVolume)

@Target(AnnotationTarget.PROPERTY)
annotation class LabAlbuminMassPerVolume(val enum: LabCodes.Albumin.MassPerVolume)

@Target(AnnotationTarget.PROPERTY)
annotation class LabAlbuminMolesPerVolume(val enum: LabCodes.Albumin.MolesPerVolume)

@Target(AnnotationTarget.PROPERTY)
annotation class LabAntithrombin(val enum: LabCodes.Antithrombin)

@Target(AnnotationTarget.PROPERTY)
annotation class LabInterleukin6(val enum: LabCodes.Interleukin6.MassPerVolume)

@Target(AnnotationTarget.PROPERTY)
annotation class LabFibrinogen(val enum: LabCodes.Fibrinogen.MassPerVolume)

@Target(AnnotationTarget.PROPERTY)
annotation class LabPPT(val enum: LabCodes.PartialThromboplastinTime)

@Target(AnnotationTarget.PROPERTY)
annotation class LabNeutrophils(val enum: LabCodes.Neutrophils.CountPerVolume)

@Target(AnnotationTarget.PROPERTY)
annotation class LabProcalcitonin(val enum: LabCodes.Procalcitonin)

@Target(AnnotationTarget.PROPERTY)
annotation class LabNatriureticPeptideB(val enum: LabCodes.NatriureticPeptideB)

@Target(AnnotationTarget.PROPERTY)
annotation class LabINR(val enum: LabCodes.INR)

@Target(AnnotationTarget.PROPERTY)
annotation class Quantity(val enum: LabCodes.INR)

@Target(AnnotationTarget.PROPERTY)
annotation class ChronicLungDiseaseEnum(val enum: ChronicLungDisease)

@Target(AnnotationTarget.PROPERTY)
annotation class CardiovascularDiseaseEnum(val enum: CardiovascularDiseases)

@Target(AnnotationTarget.PROPERTY)
annotation class ChronicLiverDiseaseEnum(val enum: ChronicLiverDiseases)

@Target(AnnotationTarget.PROPERTY)
annotation class RheumatologicalImmunologicalDiseaseEnum(val enum: RheumatologicalImmunologicalDiseases)