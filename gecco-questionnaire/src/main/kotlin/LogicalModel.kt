import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.MedicationStatement
import java.util.*
import java.time.LocalDate

@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
annotation class FhirProfile(val url: String)

@Target(AnnotationTarget.PROPERTY)
annotation class Text(val text: String)

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
annotation class EnableWhenYes(val geccoId: String)

@Target(AnnotationTarget.PROPERTY)
annotation class EnableWhen(val geccoId: String, val system: String, val code: String)

@Target(AnnotationTarget.PROPERTY)
annotation class ComboBox()

@Target(AnnotationTarget.PROPERTY)
annotation class Ignore()


data class LogicalModel(
    @Text("Anamnese / Risikofaktoren")
    var anamnesis: Anamnesis = Anamnesis(),
    @Text("Bildgebung") @Ignore
    var imaging: Imaging = Imaging(),
    @Text("Demographie")
    var demographics: Demographics = Demographics(),
    @Text("Epidemiologische Faktoren")
    var epidemiologicalFactors: EpidemiologicalFactors = EpidemiologicalFactors(),
    @Text("Komplikationen")
    var complications: Complications = Complications(),
    @Text("Krankheitsbeginn / Aufnahme")
    var onsetOfIllnessOrAdmission: OnsetOfIllnessOrAdmission = OnsetOfIllnessOrAdmission(),
    @Text("Laborwerte") @Ignore
    var laboratoryValues: LaboratoryValues = LaboratoryValues(),
    @Text("Medikation") @Ignore
    var medication: Medication = Medication(),
    @Text("Outcome bei Entlassung")
    var outcomeAtDischarge: OutcomeAtDischarge = OutcomeAtDischarge(),
    @Text("Studieneinschluss/Einschlusskriterien")
    var studyEnrollmentOrInclusionCriteria: StudyEnrollmentOrInclusionCriteria = StudyEnrollmentOrInclusionCriteria(),
    @Text("Symptome")
    var symptoms: Symptoms = Symptoms(),
    @Text("Therapie")
    var therapy: Therapy = Therapy(),
    @Text("Vitalparameter")
    var vitalSigns: VitalSigns = VitalSigns()
)


data class Anamnesis(
    @Text("Leidet der/die Patient*in unter einer chronischen Lungenerkrankung?")
    var hasChronicLungDiseases: YesNoUnknown? = null,
    @Text("An welcher chronischen Lungenerkrankung leidet der/die Patient*in?") @EnableWhenYes("anamnesis.hasChronicLungDiseases")
    var chronicLungDiseases: AnamnesisChronicLungDiseases = AnamnesisChronicLungDiseases(),

    @Text("Leidet der/die Patient*in unter einer Herz-Kreislauf-Erkrankung?")
    var hasCardiovascularDiseases: YesNoUnknown? = null,
    @Text("An welcher Herz-Kreislauf-Erkrankung leidet der/die Patient*in?") @EnableWhenYes("anamnesis.hasCardiovascularDiseases")
    var cardiovascularDiseases: AnamnesisCardiovascularDiseases = AnamnesisCardiovascularDiseases(),

    @Text("Leidet der/die Patient*in an einer chronischen Lebererkrankung?")
    var hasChronicLiverDiseases: YesNoUnknown? = null,
    @Text("An welcher chronischen Lebererkrankung leidet der/die Patient*in?") @EnableWhenYes("anamnesis.hasChronicLiverDiseases")
    var chronicLiverDiseases: AnamnesisChronicLiverDiseases = AnamnesisChronicLiverDiseases(),

    @Text("Leidet der/die Patient*in unter mind. einer rheumatologischen/immunologischen Erkrankung?")
    var hasRheumatologicalImmunologicalDiseases: YesNoUnknown? = null,
    @Text("An welcher rheumatologischen/immunologischen Erkrankung leidet der/die Patient*in?")  @EnableWhenYes("anamnesis.hasRheumatologicalImmunologicalDiseases")
    var rheumatologicalImmunologicalDiseases: AnamnesisRheumatologicalImmunologicalDiseases = AnamnesisRheumatologicalImmunologicalDiseases(),

    @FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/human-immunodeficiency-virus-infection")
    @Text("Ist der/die Patient*in HIV-infiziert?")
    var hasHivInfection: YesNoUnknown? = null,

    @Text("Ist der/die Patient*in organtransplantiert?")
    var hasHistoryOfBeingATissueOrOrganRecipient: YesNoUnknown? = null,
    @Text("Welche Transplantation wurde durchgeführt?") @EnableWhenYes("anamnesis.hasHistoryOfBeingATissueOrOrganRecipient")
    var historyOfBeingATissueOrOrganRecipient: AnamnesisHistoryOfBeingATissueOrOrganRecipient = AnamnesisHistoryOfBeingATissueOrOrganRecipient(),

    @Text("Leidet der/die Patient*in an Diabetes?")
    var hasDiabetesMellitus: YesNoUnknown? = null,
    @Text("An welchem Typ Diabetes leidet der/die Patient*in?") @EnableWhenYes("anamnesis.hasDiabetesMellitus")
    var diabetesMellitus: Diabetes? = null,

    @Text("Leidet der/die Patient*in unter mind. einer aktiven Tumor-/Krebserkrankung?")
    var malignantNeoplasticDiseases: CancerStatus? = null,

    @FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/smoking-status")
    @Text("Hat der/die Patient*in jemals Zigaretten geraucht?")
    var tobaccoSmokingStatus: SmokingStatus? = null,

    @Text("Leidet der/die Patient*in unter mind. einer chronischen neurologischen oder psychatrischen Erkrankung?")
    var hasChronicNeurologicalOrMentalDiseases: YesNoUnknown? = null,
    @Text("An welcher chronischen neurologischen oder psychatrischen Erkrankung leidet der/die Patient*in?") @EnableWhenYes("anamnesis.hasChronicNeurologicalOrMentalDiseases")
    var chronicNeurologicalOrMentalDiseases: AnamnesisChronicNeurologicalOrMentalDiseases = AnamnesisChronicNeurologicalOrMentalDiseases(),

    @FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/respiratory-therapies")
    @Text("Bestand bereits vor der aktuellen Erkrankung eine Sauerstoff- oder Beatmungstherapie?")
    var hasHadOxygenOrRespiratoryTherapyBeforeCurrentIllness: YesNoUnknown? = null,

    @Text("Leidet der/die Patient*in an einer chronischen Nierenerkrankung?")
    @FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/chronic-kidney-diseases")
    var chronicKidneyDisease: ChronicKidneyDisease? = null,

    @Text("Reiseaktivität in den letzten 14 Tagen?")
    var hasTravelled: YesNoUnknown? = null,
    @Text("In welche Länder reiste der Patient?") @EnableWhenYes("anamnesis.hasTravelled")
    var historyOfTravel: AnamnesisHistoryOfTravel? = AnamnesisHistoryOfTravel(),
    //war vorher default null

    @FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/gastrointestinal-ulcers")
    @Text("Leidet der/die Patient*in an Magengeschwüren?")
    var hasGastrointestinalUclers: YesNoUnknown? = null,

    @Text("Welche Impfungen hat der Patient bereits erhalten?")
    var immunizationStatus: AnamnesisImmunizationStatus = AnamnesisImmunizationStatus(),
    @FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/do-not-resuscitate-order")
    @Text("Falls Informationen zum DNR-Status vorliegen: Möchte der/die Patient*innen wiederbelebt werden?")
    var resuscitateOrder: Resuscitation? = null
)

@FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/chronic-lung-diseases")
data class AnamnesisChronicLungDiseases(
    @Text("Asthma")
    var asthma: YesNoUnknown? = null,
    @Text("COPD")
    var copd: YesNoUnknown? = null,
    @Text("Lungenfibrose")
    var fibrosis: YesNoUnknown? = null,
    @Text("Lungenhochdruck/pulmonale Hypertonie")
    var pulmonaryHypertension: YesNoUnknown? = null,
    @Text("OHS")
    var ohs: YesNoUnknown? = null,
    @Text("Schlafapnoe")
    var sleepApnea: YesNoUnknown? = null,
    @Text("OSAS")
    var osas: YesNoUnknown? = null,
    @Text("Cystische Fibrose")
    var cysticFibrosis: YesNoUnknown? = null
)

@FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/cardiovascular-diseases")
data class AnamnesisCardiovascularDiseases(
    @Text("Bluthochdruck")
    var arterialHyptertension: YesNoUnknown? = null,
    @Text("Zustand nach Herzinfarkt")
    var stateAfterHeartAttack: YesNoUnknown? = null,
    @Text("Herzrhythmusstörungen")
    var cardiacArrhytmia: YesNoUnknown? = null,
    @Text("Herzinsuffizienz")
    var heartFailure: YesNoUnknown? = null,
    @Text("pAVK")
    var peripherialArterialOcclusiveDisease: YesNoUnknown? = null,
    @Text("Zustand nach Revaskularisation")
    var stateAfterRevascularization: YesNoUnknown? = null,
    @Text("Koronare Herzerkrankung (KHK)")
    var coronaryArteriosclerosis: YesNoUnknown? = null,
    @Text("Carotisstenose")
    var carotidArteryStenosis: YesNoUnknown? = null,
)

@FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/chronic-liver-diseases")
data class AnamnesisChronicLiverDiseases(
    @Text("Fettleber")
    var steatosisOfLiver: YesNoUnknown? = null,
    @Text("Leberzirrhose")
    var cirrhosisOfLiver: YesNoUnknown? = null,
    @Text("Chronische infektiöse Hepatitis")
    var chronicViralHepatitis: YesNoUnknown? = null,
    @Text("Autoimmune Lebererkrankungen")
    var autoimmuneLiverDisease: YesNoUnknown? = null,
)

@FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/rheumatological-immunological-diseases")
data class AnamnesisRheumatologicalImmunologicalDiseases(
    @Text("Chronisch entzündl. Darmerkrankung")
    var inflammatoryBowelDisease: YesNoUnknown? = null,
    @Text("Rheumatoide Arthritis")
    var rheumatoidArthritis: YesNoUnknown? = null,
    @Text("Kollagenosen")
    var collagenosis: YesNoUnknown? = null,
    @Text("Vaskulitiden")
    var vasculitis: YesNoUnknown? = null,
    @Text("angeborene Immundefekte")
    var congenitalImmunodeficiencyDisease: YesNoUnknown? = null
)

@FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/organ-recipient")
data class AnamnesisHistoryOfBeingATissueOrOrganRecipient(
    @Text("Herz")
    var entireHeart: YesNoUnknown? = null,
    @Text("Lunge")
    var entireLung: YesNoUnknown? = null,
    @Text("Leber")
    var entireLiver: YesNoUnknown? = null,
    @Text("Niere")
    var entireKidney: YesNoUnknown? = null,
    @Text("Bauchspeicheldrüse")
    var entirePancreas: YesNoUnknown? = null,
    @Text("Darmstruktur")
    var intestinalStructure: YesNoUnknown? = null,
    @Text("gesamter Dünndarm")
    var entireSmallIntestine: YesNoUnknown? = null,
    @Text("gesamter Dickdarm")
    var entireLargeIntestine: YesNoUnknown? = null,
    @Text("Haut")
    var skinPart: YesNoUnknown? = null,
    @Text("Hornhaut")
    var entireCornea: YesNoUnknown? = null,
    @Text("Gehörknöchelchen")
    var earOssicleStructure: YesNoUnknown? = null,
    @Text("Herzklappen")
    var entireHeartValve: YesNoUnknown? = null,
    @Text("Blutgefäß")
    var bloodVesselPart: YesNoUnknown? = null,
    @Text("Hirnhaut")
    var cerebralMeningitisStructure: YesNoUnknown? = null,
    @Text("Knochengewebe")
    var boneTissueOrStructure: YesNoUnknown? = null,
    @Text("Knorpelgewebe")
    var cartilageTissue: YesNoUnknown? = null,
    @Text("Sehne")
    var tendonStructure: YesNoUnknown? = null
)

//@FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/diabetes-mellitus")
//data class AnamnesisDiabetesMellitus(
//    var diabetesMellitusType1: YesNoUnknown? = null,
//    var diabetesMellitusType2: YesNoUnknown? = null,
//    var insulinTreatedType2DiabetesMellitus: YesNoUnknown? = null,
//    var secondaryDiabetesMellitus: YesNoUnknown? = null
//)

@FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/malignant-neoplastic-disease")
data class AnamnesisMalignantNeoplasticDiseases(
    @Text("Aktiv")
    var activeTumor: YesNoUnknown? = null,
    @Text("in Remission")
    var remissionTumor: YesNoUnknown? = null
)


@FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/chronic-neurological-mental-diseases")
data class AnamnesisChronicNeurologicalOrMentalDiseases(
//    @Text("Chronische neurologische Erkrankung")
//    var chronicNervousSystemDisorder: YesNoUnknown? = null,
//    @Text("Psychiatrische Erkrankung")
//    var mentalDisorder: YesNoUnknown? = null,
    @Text("Angsterkrankung") @AnaChronicNeurologicalOrMentalDisease(ChronicNeurologicalMentalDisease.ANXIETY_DISORDER)
    var anxietyDisorder: YesNoUnknown? = null,
    @Text("Depression") @AnaChronicNeurologicalOrMentalDisease(ChronicNeurologicalMentalDisease.DEPRESSIVE_DISORDER)
    var depressiveDisorder: YesNoUnknown? = null,
    @Text("Psychose")  @AnaChronicNeurologicalOrMentalDisease(ChronicNeurologicalMentalDisease.PSYCHOTIC_DISORDER)
    var psychoticDisorder: YesNoUnknown? = null,
    @Text("M. Parkinson")  @AnaChronicNeurologicalOrMentalDisease(ChronicNeurologicalMentalDisease.PARKINSONS_DISEASE)
    var parkinsonDisorder: YesNoUnknown? = null,
    @Text("Demenz") @AnaChronicNeurologicalOrMentalDisease(ChronicNeurologicalMentalDisease.DEMENTIA)
    var dementia: YesNoUnknown? = null,
    @Text("Multiple Sklerose") @AnaChronicNeurologicalOrMentalDisease(ChronicNeurologicalMentalDisease.MULTIPLE_SCLEROSIS)
    var multipleSclerosis: YesNoUnknown? = null,
    @Text("Neuromuskuläre Erkrankungen") @AnaChronicNeurologicalOrMentalDisease(ChronicNeurologicalMentalDisease.COMBINED_DISORDER_OF_MUSCLE_AND_PERIPHERAL_NERVE)
    var combinedDisorderOfMuscleAndPeripheralNerve: YesNoUnknown? = null,
    @Text("Epilepsie") @AnaChronicNeurologicalOrMentalDisease(ChronicNeurologicalMentalDisease.EPILEPSY)
    var epilepsy: YesNoUnknown? = null,
    @Text("Migräne") @AnaChronicNeurologicalOrMentalDisease(ChronicNeurologicalMentalDisease.MIGRAINE)
    var migraine: YesNoUnknown? = null,
    @Text("Z.n. Apoplex mit Residuen") @AnaChronicNeurologicalOrMentalDisease(ChronicNeurologicalMentalDisease.HISTORY_OF_CEREBROVASCULAR_ACCIDENT_WITH_RESIDUAL_DEFICIT)
    var historyOfCerebrovascularAccidentWithResidualDeficit: YesNoUnknown? = null,
    @Text("Z.n. Apoplex ohne Residuen") @AnaChronicNeurologicalOrMentalDisease(ChronicNeurologicalMentalDisease.HISTORY_OF_CEREBROVASCULAR_ACCIDENT_WITHOUT_RESIDUAL_DEFICITS)
    var historyOfCerebrovascularAccidentWithoutResidualDeficits: YesNoUnknown? = null
)

@FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/history-of-travel")
data class AnamnesisHistoryOfTravel(
    @Text("Von")
    var from: LocalDate? = null,
    @Text("Bis")
    var till: LocalDate? = null,
    @Text("Land") @ComboBox
    var country: Countries? = null,
    @Text("Bundesland") @ComboBox @EnableWhen("anamnesis.historyOfTravel.country", "urn:iso:std:iso:3166", "DE")
    var federalState: FederalStates? = null,
    @Text("Stadt")
    var city: String? = null
)

@FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/immunization")
data class AnamnesisImmunizationStatus(
    @Text("Influenza")
    var influenza: YesNoUnknownWithDate? = null,
    @Text("Pneumokokken")
    var pneumococcal: YesNoUnknownWithDate? = null,
    @Text("BCG")
    var bcg: YesNoUnknownWithDate? = null,
    @Text("Covid-19: 1. Impfung")
    var covid19_first: Covid19Vaccination? = Covid19Vaccination(),
    @Text("Covid-19: 2. Impfung") @EnableWhenYes("anamnesis.immunizationStatus.covid19_first.status")
    var covid19_second: Covid19Vaccination? = Covid19Vaccination(),
    @Text("Covid-19: 3. Impfung") @EnableWhenYes("anamnesis.immunizationStatus.covid19_second.status")
    var covid19_third: Covid19Vaccination? = Covid19Vaccination()
)

data class Covid19Vaccination(
    @Text("Status")
    var status: YesNoUnknown? = null,
    @Text("Zeitpunkt der Impfung")
    var date: LocalDate? = null,
    @Text("Impfstoff")
    var vaccine: Covid19Vaccine? = null
)

data class Imaging(
    @Text("Wurden Bildgebungsverfahren der Lunge im Rahmen von Covid-19 durchgeführt (CT,Röntgen,Ultrashall)? ")
    var hasHadImagingProcedures: YesNoUnknown? = null,
    @Text("Welche Bildgebungsverfahren wurden durchgeführt?") @EnableWhenYes("imaging.hasHadImagingProcedures")
    var imagingProcedures: ImagingProcedures? = ImagingProcedures(),

    @Text("Liegt ein Befund bildgebender Verfahren im Rahmen von Covid-19 vor?")
    var hasRadiologicalFindings: YesNoUnknown? = null,
    @FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/diagnostic-report-radiology")
    @Text("Befund bildgebender Verfahren im Rahmen von Covid-19 (CT,Röntgen,Ultrashall)") @EnableWhenYes("imaging.hasRadiologicalFindings")
    var radiologicalFindings: RadiologicFindings? = null
)

@FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/radiology-procedures")
data class ImagingProcedures(
    @Text("Computertomographie")
    var computedTomography: YesNoUnknown? = null,
    @Text("Röntgen")
    var radiographicImaging: YesNoUnknown? = null,
    @Text("Ultraschall")
    var ultrasound: YesNoUnknown? = null
)


data class Demographics(
    @FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/sex-assigned-at-birth")
    @Text("Biologisches Geschlecht")
    var biologicalSex: BirthSex? = null,
    @FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/pregnancy-status")
    @Text("Liegt eine Schwangerschaft vor?") @EnableWhen("demographics.biologicalSex", "http://hl7.org/fhir/administrative-gender", "female")
    var pregnancyStatus: PregnancyStatus? = null,
    @FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/ethnic-group")
    @Text("ethnische Zugehörigkeit")
    var ethnicGroup: EthnicGroup? = null,
    @FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/Patient")
    @Text("Geburtsdatum")
    var dateOfBirth: LocalDate? = null,
    @FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/Patient")
    @Text("Alter bei Studieneinschluss in Jahren")
    var ageInYears: Int? = null, //TODO Berechne die anderen Werte von dem einen
    @FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/Patient")
    @Text("Alter bei Studieneinschluss in Monaten")
    var ageInMonth: Int? = null, //TODO
    @FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/frailty-score")
    @Text("Frailty-Score vor Aufnahme")
    var frailityScore: FrailityScore? = null,
    @FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/body-weight")
    @Text("Körpergewicht in kg")
    var bodyWeight: Float? = null,
    @FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/body-height")
    @Text("Körpergröße in cm")
    var bodyHeight: Float? = null,
)


data class EpidemiologicalFactors(
    @FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/known-exposure")
    @Text("Hatte der/die Patient*in in den letzten 14 Tagen vor Beginn seiner/ihrer Beschwerden wissentlich Kontakt mit einer wahrscheinlich oder nachgewiesenermaßen an COVID-19 erkrankten Person?")
    var knownCovid19Exposure: YesNoUnknown? = null,
)


@FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/complications-covid-19")
data class Complications(
    @Text("Thrombembolische Ereignisse")
    var hasHadThromboembolicComplications: YesNoUnknown? = null,
    @Text("Welche?") @EnableWhenYes("complications.hasHadThromboembolicComplications")
    var thromboembolicComplications: ThromboembolicComplications = ThromboembolicComplications(),
    @Text("Infektion der Lunge")
    var infectiousDiseaseOfLung: YesNoUnknown? = null,
    @Text("Blutstrominfektion")
    var infectiousAgentInBloodstream: YesNoUnknown? = null,
    @Text("Akutes Nierenversagen")
    var acuteRenalFailureSyndrome: YesNoUnknown? = null
    //TODO ComplicationsCovid19ICD-ValueSet
)

data class ThromboembolicComplications(
    @Text("Embolie")
    var embolism: YesNoUnknown? = null,
    @Text("Thrombose")
    var thrombosis: YesNoUnknown? = null,
    @Text("Venöse Thrombose")
    var venousThrombosis: YesNoUnknown? = null,
    @Text("Lungenarterienembolie")
    var pulmonaryEmbolism: YesNoUnknown? = null,
    @Text("Stroke")
    var cerebrovascularAccident: YesNoUnknown? = null,
    @Text("Myokardinfarkt")
    var myocardialInfarction: YesNoUnknown? = null,
)

data class YesNoUnknownWithIntent(
    @Text("Wurde das Medikament verabreicht?")
    val administration: YesNoUnknown? = null,
    @Text("Mit welcher therapheutischen Absicht das Medikament verarbreicht?")
    val intent: TherapeuticIntent? = null
)

data class OnsetOfIllnessOrAdmission(
    @Text("Erkrankungsphase zum Zeitpunkt der COVID-19 Diagnose")
    var stageAtDiagnosis: StageAtDiagnosis? = null,
    @Text("Datum der Aufnahme")
    var dateTimeOfAdmission: LocalDate? = null //TODO: Add in extraction
)


data class LaboratoryValues(
    var laboratoryValues: LaboratoryValuesLaboratoryValue = LaboratoryValuesLaboratoryValue(),
    @FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/sars-cov-2-rt-pcr")
    var sarsCov2RtPcrResult: YesNoUnknown? = null,
    @FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/sars-cov-2-ab-pnl-ser-pl-ia")
    var sarsCov2AntibodiesResult: YesNoUnknown? = null,
)

@FhirProfile("https://www.medizininformatik-initiative.de/fhir/core/modul-labor/StructureDefinition/ObservationLab")
data class LaboratoryValuesLaboratoryValue(
    @LabCRPEnum(LabCodes.CRP.MASS_PER_VOLUME_IN_CAPILLARY_BLOOD)
    var crpMassPerVolumeInCapillaryBlood: Float? = null,
    @LabCRPEnum(LabCodes.CRP.MASS_PER_VOLUME_IN_BLOOD_HIGH_SENSITIVITY)
    var crpMassPerVolumeInBloodByHighSensitivityMethod: Float? = null,
    @LabCRPEnum(LabCodes.CRP.MOLES_PER_VOLUME_IN_SERUM_OR_PLASMA_HIGH_SENSITIVITY)
    var crpMolesPerVolumeInSerumOrPlasmaByHighSensitivityMethod: Float? = null,
    @LabCRPEnum(LabCodes.CRP.MASS_PER_VOLUME_IN_SERUM_OR_PLASMA_HIGH_SENSITIVITY)
    var crpMassPerVolumeInSerumOrPlasmaByHighSensitivityMethod: Float? = null,
    @LabCRPEnum(LabCodes.CRP.MASS_PER_VOLUME_IN_SERUM_OR_PLASMA)
    var crpMassPerVolumeInSerumOrPlasma: Float? = null,
    @LabCRPEnum(LabCodes.CRP.MOLES_PER_VOLUME_IN_SERUM_OR_PLASMA)
    var crpMolesPerVolumeInSerumOrPlasma: Float? = null,

    @LabFerritin(LabCodes.Ferritin.MASS_PER_VOLUME_IN_SERUM_OR_PLASMA_BY_IMMUNOASSAY)
    var ferritinMassPerVolumeInSerumOrPlasmaByImmunoassay: Float? = null,
    @LabFerritin(LabCodes.Ferritin.GOAL_SERUM_OR_PLASMA)
    var ferritinGoalMassPerVolumeInSerumOrPlasma: Float? = null,
    @LabFerritin(LabCodes.Ferritin.IN_SERUM_OR_PLASMA)
    var ferritinMolesPerVolumeInSerumOrPlasma: Float? = null,
    @LabFerritin(LabCodes.Ferritin.IN_SERUM_OR_PLASMA_MASS)
    var ferritinMassPerVolumeInSerumOrPlasma: Float? = null,
    @LabFerritin(LabCodes.Ferritin.IN_BLOOD_MASS)
    var ferritinMassPerVolumeInBlood: Float? = null,

    @LabBilirubin(LabCodes.Bilirubin.INDIRECT_MASS_PER_VOLUME_IN_SERUM_OR_PLASMA)
    var bilirubinIndirectMassPerVolumeInSerumOrPlasma: Float? = null,
    @LabBilirubin(LabCodes.Bilirubin.TOTAL_MASS_PER_VOLUME_IN_SERUM_OR_PLASMA)
    var bilirubinTotalMassPerVolumeInSerumOrPlasma: Float? = null,
    @LabBilirubin(LabCodes.Bilirubin.TOTAL_MOLES_PER_VOLUME_IN_SERUM_OR_PLASMA)
    var bilirubinTotalMolesPerVolumeInSerumOrPlasma: Float? = null,
    @LabBilirubin(LabCodes.Bilirubin.TOTAL_MASS_PER_VOLUME_IN_VENOUS_BLOOD)
    var bilirubinTotalMassPerVolumeInVenousBlood: Float? = null,
    var bilirubinTotalMolesPerVolumeInArterialBlood: Float? = null,
    var bilirubinTotalMolesPerVolumeInBlood: Float? = null,

    var fibrinDDimerDduMassPerVolumeInPlateletPoorPlasma: Float? = null,
    var fibrinDDimerFeuMassPerVolumeInPlateletPoorPlasma: Float? = null,
    var fibrinDDimerFeuMassPerVolumeInPlateletPoorPlasmaByImmunoassay: Float? = null,
    var shortFibrinDDimerFeuAndDduPanelPlateletPoorPlasma: Float? = null,
    var fibrinDDimerFeuMassPerVolumeInBloodByImmunoassay: Float? = null,
    var fibrinDDimerUnitsPerVolumeInPlateletPoorPlasma: Float? = null,
    var fibrinDDimerDduMassPerVolumeInBloodByImmunoassay: Float? = null,
    var fibrinDDimerTiterInPlateletPoorPlasma: Float? = null,
    var fibrinDDimerUnitsPerVolumeInPlateletPoorPlasmaByImmunoassay: Float? = null,
    var fibrinDDimerDduMassPerVolumeInPlateletPoorPlasmaByImmunoassay: Float? = null,

    var gammaGlutamylTransferaseToAspartateAminotransferaseEnzymaticActivityRatioInSerumOrPlasma: Float? = null,
    var gammaGlutamylTransferaseEnzymaticActivityPerVolumeInSerumOrPlasma: Float? = null,

    var aspartateAminotransferaseEnzymaticActivityPerVolumeInSerumOrPlasma: Float? = null,
    var aspartateAminotransferaseEnzymaticActivityPerVolumeInSerumOrPlasmaWithP5P: Float? = null,
    var aspartateAminotransferaseEnzymaticActivityPerVolumeInSerumOrPlasmaByNoAdditionOfP5P: Float? = null,

    @LabLactateDehydrogenase(LabCodes.LactateDehydrogenase.ENZYMATIC_ACTIVITY_PER_VOLUME_IN_SERUM_OR_PLASMA)
    var lactateDehydrogenaseEnzymaticActivityPerVolumeInSerumOrPlasma: Float? = null,
    @LabLactateDehydrogenase(LabCodes.LactateDehydrogenase.ENZYMATIC_ACTIVITY_PER_VOLUME_IN_SERUM_OR_PLASMA_BY_PYRUVATE_TO_LACTATE_REACTION)
    var lactateDehydrogenaseEnzymaticActivityPerVolumeInSerumOrPlasmaByPyruvateToLactateReaction: Float? = null,
    @LabLactateDehydrogenase(LabCodes.LactateDehydrogenase.ENZYMATIC_ACTIVITY_PER_VOLUME_IN_SERUM_OR_PLASMA_BY_LACTATE_TO_PYRUVATE_REACTION)
    var lactateDehydrogenaseEnzymaticActivityPerVolumeInSerumOrPlasmaByLactateToPyruvateReaction: Float? = null,
    @LabLactateDehydrogenase(LabCodes.LactateDehydrogenase.ENZYMATIC_ACTIVITY_PER_VOLUME_IN_BODY_FLUID_BY_LACTATE_TO_PYRUVATE_REACTION)
    var lactateDehydrogenaseEnzymaticActivityPerVolumeInBodyFluidByLactateToPyruvateReaction: Float? = null,
    @LabLactateDehydrogenase(LabCodes.LactateDehydrogenase.ENZYMATIC_ACTIVITY_PER_VOLUME_IN_BODY_FLUID_BY_PYRUVATE_TO_LACTATE_REACTION)
    var lactateDehydrogenaseEnzymaticActivityPerVolumeInBodyFluidByPyruvateToLactateReaction: Float? = null,

    @LabTroponinICardiacMassPerVolume(LabCodes.Troponin.ICardiac.MassPerVolume.IN_SERUM_OR_PLASMA_BY_DETECTION_LIMIT_LOWER_THAN_0_01_ng_PER_mL)
    var troponinICardiacMassPerVolumeInSerumOrPlasmaByDetectionLimitLessOrEqualToOneHundredthNgPerMl: Float? = null,
    @LabTroponinICardiacMassPerVolume(LabCodes.Troponin.ICardiac.MassPerVolume.IN_SERUM_OR_PLASMA_BY_HIGH_SENSITIVITY_METHOD)
    var troponinICardiacMassPerVolumeInSerumOrPlasmaByHighSensitivityMethod: Float? = null,
    @LabTroponinTCardiacMassPerVolume(LabCodes.Troponin.TCardiac.MassPerVolume.IN_SERUM_OR_PLASMA_BY_HIGH_SENSITIVITY_METHOD)
    var troponinTCardiacMassPerVolumeInSerumOrPlasmaByHighSensitivityMethod: Float? = null,
    @LabTroponinTCardiacMassPerVolume(LabCodes.Troponin.TCardiac.MassPerVolume.IN_SERUM_OR_PLASMA)
    var troponinTCardiacMassPerVolumeInSerumOrPlasma: Float? = null,
    @LabTroponinTCardiacMassPerVolume(LabCodes.Troponin.TCardiac.MassPerVolume.IN_VENOUS_BLOOD)
    var troponinTCardiacMassPerVolumeInVenousBlood: Float? = null,
    @LabTroponinTCardiacMassPerVolume(LabCodes.Troponin.TCardiac.MassPerVolume.IN_BLOOD)
    var troponinTCardiacMassPerVolumeInBlood: Float? = null,
    @LabTroponinICardiacMassPerVolume(LabCodes.Troponin.ICardiac.MassPerVolume.IN_BLOOD)
    var troponinICardiacMassPerVolumeInBlood: Float? = null,
    @LabTroponinICardiacMassPerVolume(LabCodes.Troponin.ICardiac.MassPerVolume.IN_SERUM_OR_PLASMA)
    var troponinLCardiacMassPerVolumeInSerumOrPlasma: Float? = null,

    @LabHemoglobinMassPerVolume(LabCodes.Hemoglobin.MassPerVolume.IN_BLOOD_BY_OXIMETRY)
    var hemoglobinMassPerVolumeInVenousBloodByOximetry: Float? = null,
    @LabHemoglobinMolesPerVolume(LabCodes.Hemoglobin.MolesPerVolume.IN_BLOOD)
    var hemoglobinMolesPerVolumeInBlood: Float? = null,
    @LabHemoglobinMassPerVolume(LabCodes.Hemoglobin.MassPerVolume.IN_BLOOD)
    var hemoglobinMassPerVolumeInBlood: Float? = null,
    @LabHemoglobinMassPerVolume(LabCodes.Hemoglobin.MassPerVolume.IN_BLOOD_BY_CALCULATION)
    var hemoglobinMassPerVolumeInBloodByCalculation: Float? = null,
    @LabHemoglobinMassPerVolume(LabCodes.Hemoglobin.MassPerVolume.IN_BLOOD_BY_OXIMETRY)
    var hemoglobinMassPerVolumeInBloodByOximetry: Float? = null,
    @LabHemoglobinMassPerVolume(LabCodes.Hemoglobin.MassPerVolume.IN_ARTERIAL_BLOOD)
    var hemoglobinMolesPerVolumeInArterialBlood: Float? = null,
    @LabHemoglobinMassPerVolume(LabCodes.Hemoglobin.MassPerVolume.IN_ARTERIAL_BLOOD_BY_OXIMETRY)
    var hemoglobinMassPerVolumeInArterialBloodByOximetry: Float? = null,
    @LabHemoglobinMassPerVolume(LabCodes.Hemoglobin.MassPerVolume.IN_ARTERIAL_BLOOD)
    var hemoglobinMassPerVolumeInArterialBlood: Float? = null,
    @LabHemoglobinMassPerVolume(LabCodes.Hemoglobin.MassPerVolume.IN_VENOUS_BLOOD)
    var hemoglobinMassPerVolumeInVenousBlood: Float? = null,
    @LabHemoglobinMassPerVolume(LabCodes.Hemoglobin.MassPerVolume.IN_MIXED_VENOUS_BLOOD)
    var hemoglobinMassPerVolumeInMixedVenousBlood: Float? = null,
    @LabHemoglobinMassPerVolume(LabCodes.Hemoglobin.MassPerVolume.IN_CAPILLARY_BLOOD)
    var hemoglobinMassPerVolumeInCapillaryBlood: Float? = null,
    @LabHemoglobinMassPerVolume(LabCodes.Hemoglobin.MassPerVolume.IN_MIXED_VENOUS_BLOOD_BY_OXIMETRY)
    var hemoglobinMassPerVolumeInMixedVenousBloodByOximetry: Float? = null,

    @LabCreatineMassPerVolume(LabCodes.Creatinine.MassPerVolume.IN_ARTERIAL_BLOOD)
    var creatinineMassPerVolumeInArterialBlood: Float? = null,
    @LabCreatineMassPerVolume(LabCodes.Creatinine.MassPerVolume.IN_BLOOD)
    var creatinineMassPerVolumeInBlood: Float? = null,
//    @LabCreatineMolesPerVolume(LabCodes.Creatinine.MolesPerVolume.)
//    var creatinineMolesPerVolumeInArterialBlood: Float? = null,
    @LabCreatineMolesPerVolume(LabCodes.Creatinine.MolesPerVolume.IN_BLOOD)
    var creatinineMolesPerVolumeInBlood: Float? = null,
    @LabCreatineMassPerVolume(LabCodes.Creatinine.MassPerVolume.IN_BODY_FLUID)
    var creatinineMassPerVolumeInBodyFluid: Float? = null,
    @LabCreatineMolesPerVolume(LabCodes.Creatinine.MolesPerVolume.IN_BODY_FLUID)
    var creatinineMolesPerVolumeInBodyFluid: Float? = null,
    @LabCreatineMassPerVolume(LabCodes.Creatinine.MassPerVolume.IN_SERUM_OR_PLASMA)
    var creatinineMassPerVolumeInSerumOrPlasma: Float? = null,
    @LabCreatineMolesPerVolume(LabCodes.Creatinine.MolesPerVolume.IN_SERUM_OR_PLASMA)
    var creatinineMolesPerVolumeInSerumOrPlasma: Float? = null,

    @LabLactateMassPerVolume(LabCodes.Lactate.MassPerVolume.IN_SERUM_OR_PLASMA)
    var lactateMassPerVolumeInSerumOrPlasma: Float? = null,
    @LabLactateMassPerVolume(LabCodes.Lactate.MassPerVolume.IN_BLOOD)
    var lactateMassPerVolumeInBlood: Float? = null,
    @LabLactateMolesPerVolume(LabCodes.Lactate.MolesPerVolume.IN_BLOOD)
    var lactateMolesPerVolumeInBlood: Float? = null,
    @LabLactateMolesPerVolume(LabCodes.Lactate.MolesPerVolume.IN_CEREBRAL_SPINE_FLUID)
    var lactateMolesPerVolumeInCerebralSpinalFluid: Float? = null,
    @LabLactateMolesPerVolume(LabCodes.Lactate.MolesPerVolume.IN_CAPILLARY_BLOOD)
    var lactateMolesPerVolumeInCapillaryBlood: Float? = null,
    @LabLactateMolesPerVolume(LabCodes.Lactate.MolesPerVolume.IN_MIXED_VENOUS_BLOOD)
    var lactateMolesPerVolumeInMixedVenousBlood: Float? = null,
    @LabLactateMolesPerVolume(LabCodes.Lactate.MolesPerVolume.IN_ARTERIAL_BLOOD)
    var lactateMolesPerVolumeInArterialBlood: Float? = null,
    @LabLactateMolesPerVolume(LabCodes.Lactate.MolesPerVolume.IN_VENOUS_BLOOD)
    var lactateMolesPerVolumeInVenousBlood: Float? = null,
    @LabLactateMolesPerVolume(LabCodes.Lactate.MolesPerVolume.IN_SERUM_OR_PLASMA)
    var lactateMolesPerVolumeInSerumOrPlasma: Float? = null,
    @LabLactateMassPerVolume(LabCodes.Lactate.MassPerVolume.IN_ARTERIAL_BLOOD)
    var lactateMassPerVolumeInArterialBlood: Float? = null,
    @LabLactateMassPerVolume(LabCodes.Lactate.MassPerVolume.IN_CEREBRAL_SPINE_FLUID)
    var lactateMassPerVolumeInCerebralSpinalFluid: Float? = null,

    @LabLeukocytes(LabCodes.Leukocytes.CountPerVolume.IN_BLOOD_BY_ESTIMATE)
    var leukocytesCountPerVolumeInBloodByEstimate: Float? = null,
    @LabLeukocytes(LabCodes.Leukocytes.CountPerVolume.IN_BLOOD_BY_AUTOMATED_COUNT)
    var leukocytesCountPerVolumeInBloodByAutomatedCount: Float? = null,
    @LabLeukocytes(LabCodes.Leukocytes.CountPerVolume.IN_BLOOD_BY_MANUAL_COUNT)
    var leukocytesCountPerVolumeInBloodByManualCount: Float? = null,
    @LabLeukocytes(LabCodes.Leukocytes.CountPerVolume.IN_BLOOD)
    var leukocytesCountPerVolumeInBlood: Float? = null,

    @LabLymphocytes(LabCodes.Lymphocytes.CountPerVolume.IN_BLOOD_BY_MANUAL_COUNT)
    var lymphocytesCountPerVolumeInBloodByManualCount: Float? = null,
    @LabLymphocytes(LabCodes.Lymphocytes.CountPerVolume.IN_BLOOD_BY_AUTOMATED_COUNT)
    var lymphocytesCountPerVolumeInBloodByAutomatedCount: Float? = null,
    @LabLymphocytes(LabCodes.Lymphocytes.CountPerVolume.IN_BLOOD_BY_FLOW_CYTOMETRY)
    var lymphocytesCountPerVolumeInBloodByFlowCytometry: Float? = null,
    @LabLymphocytes(LabCodes.Lymphocytes.CountPerVolume.IN_BLOOD)
    var lymphocytesCountPerVolumeInBlood: Float? = null,

    @LabNeutrophils(LabCodes.Neutrophils.CountPerVolume.IN_BLOOD_BY_MANUAL_COUNT)
    var neutrophilsCountPerVolumeInBloodByManualCount: Float? = null,
    @LabNeutrophils(LabCodes.Neutrophils.CountPerVolume.IN_BLOOD_BY_AUTOMATED_COUNT)
    var neutrophilsCountPerVolumeInBloodByAutomatedCount: Float? = null,
    @LabNeutrophils(LabCodes.Neutrophils.CountPerVolume.IN_BLOOD)
    var neutrophilsCountPerVolumeInBlood: Float? = null,

    @LabPPT(LabCodes.PartialThromboplastinTime.IN_BLOOD_BY_COAGULATION_SALINE)
    var aPttInBloodByCoagulationOneToOneSaline: Float? = null,
    @LabPPT(LabCodes.PartialThromboplastinTime.IN_PLATELET_POOR_PLASMA_BY_COAGULATION_SALINE)
    var aPttInPlateletPoorPlasmaByCoagulationOneToOneSaline: Float? = null,
    @LabPPT(LabCodes.PartialThromboplastinTime.IN_PLATELET_POOR_PLASMA_BY_COAGULATION_SALINE)
    var aPttInPlateletPoorPlasmaByCoagulationAssay: Float? = null,
    @LabPPT(LabCodes.PartialThromboplastinTime.IN_BLOOD_BY_COAGULATION_ASSAY)
    var aPttInPlasmaByCoagulationAssay: Float? = null,

    @LabPlateletsCountPerVolume(LabCodes.Platelets.CountPerVolume.IN_PLATELET_RICH_PLASMA_BY_AUTOMATED_COUNT)
    var plateletsCountPerVolumeInPlateletRichPlasmaByAutomatedCount: Float? = null,
    @LabPlateletsCountPerVolume(LabCodes.Platelets.CountPerVolume.IN_PLASMA_BY_AUTOMATED_COUNT)
    var plateletsCountPerVolumeInPlasmaByAutomatedCount: Float? = null,
    @LabPlateletsCountPerVolume(LabCodes.Platelets.CountPerVolume.IN_PLASMA)
    var plateletsCountPerVolumeInPlasma: Float? = null,
    @LabPlateletsCountPerVolume(LabCodes.Platelets.CountPerVolume.IN_CAPILLARY_BLOOD_BY_MANUAL_COUNT)
    var plateletsCountPerVolumeInCapillaryBloodByManualCount: Float? = null,
    @LabPlateletsCountPerVolume(LabCodes.Platelets.CountPerVolume.IN_BLOOD_BY_MANUAL_COUNT)
    var plateletsCountPerVolumeInBloodByManualCount: Float? = null,
    @LabPlateletsCountPerVolume(LabCodes.Platelets.CountPerVolume.IN_BLOOD_BY_ESTIMATE)
    var plateletsCountPerVolumeInBloodByEstimate: Float? = null,
    @LabPlateletsCountPerVolume(LabCodes.Platelets.CountPerVolume.IN_BLOOD_BY_AUTOMATED_COUNT)
    var plateletsCountPerVolumeInBloodByAutomatedCount: Float? = null,
    @LabPlateletsCountPerVolume(LabCodes.Platelets.CountPerVolume.IN_BLOOD)
    var plateletsCountPerVolumeInBlood: Float? = null,

    @LabINR(LabCodes.INR.IN_PLATELET_POOR_PLASMA_BY_COAGULATION_ASSAY)
    var inrInPlateletPoorPlasmaByCoagulationAssay: Float? = null,
    @LabINR(LabCodes.INR.IN_CAPILLARY_BLOOD_BY_COAGULATION_ASSAY)
    var inrInCapillaryBloodByCoagulationAssay: Float? = null,
    @LabINR(LabCodes.INR.IN_BLOOD_BY_COAGULATION_ASSAY)
    var inrInBloodByCoagulationAssay: Float? = null,

    @LabAlbuminMassPerVolume(LabCodes.Albumin.MassPerVolume.IN_BLOOD_BY_BCP)
    var albuminMassPerVolumeInBloodByBromocresolPurpleDyeBindingMethod: Float? = null,
    @LabAlbuminMolesPerVolume(LabCodes.Albumin.MolesPerVolume.IN_SERUM_OR_PLASMA_BY_BCG)
    var albuminMolesPerVolumeInSerumOrPlasmaByBromocresolGreenDyeBindingMethod: Float? = null,
    @LabAlbuminMolesPerVolume(LabCodes.Albumin.MolesPerVolume.IN_SERUM_OR_PLASMA_BY_BCP)
    var albuminMolesPerVolumeInSerumOrPlasmaByBromocresolPurpleDyeBindingMethod: Float? = null,
    @LabAlbuminMassPerVolume(LabCodes.Albumin.MassPerVolume.IN_SERUM_OR_PLASMA_BY_BCP)
    var albuminMassPerVolumeInSerumOrPlasmaByBromocresolPurpleDyeBindingMethod: Float? = null,
    @LabAlbuminMassPerVolume(LabCodes.Albumin.MassPerVolume.IN_SERUM_OR_PLASMA_BY_BCG)
    var albuminMassPerVolumeInSerumOrPlasmaByBromocresolGreenDyeBindingMethod: Float? = null,
    @LabAlbuminMassPerVolume(LabCodes.Albumin.MassPerVolume.IN_SERUM_OR_PLASMA_BY_ELECTROPHORESIS)
    var albuminMassPerVolumeInSerumOrPlasmaByElectrophoresis: Float? = null,
    @LabAlbuminMolesPerVolume(LabCodes.Albumin.MolesPerVolume.IN_SERUM_OR_PLASMA)
    var albuminMolesPerVolumeInSerumOrPlasma: Float? = null,
    @LabAlbuminMassPerVolume(LabCodes.Albumin.MassPerVolume.IN_SERUM_OR_PLASMA)
    var albuminMassPerVolumeInSerumOrPlasma: Float? = null,

    @LabAntithrombin(LabCodes.Antithrombin.MOLES_PER_VOLUME_IN_PLATELET_POOR_PLASMA_BY_CHROMOGENIC_METHOD)
    var antithrombinMolesPerVolumeInPlateletPoorPlasmaByChromogenicMethod: Float? = null,
    @LabAntithrombin(LabCodes.Antithrombin.BY_CHROMO_NO_ADDITION_OF_HEPARIN)
    var antithrombinInPlateletPoorPlasmaByChromoNoAdditionOfHeparin: Float? = null,
    @LabAntithrombin(LabCodes.Antithrombin.UNITS_PER_VOLUME_IN_PLATELET_POOR_PLASMA_BY_CHROMOGENIC_METHOD)
    var antithrombinUnitsPerVolumeInPlateletPoorPlasmaByChromogenicMethod: Float? = null,
    @LabAntithrombin(LabCodes.Antithrombin.ACTUAL_IN_PLATELET_POOR_PLASMA_BY_CHROMOGENIC_METHOD)
    var antithrombinActualToNormalRatioInPlateletPoorPlasmaByChromogenicMethod: Float? = null,
    @LabAntithrombin(LabCodes.Antithrombin.AG_ACTUAL_IN_PLATELET_POOR_PLASMA_BY_IMMUNOASSAY)
    var antithrombinAgActualToNormalRatioInPlateletPoorPlasmaByImmunoassay: Float? = null,

    @LabProcalcitonin(LabCodes.Procalcitonin.IN_SERUM_OR_PLASMA_BY_IMMUNOASSAY)
    var procalcitoninMassPerVolumeInSerumOrPlasmaByImmunoassay: Float? = null,
    @LabProcalcitonin(LabCodes.Procalcitonin.IN_SERUM_OR_PLASMA)
    var procalcitoninMassPerVolumeInSerumOrPlasma: Float? = null,

    //TODO
    var interleukin6PresenceInSerumOrPlasma: YesNoUnknown? = null,

    @LabInterleukin6(LabCodes.Interleukin6.MassPerVolume.IN_SERUM_OR_PLASMA)
    var interleukin6MassPerVolumeInSerumOrPlasma: Float? = null,
    @LabInterleukin6(LabCodes.Interleukin6.MassPerVolume.IN_CEREBRAL_SPINE_FLUID)
    var interleukin6MassPerVolumeInCerebralSpinalFluid: Float? = null,
    @LabInterleukin6(LabCodes.Interleukin6.MassPerVolume.IN_BODY_FLUID)
    var interleukin6MassPerVolumeInBodyFluid: Float? = null,

    @LabNatriureticPeptideB(LabCodes.NatriureticPeptideB.IN_SERUM_OR_PLASMA)
    var natriureticPeptideBProhormoneNTerminalMassPerVolumeInSerumOrPlasma: Float? = null,

    //TODO
    var fibrinogenPeresenceInPlateletPoorPlasma: YesNoUnknown? = null,

    @LabFibrinogen(LabCodes.Fibrinogen.MassPerVolume.IN_PLATELET_POOR_PLASMA_BY_HEAT_DENATURATION)
    var fibrinogenMassPerVolumeInPlateletPoorPlasmaByHeatDenaturation: Float? = null,
    @LabFibrinogen(LabCodes.Fibrinogen.MassPerVolume.IN_PLATELET_POOR_PLASMA_BY_COAGULATION_DERIVED)
    var fibrinogenMassPerVolumeInPlateletPoorPlasmaByCoagulationDerived: Float? = null,
    @LabFibrinogen(LabCodes.Fibrinogen.MassPerVolume.IN_PLATELET_POOR_PLASMA_BY_COAGULATION_ASSAY)
    var fibrinogenMassPerVolumeInPlateletPoorPlasmaByCoagulationAssay: Float? = null,
)

data class Medication(
    @Text("Medikamentöse Therapie bei Covid19-Erkrankung?")
    var hadCovid19Therapy: YesNoUnknown? = null,
    @Text("Medikamentöse Therapie bei Covid19-Erkrankung")  @EnableWhenYes("medication.hadCovid19Therapy")
    var covid19Therapy: MedicationCovid19Therapy = MedicationCovid19Therapy(),

    @Text("ACE-Hemmer")
    @FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/pharmacological-therapy-ace-inhibitors")
    var aceInhibitors: ACEInhibitorAdministration? = null,

    @Text("Immunglobuline?")
    var immunoglobulins: YesNoUnknown? = null,

    @Text("Medikamentöse Therapie mit Antikoagulantien?")
    var hadAnticoagulation: YesNoUnknown? = null,
    @Text("Medikamentöse Therapie mit Antikoagulantien") @EnableWhenYes("medication.hadAnticoagulation")
    var anticoagulation: MedicationAnticoagulation = MedicationAnticoagulation()

)

@FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/pharmacological-therapy")
data class MedicationCovid19Therapy(
    //Todo Text für productContainingSteroid
    var productContainingSteroid: YesNoUnknown? = null,
    @Text("Atazanavir")
    var productContainingAtazanavir: YesNoUnknown? = null,
    @Text("Darunavir")
    var productContainingDarunavir: YesNoUnknown? = null,
    @Text("Chloroquine phosphate")
    var productContainingChloroquine: YesNoUnknown? = null,
    @Text("Hydroxychloroquine")
    var productContainingHydroxychloroquine: YesNoUnknown? = null,
    @Text("Ivermectin")
    var productContainingIvermectin: YesNoUnknown? = null,
    @Text("Lopinavir/Ritonavir")
    var productContainingLopinavirAndRitonavir: YesNoUnknown? = null,
    @Text("Ganciclovir")
    var productContainingGanciclovir: YesNoUnknown? = null,
    @Text("Oseltamivir")
    var productContainingOseltamivir: YesNoUnknown? = null,
    @Text("Remdesivir")
    var productContainingRemdesivir: YesNoUnknown? = null,
    @Text("Ribavirin")
    var productContainingRibavirin: YesNoUnknown? = null,
    @Text("Tocilizumab")
    var productContainingTocilizumab: YesNoUnknown? = null,
    @Text("Sarilumab")
    var productContainingSarilumab: YesNoUnknown? = null,
    @Text("CNI or mTor inhibitors (e.g. cyclosporin A, tacrolimus, sirolimus, everolimus)")
    var productContainingCalcineurinInhibitor: YesNoUnknown? = null,
    @Text("Anti-TNF-alpha inhibitors (e.g. adalimumab, etanercept)")
    var productContainingTumorNecrosisFactorAlphaInhibitor: YesNoUnknown? = null,
    @Text("Il1-receptor antangonists")
    var productContainingInterleukin1ReceptorAntagonist: YesNoUnknown? = null,
    @Text("Ruxolitinib")
    var productContainingRuxolitinib: YesNoUnknown? = null,
    @Text("Colchicine")
    var productContainingColchicine: YesNoUnknown? = null,
    @Text("Interferone (any)")
    var productContainingInterferon: YesNoUnknown? = null,
    @Text("Calcifediol")
    var productContainingCalcifediol: YesNoUnknown? = null,
    @Text("Antipyretic")
    var productContainingAntipyretic: YesNoUnknown? = null,
    @Text("Camostat")
    var productContainingCamostat: YesNoUnknown? = null,
    @Text("Favipiravir")
    var productContainingFavipiravir: YesNoUnknown? = null,
    @Text("Convalescent plasma")
    var productContainingPlasma: YesNoUnknown? = null,
    @Text("Zink")
    var productContainingZinc: YesNoUnknown? = null,
    @Text("Steroids (> 0.5 mg/kg prednisone equivalents)")
    var steroidsGtHalfMgPerKgPrednisoneEquivalents: YesNoUnknown? = null,
    @Text("Steroids (<= 0.5 mg/kg prednisone equivalents)")
    var streoidsLtHalfMgPerKgPrednisoneEquivalents: YesNoUnknown? = null
)

enum class ACEInhibitorAdministration(override val coding: Coding, val status: MedicationStatement.MedicationStatementStatus): CodeableEnum<ACEInhibitorAdministration> {
    YES(Coding("http://hl7.org/fhir/CodeSystem/medication-statement-status", "active", "Active"), MedicationStatement.MedicationStatementStatus.ACTIVE),
    NO(Coding("http://hl7.org/fhir/CodeSystem/medication-statement-status", "not-taken", "Not Taken"), MedicationStatement.MedicationStatementStatus.NOTTAKEN),
    UNKNOWN(Coding("http://hl7.org/fhir/CodeSystem/medication-statement-status", "unknown", "Unknown"), MedicationStatement.MedicationStatementStatus.UNKNOWN),
    STOPPED(Coding("http://hl7.org/fhir/CodeSystem/medication-statement-status", "stopped", "Stopped"), MedicationStatement.MedicationStatementStatus.STOPPED)
}


@FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/pharmacological-therapy-anticoagulants")
data class MedicationAnticoagulation(
//    Todo: Text for intents
//    var adjunctIntent: YesNoUnknownWithIntent? = null,
//    var adjuvantIntent: YesNoUnknownWithIntent? = null,
//    var curativeIntent: YesNoUnknownWithIntent? = null,
//    var neoAdjuvantIntent: YesNoUnknownWithIntent? = null,
//    var prophylaxixIntent: YesNoUnknownWithIntent? = null,
//    var supportiveIntent: YesNoUnknownWithIntent? = null,

    //Todo: Text for  heparinGroup
    var heparinGroup: YesNoUnknownWithIntent? = null,
    @Text("Unfraktioniertes Heparin")
    var heparin: YesNoUnknownWithIntent? = null,
    @Text("Antothrombin III oder Antithrombin alpha")
    var antiThrombin3OrAntithrombinAlpha: YesNoUnknownWithIntent? = null,
    @Text("Dalteparin")
    var dalteparin: YesNoUnknownWithIntent? = null,
    @Text("Enoxaparin-Natrium")
    var enoxaparin: YesNoUnknownWithIntent? = null,
    @Text("Nadroparin-Calcium")
    var nadroparin: YesNoUnknownWithIntent? = null,
    @Text("Parnaparin")
    var parnaparin: YesNoUnknownWithIntent? = null,
    @Text("Reviparin")
    var reviparin: YesNoUnknownWithIntent? = null,
    @Text("Danaparoid")
    var danaparoid: YesNoUnknownWithIntent? = null,
    @Text("Tinzaparin ")
    var tinzaparin: YesNoUnknownWithIntent? = null,
    @Text("Sulodexide")
    var sulodexid: YesNoUnknownWithIntent? = null,
    @Text("Bemiparin-Natrium")
    var bemiparin: YesNoUnknownWithIntent? = null,
    @Text("Certoparin ")
    var certoparin: YesNoUnknownWithIntent? = null,
    @Text("Heparinkombinationen")
    var heparinCombinations: YesNoUnknownWithIntent? = null,
    @Text("Certoparinkombinationen")
    var certoparinCombinations: YesNoUnknownWithIntent? = null,
    @Text("Plättchenaggregationshemmer")
    var thrombocyteAggregationInhibitorExclHeparin: YesNoUnknownWithIntent? = null,
    @Text("Ditazol")
    var ditazol: YesNoUnknownWithIntent? = null,
    @Text("Cloricromen")
    var cloricromen: YesNoUnknownWithIntent? = null,
    @Text("Picotamid")
    var picotamid: YesNoUnknownWithIntent? = null,
    @Text("Clopidogrel")
    var clopidogrel: YesNoUnknownWithIntent? = null,
    @Text("Ticlopidin")
    var ticlopidin: YesNoUnknownWithIntent? = null,
    @Text("Acetylsalicylsäure")
    var acetylsalicylicAcid: YesNoUnknownWithIntent? = null,
    @Text("Dipyridamol ")
    var dipyridamol: YesNoUnknownWithIntent? = null,
    @Text("Carbasalat-Calcium")
    var carbasalatCalcium: YesNoUnknownWithIntent? = null,
    @Text("Epoprostenol")
    var epoprostenol: YesNoUnknownWithIntent? = null,
    @Text("Indobufen")
    var indobufen: YesNoUnknownWithIntent? = null,
    @Text("Iloprost")
    var iloprost: YesNoUnknownWithIntent? = null,
    @Text("Sulfinpyrazon")
    var sulfinpyrazon: YesNoUnknownWithIntent? = null,
    @Text("Abciximab")
    var abciximab: YesNoUnknownWithIntent? = null,
    @Text("Aloxiprin")
    var aloxiprin: YesNoUnknownWithIntent? = null,
    @Text("Eptifibatid")
    var eptifibatid: YesNoUnknownWithIntent? = null,
    @Text("Tirofiban")
    var tirofiban: YesNoUnknownWithIntent? = null,
    @Text("Triflusal")
    var triflusal: YesNoUnknownWithIntent? = null,
    @Text("Beraprost")
    var beraprost: YesNoUnknownWithIntent? = null,
    @Text("Treprostinil")
    var treprostinil: YesNoUnknownWithIntent? = null,
    @Text("Prasugrel")
    var prasugrel: YesNoUnknownWithIntent? = null,
    @Text("Cilostazol")
    var cilostazol: YesNoUnknownWithIntent? = null,
    @Text("Ticagrelor")
    var ticagrelor: YesNoUnknownWithIntent? = null,
    @Text("Cangrelor")
    var cangrelor: YesNoUnknownWithIntent? = null,
    @Text("Vorapaxar")
    var vorapaxar: YesNoUnknownWithIntent? = null,
    @Text("Selexipag")
    var selexipag: YesNoUnknownWithIntent? = null,
    //Todo: Text für combinations
    @Text("Kombinationen")
    var combinations: YesNoUnknownWithIntent? = null,
    @Text("Clopidogrel und Acetylsalicylsäure")
    var clopidogrelAndAcetylsalicylicAcid: YesNoUnknownWithIntent? = null,
    @Text("Dipyridamol und Acetylsalicylsäure")
    var dipyridamolAndAcetylsalicylicAcid: YesNoUnknownWithIntent? = null,
    @Text("Acetylsalicylsäurekombinationen mit Protonenpumpenhemmern")
    var acetylsalicylicAcidCombinationsWithProtonpumpInhibitors: YesNoUnknownWithIntent? = null,
    @Text("Acetylsalicylsäurekombinationen mit Esomeprazol")
    var acetylsalicylicAcidCombinationsWithEsomeprazol: YesNoUnknownWithIntent? = null,
    @Text("Phenprocoumon")
    var phenprocoumon: YesNoUnknownWithIntent? = null,


    @Text("Faktor-Xa-Inhibitor")
    var directFactorXaInhibitors: YesNoUnknownWithIntent? = null,
    @Text("Rivaroxaban")
    var rivaroxaban: YesNoUnknownWithIntent? = null,
    @Text("Apixaban")
    var apixaban: YesNoUnknownWithIntent? = null,
    @Text("Edoxaban")
    var edoxaban: YesNoUnknownWithIntent? = null,
    @Text("Betrixaban")
    var betrixaban: YesNoUnknownWithIntent? = null,
    @Text("direkter Thrombininhibitor")
    var directThrombininIhibitors: YesNoUnknownWithIntent? = null,
    @Text("Hirudin")
    var desirudin: YesNoUnknownWithIntent? = null,
    @Text("Lepirudin ")
    var lepirudin: YesNoUnknownWithIntent? = null,
    @Text("Argatroban")
    var argatroban: YesNoUnknownWithIntent? = null,
    @Text("Melagatran")
    var melagatran: YesNoUnknownWithIntent? = null,
    @Text("Ximelagatran")
    var ximelagatran: YesNoUnknownWithIntent? = null,
    @Text("Bivalirudin")
    var bivalirudin: YesNoUnknownWithIntent? = null,
    @Text("Dabigatranetexilat")
    var dabigatranetexilat: YesNoUnknownWithIntent? = null,
)

data class OutcomeAtDischarge(
    @FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/dependence-on-ventilator")
    @Text("Beatmet?")
    var respiratoryOutcomeisVentilated: YesNoUnknown? = null,
    @FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/discharge-disposition")
    @Text("Zeitpunkt der Entlassung")
    var dateOfDischarge: LocalDate? = null, //TODO add in extraction logic
    @FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/discharge-disposition")
    @Text("Entlassungsart")
    var typeOfDischarge: TypeOfDischarge? = null,
    @FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/sars-cov-2-rt-pcr")
    @Text("Ergebnis des Folgeabstrich?")
    var followupSwapResultIsPositive: DetectedNotDetectedInconclusive? = null
)

data class StudyEnrollmentOrInclusionCriteria(
    @FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/study-inclusion-covid-19")
    @Text("Bestätigte Covid-19-Diagnose als Hauptursache für Aufnahme in Studie")
    var enrolledWithCovid19DiagnosisAsMainReason: YesNoUnknownOtherNa? = null,
    @FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/interventional-clinical-trial-participation")
    @Text("Hat der Patient an einer oder mehreren interventionellen Klinischen Studie teilgenommen?")
    var hasPatientParticipatedInOneOrMoreInterventionalClinicalTrials: YesNoUnknownOtherNa? = null
)


@FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/ValueSet/sars-cov-2-symptoms")
data class Symptoms(
    @Text("Bauchschmerzen")
    @SymptomEnum(Symptom.ABDOMINAL_PAIN)
    var abdominalPain: YesNoUnknownWithSymptomSeverity? = null,
    @Text("Asymptomatisch")
    @SymptomEnum(Symptom.ASYMPTOMATIC)
    var asymptomatic: YesNoUnknown? = null,
    @Text("Blutung")
    @SymptomEnum(Symptom.BLEEDING)
    var bleeding: YesNoUnknownWithSymptomSeverity? = null,
    @Text("Brustschmerzen")
    @SymptomEnum(Symptom.CHEST_PAIN)
    var chestPain: YesNoUnknownWithSymptomSeverity? = null,
    @Text("Schüttelfrost")
    @SymptomEnum(Symptom.CHILL)
    var chill: YesNoUnknownWithSymptomSeverity? = null,
    @Text("Konjunktivitis")
    @SymptomEnum(Symptom.CONJUNCTIVITIS)
    var conjunctivis: YesNoUnknownWithSymptomSeverity? = null,
    @Text("Husten")
    @SymptomEnum(Symptom.COUGH)
    var cough: YesNoUnknownWithSymptomSeverity? = null,
    @Text("Durchfall")
    @SymptomEnum(Symptom.DIARRHEA)
    var diarrhea: YesNoUnknownWithSymptomSeverity? = null,
    @Text("Bewusstseinsstörung")
    @SymptomEnum(Symptom.DISTURBANCE_OF_CONSCIOUSNESS)
    var disturbanceOfConsciousness: YesNoUnknownWithSymptomSeverity? = null,
    @Text("Atemnot")
    @SymptomEnum(Symptom.DYSPNEA)
    var dyspnea: YesNoUnknownWithSymptomSeverity? = null,
    @Text("Hautausschlag")
    @SymptomEnum(Symptom.ERUPTION_OF_SKIN)
    var erruptionOfSkin: YesNoUnknownWithSymptomSeverity? = null,
    @Text("Müdigkeit")
    @SymptomEnum(Symptom.FATIGUE)
    var fatigue: YesNoUnknownWithSymptomSeverity? = null,
    @Text("Fieberigkeit")
    @SymptomEnum(Symptom.FEELING_FEVERISH)
    var feelingFeverish: YesNoUnknownWithSymptomSeverity? = null,
    @Text("Fieber")
    @SymptomEnum(Symptom.FEELING_FEVERISH)
    var fever: YesNoUnknownWithSymptomSeverity? = null,
    @Text("Kopfschmerzen")
    @SymptomEnum(Symptom.HEADACHE)
    var headache: YesNoUnknownWithSymptomSeverity? = null,
    @Text("Bluthusten")
    @SymptomEnum(Symptom.HEMOPTYSIS)
    var hemoptysis: YesNoUnknownWithSymptomSeverity? = null,
    @Text("Hauteinziehungen des Brustkorbs bei der Einatmung")
    @SymptomEnum(Symptom.INDRAWING_OF_RIBS_DURING_RESPIRATION)
    var indrawingOfRibsDuringRespiration: YesNoUnknownWithSymptomSeverity? = null,
    @Text("Gelenkschmerz")
    @SymptomEnum(Symptom.JOINT_PAIN)
    var jointPain: YesNoUnknownWithSymptomSeverity? = null,
    @Text("Appetitverlust")
    @SymptomEnum(Symptom.LOSS_OF_APPETITE)
    var lossOfAppetite: YesNoUnknownWithSymptomSeverity? = null,
    @Text("Geruchverlust")
    @SymptomEnum(Symptom.LOSS_OF_SENSE_OF_SMELL)
    var lossOfSenseOfSmell: YesNoUnknownWithSymptomSeverity? = null,
    @Text("Geschmackverlust")
    @SymptomEnum(Symptom.LOSS_OF_TASTE)
    var lossOfTaste: YesNoUnknownWithSymptomSeverity? = null,
    @Text("Lymphadenopathie")
    @SymptomEnum(Symptom.LYMPHADENOPATHY)
    var lymphadenopathy: YesNoUnknownWithSymptomSeverity? = null,
    @Text("Unwohlsein")
    @SymptomEnum(Symptom.MALAISE)
    var malaise: YesNoUnknownWithSymptomSeverity? = null,
    @Text("Muskelschmerzen")
    @SymptomEnum(Symptom.MUSCLE_PAIN)
    var musclePain: YesNoUnknownWithSymptomSeverity? = null,
    @Text("Verstopfte Nase")
    @SymptomEnum(Symptom.NASAL_CONGESTION)
    var nasalCongestion: YesNoUnknownWithSymptomSeverity? = null,
    @Text("Laufende Nase")
    @SymptomEnum(Symptom.NASAL_DISCHARGE)
    var nasalDischarge: YesNoUnknownWithSymptomSeverity? = null,
    @Text("Übelkeit")
    @SymptomEnum(Symptom.NAUSEA)
    var nausea: YesNoUnknownWithSymptomSeverity? = null,
    @Text("Halsschmerzen")
    @SymptomEnum(Symptom.PAIN_IN_THROAT)
    var painInThroat: YesNoUnknownWithSymptomSeverity? = null,
    @Text("Starre")
    @SymptomEnum(Symptom.RIGOR)
    var rigor: YesNoUnknownWithSymptomSeverity? = null,
    @Text("Krampfanfall")
    @SymptomEnum(Symptom.SEIZURE)
    var seizure: YesNoUnknownWithSymptomSeverity? = null,
    @Text("Hautgeschwüre")
    @SymptomEnum(Symptom.SKIN_ULCER)
    var skinUlcer: YesNoUnknownWithSymptomSeverity? = null,
    @Text("Unfähig zu gehen")
    @SymptomEnum(Symptom.UNABLE_TO_WALK)
    var unableToWalk: YesNoUnknownWithSymptomSeverity? = null,
    @Text("Erbrechen")
    @SymptomEnum(Symptom.VOMITING)
    var vomiting: YesNoUnknownWithSymptomSeverity? = null,
    @Text("Keuchen")
    @SymptomEnum(Symptom.WHEEZING)
    var wheezing: YesNoUnknownWithSymptomSeverity? = null,
    @Text("Fieber über 38° Celsius")
    @SymptomEnum(Symptom.FEVER_GREATER_THAN_38_CELSIUS)
    var feverGreaterThan38: YesNoUnknownWithSymptomSeverity? = null,
    @Text("Schwächegefühl")
    @SymptomEnum(Symptom.ASTHENIA)
    var asthenia: YesNoUnknownWithSymptomSeverity? = null,
    @Text("Schmerzen")
    @SymptomEnum(Symptom.PAIN)
    var pain: YesNoUnknownWithSymptomSeverity? = null,
    @Text("Produktiver Husten")
    @SymptomEnum(Symptom.PAIN_IN_THROAT)
    var productiveCough: YesNoUnknownWithSymptomSeverity? = null,
    @Text("Trockener Husten")
    @SymptomEnum(Symptom.DRY_COUGH)
    var dryCough: YesNoUnknownWithSymptomSeverity? = null,
    @Text("Bewusstseinstrübung")
    @SymptomEnum(Symptom.CLOUDED_CONSCIOUSNESS)
    var cloudedConsciousness: YesNoUnknownWithSymptomSeverity? = null
)


data class Therapy(
    @FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/dialysis")
    @Text("Dialyse / Hämofiltration")
    var dialysisOrHemofiltration: YesNoUnknown? = null,
    @FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/apheresis")
    @Text("Apherese")
    var apheresis: YesNoUnknown? = null,
    @FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/prone-position")
    @Text("Bauchlage")
    var pronePosition: YesNoUnknown? = null,
    @FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/extracorporeal-membrane-oxygenation")
    @Text("Extrakorporale Membranoxygenierung")
    var ecmoTherapy: YesNoUnknown? = null,
    @FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/patient-in-icu")
    @Text("Liegt der Patient auf der Intensivstation?")
    var isPatientInTheIntensiveCareUnit: YesNoUnknown? = null,
    @FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/respiratory-therapies")
    @Text("Bitte dokumentieren Sie die Beatmungstherapie")
    var ventilationType: VentilationTypes? = null
)


data class VitalSigns(
    @Text("Kohlendioxidpartialdruck")
    @FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/carbon-dioxide-partial-pressure")
    var pacCO2: Float? = null,
    @Text("Sauerstoffpartialdruck")
    @FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/oxygen-partial-pressure")
    var paO2: Float? = null,
    @Text("FiO2")
    @FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/inhaled-oxygen-concentration")
    var FiO2: Float? = null,
    @Text("pH-Wert")
    @FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/pH")
    var pH: Float? = null,
    @Text("Sepsis-related organ failure assessment score")
    @FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/sofa-score")
    var sofaScore: Int? = null, //TODO: Add to gecco-easy
    @Text("Atemfrequenz")
    @FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/respiratory-rate")
    var respiratoryRate: Int? = null,
    @Text("Blutdruck diastolisch")
    @FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/blood-pressure")
    var diastolicBloodPressure: Int? = null,
    @Text("Blutdruck systolisch")
    @FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/blood-pressure")
    var systolicBloodPressure: Int? = null,
    @FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/heart-rate")
    @Text("Herzfrequenz")
    var heartRate: Int? = null,
    @FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/body-temperature")
    @Text("Körpertemperatur")
    var bodyTemperature: Float? = null,
    @FhirProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/oxygen-saturation")
    @Text("Periphere Sauerstoffsättigung")
    var peripheralOxygenSaturation: Float? = null,
)