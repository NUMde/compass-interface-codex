import CardiovascularDiseases.*
import ChronicNeurologicalMentalDisease.*
import ComplicationsCovid19.*
import RheumatologicalImmunologicalDiseases.*
import YesNoUnknown.NO
import YesNoUnknown.UNKNOWN
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.util.BundleBuilder
import org.hl7.fhir.r4.model.DateTimeType
import org.hl7.fhir.r4.model.IdType
import org.hl7.fhir.r4.model.MedicationStatement
import org.hl7.fhir.r4.model.Reference
import java.io.FileWriter
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*


fun main() {
    val patientId = IdType.newRandomUuid()
    val patientRef = Reference(patientId)
    val dateTimeOfDocumentation = DateTimeType(LocalDateTime.now().toUtilDate())

    val fhirContext = FhirContext.forR4()
    val bundle = BundleBuilder(fhirContext).apply {
        addCreateEntry(
            GeccoPatient(
                patientId = patientId,
                ethnicGroup = EthnicGroup.CAUCASIAN,
                ageInYears = 50.0.toBigDecimal(),
                dateTimeOfDocumentation = dateTimeOfDocumentation
            )
        )
        addCreateEntry(AnaChronicLungDisease(patientRef, ChronicLungDisease.ASTHMA, NO, dateTimeOfDocumentation))
        addCreateEntry(AnaChronicLungDisease(patientRef, ChronicLungDisease.COPD, NO, dateTimeOfDocumentation))
        addCreateEntry(AnaChronicLungDisease(patientRef, ChronicLungDisease.FIBROSIS, NO, dateTimeOfDocumentation))
        addCreateEntry(
            AnaChronicLungDisease(patientRef, ChronicLungDisease.PULMONARY_HYPERTENSION, NO, dateTimeOfDocumentation)
        )
        addCreateEntry(AnaChronicLungDisease(patientRef, ChronicLungDisease.OHS, NO, dateTimeOfDocumentation))
        addCreateEntry(AnaChronicLungDisease(patientRef, ChronicLungDisease.SLEEP_APNEA, NO, dateTimeOfDocumentation))
        addCreateEntry(AnaChronicLungDisease(patientRef, ChronicLungDisease.OSAS, NO, dateTimeOfDocumentation))
        addCreateEntry(
            AnaChronicLungDisease(patientRef, ChronicLungDisease.CYSTIC_FIBROSIS, NO, dateTimeOfDocumentation)
        )
//		addCreateEntry(AnaChronicLungDisease(ChronicLungDisease.OTHER, NO)) //TODO
        addCreateEntry(
            AnaCardiovascular(patientRef, HYPERTENSIVE_DISORDER_SYSTEMIC_ARTERIAL, NO, dateTimeOfDocumentation)
        )
        addCreateEntry(AnaCardiovascular(patientRef, ZUSTAND_NACH_HERZINFARKT, NO, dateTimeOfDocumentation))
        addCreateEntry(AnaCardiovascular(patientRef, CARDIAC_ARRHYTHMIA, NO, dateTimeOfDocumentation))
        addCreateEntry(AnaCardiovascular(patientRef, HEART_FAILURE, NO, dateTimeOfDocumentation))
        addCreateEntry(
            AnaCardiovascular(patientRef, PERIPHERAL_ARTERIAL_OCCLUSIVE_DISEASE, NO, dateTimeOfDocumentation)
        )
        addCreateEntry(AnaCardiovascular(patientRef, ZUSTAND_NACH_REVASKULARISATION, NO, dateTimeOfDocumentation))
        addCreateEntry(AnaCardiovascular(patientRef, CORONARY_ARTERIOSCLEROSIS, NO, dateTimeOfDocumentation))
        addCreateEntry(AnaCardiovascular(patientRef, CAROTID_ARTERY_STENOSIS, NO, dateTimeOfDocumentation))
//		addCreateEntry(AnaDisorderCardiovascular(CardiovascularDiseases.OTHER, NO))
        addCreateEntry(
            AnaChronicLiver(patientRef, ChronicLiverDiseases.STEATOSIS_OF_LIVER, NO, dateTimeOfDocumentation)
        )
        addCreateEntry(
            AnaChronicLiver(patientRef, ChronicLiverDiseases.CIRRHOSIS_OF_LIVER, NO, dateTimeOfDocumentation)
        )
        addCreateEntry(
            AnaChronicLiver(patientRef, ChronicLiverDiseases.CHRONIC_VIRAL_HEPATITIS, NO, dateTimeOfDocumentation)
        )
        addCreateEntry(
            AnaChronicLiver(patientRef, ChronicLiverDiseases.AUTOIMMUNE_LIVER_DISEASE, NO, dateTimeOfDocumentation)
        )
//		addCreateEntry(AnaChronicLiver(ChronicLiverDiseases.OTHER, NO))
        addCreateEntry(
            AnaRheumaticImmunological(
                patientRef,
                RheumatologicalImmunologicalDiseases.INFLAMMATORY_BOWEL_DISEASE, NO, dateTimeOfDocumentation
            )
        )
        addCreateEntry(AnaRheumaticImmunological(patientRef, RHEUMATOID_ARTHRITIS, NO, dateTimeOfDocumentation))
        addCreateEntry(AnaRheumaticImmunological(patientRef, COLLAGENOSIS, NO, dateTimeOfDocumentation))
        addCreateEntry(AnaRheumaticImmunological(patientRef, VASCULITIS, NO, dateTimeOfDocumentation))
        addCreateEntry(
            AnaRheumaticImmunological(patientRef, CONGENITAL_IMMUNODEFICIENCY_DISEASE, NO, dateTimeOfDocumentation)
        )
//		addCreateEntry(RheumatologicalImmunologicalDiseases(RheumatologicalImmunologicalDiseases.OTHER, NO))

        addCreateEntry(AnaTransplant(patientRef, NO, dateTimeOfDocumentation, OrgansForTransplant.BLOOD_VESSEL_PART))
        //TODO: Im ORBIS- und RedCap-Formular ist das beides eins
        addCreateEntry(
            AnaTransplant(patientRef, NO, dateTimeOfDocumentation, OrgansForTransplant.ENTIRE_SMALL_INTESTINE)
        )
        addCreateEntry(
            AnaTransplant(patientRef, NO, dateTimeOfDocumentation, OrgansForTransplant.ENTIRE_LARGE_INTESTINE)
        )
        addCreateEntry(
            AnaTransplant(patientRef, NO, dateTimeOfDocumentation, OrgansForTransplant.EAR_OSSICLE_STRUCTURE)
        )
        addCreateEntry(AnaTransplant(patientRef, NO, dateTimeOfDocumentation, OrgansForTransplant.SKIN_PART))
        addCreateEntry(AnaTransplant(patientRef, NO, dateTimeOfDocumentation, OrgansForTransplant.ENTIRE_HEART))
        addCreateEntry(AnaTransplant(patientRef, NO, dateTimeOfDocumentation, OrgansForTransplant.ENTIRE_HEART_VALVE))
        addCreateEntry(
            AnaTransplant(patientRef, NO, dateTimeOfDocumentation, OrgansForTransplant.CEREBRAL_MENINGES_STRUCTURE)
        )
        addCreateEntry(
            AnaTransplant(patientRef, NO, dateTimeOfDocumentation, OrgansForTransplant.BONE_TISSUE_STRUCTURE)
        )
        addCreateEntry(AnaTransplant(patientRef, NO, dateTimeOfDocumentation, OrgansForTransplant.CARTILAGE_TISSUE))
        addCreateEntry(AnaTransplant(patientRef, NO, dateTimeOfDocumentation, OrgansForTransplant.ENTIRE_LIVER))
        addCreateEntry(AnaTransplant(patientRef, NO, dateTimeOfDocumentation, OrgansForTransplant.ENTIRE_LUNG))
        addCreateEntry(AnaTransplant(patientRef, NO, dateTimeOfDocumentation, OrgansForTransplant.ENTIRE_KIDNEY))
        addCreateEntry(AnaTransplant(patientRef, NO, dateTimeOfDocumentation, OrgansForTransplant.TENDON_STRUCTURE))

        addCreateEntry(
            AnaChronicNeurologicalMental(
                patientRef,
                PARKINSONS_DISEASE, NO, dateTimeOfDocumentation
            )
        )
        addCreateEntry(AnaChronicNeurologicalMental(patientRef, DEMENTIA, NO, dateTimeOfDocumentation))
        addCreateEntry(AnaChronicNeurologicalMental(patientRef, MULTIPLE_SCLEROSIS, NO, dateTimeOfDocumentation))
        addCreateEntry(
            AnaChronicNeurologicalMental(
                patientRef,
                COMBINED_DISORDER_OF_MUSCLE_AND_PERIPHERAL_NERVE,
                NO,
                dateTimeOfDocumentation
            )
        )
        addCreateEntry(AnaChronicNeurologicalMental(patientRef, EPILEPSY, NO, dateTimeOfDocumentation))
        addCreateEntry(AnaChronicNeurologicalMental(patientRef, MIGRAINE, NO, dateTimeOfDocumentation))
        addCreateEntry(
            AnaChronicNeurologicalMental(
                patientRef,
                HISTORY_OF_CEREBROVASCULAR_ACCIDENT_WITH_RESIDUAL_DEFICIT,
                NO,
                dateTimeOfDocumentation
            )
        )
        addCreateEntry(
            AnaChronicNeurologicalMental(
                patientRef,
                HISTORY_OF_CEREBROVASCULAR_ACCIDENT_WITHOUT_RESIDUAL_DEFICITS,
                NO,
                dateTimeOfDocumentation
            )
        )
        addCreateEntry(AnaChronicNeurologicalMental(patientRef, PSYCHOTIC_DISORDER, NO, dateTimeOfDocumentation))
        addCreateEntry(AnaChronicNeurologicalMental(patientRef, DEPRESSIVE_DISORDER, NO, dateTimeOfDocumentation))
        addCreateEntry(AnaChronicNeurologicalMental(patientRef, ANXIETY_DISORDER, NO, dateTimeOfDocumentation))

        //TODO: no-immunization-info / no-known-immunizations slice
        addCreateEntry(Vaccination(patientRef, ImmunizationDisease.INFLUENZA, unknownDateTime()))
        addCreateEntry(Vaccination(patientRef, ImmunizationDisease.BCG, unknownDateTime()))
        addCreateEntry(Vaccination(patientRef, ImmunizationDisease.PNEUMOKOKKEN, unknownDateTime()))
        addCreateEntry(Vaccination(patientRef, ImmunizationDisease.COVID19_ANTIGEN, unknownDateTime()))
        addCreateEntry(Vaccination(patientRef, ImmunizationDisease.COVID19_RNA, unknownDateTime()))
//		addCreateEntry(Vaccination(patientRef, ImmunizationDisease.OTHER, unknownDateTime())) //TODO
        addCreateEntry(NoKnownVaccinations(patientRef))
        //TODO: Im ORBIS-Formular gibt es ein Freitext-Feld für welche Impfungen genau

        addCreateEntry(AnaHIV(patientRef, NO, dateTimeOfDocumentation))
        addCreateEntry(AnaGastrointestinalUlcers(patientRef, NO, dateTimeOfDocumentation))
        addCreateEntry(AnaRespiratoryTherapy(patientRef, NO, unknownDateTime()))
        addCreateEntry(AnaHistoryOfTravel(patientRef, YesNoUnknownOtherNa.UNKNOWN))
        addCreateEntry(AnaChronicKidneyDisease(patientRef, ChronicKidneyDisease.ABSENT, dateTimeOfDocumentation))
        //TODO Dropdown but FHIR allows state for each Diabetes
        addCreateEntry(AnaDiabetes(patientRef, Diabetes.TYPE1, NO, dateTimeOfDocumentation))
        addCreateEntry(AnaCancer(patientRef, CancerStatus.NO, dateTimeOfDocumentation))
        addCreateEntry(AnaSmoking(patientRef, SmokingStatus.FORMER_SMOKER, dateTimeOfDocumentation))
        addCreateEntry(AnaDNR(patientRef, NO))

        //TODO: Bildgebung: Wie wird abgebildet, ob eine Prozedur durchgeführt wurde oder nicht
        addCreateEntry(ImagingProcedure(patientRef, Imaging.CT, unknownDateTime()))
        addCreateEntry(ImagingProcedure(patientRef, Imaging.XRAY, unknownDateTime()))
        addCreateEntry(ImagingProcedure(patientRef, Imaging.ULTRASOUND, unknownDateTime()))
        addCreateEntry(ImagingFinding(patientRef, RadiologicFindings.COVID19))

        addCreateEntry(BodyHeight(patientRef, 180.0, unknownPeriod()))
        addCreateEntry(BodyWeight(patientRef, 80.0, unknownPeriod()))
        addCreateEntry(PregnancyStatus(patientRef, NO, unknownDateTime()))
        addCreateEntry(FrailtyScore(patientRef, FrailityScore.MODERATELY_FRAIL))

        addCreateEntry(KnownExposure(patientRef, NO))


        addCreateEntry(Complications(patientRef, THROMBOSIS, NO, dateTimeOfDocumentation))
        addCreateEntry(Complications(patientRef, EMBOLISM, NO, dateTimeOfDocumentation))
        addCreateEntry(Complications(patientRef, INFECTIOUS_DISEASE_OF_LUNG, NO, dateTimeOfDocumentation))
        addCreateEntry(Complications(patientRef, INFECTIOUS_AGENT_IN_BLOODSTREAM, NO, dateTimeOfDocumentation))
        addCreateEntry(Complications(patientRef, VENOUS_THROMBOSIS, NO, dateTimeOfDocumentation))
        addCreateEntry(Complications(patientRef, PULMONARY_EMBOLISM, NO, dateTimeOfDocumentation))
        addCreateEntry(Complications(patientRef, CEREBROVASCULAR_ACCIDENT, NO, dateTimeOfDocumentation))
        addCreateEntry(Complications(patientRef, MYOCARDIAL_INFARCTION, NO, dateTimeOfDocumentation))
        addCreateEntry(Complications(patientRef, PRE_RENAL_ACUTE_KINDEY_INJURY, NO, dateTimeOfDocumentation))

        addCreateEntry(DiagnosisCovid19(patientRef, StageAtDiagnosis.CRITICAL, unknownDateTime()))

        //TODO: Medication Consistent Yes/No/Unknown?!
        addCreateEntry(MediCovid19(patientRef, MedicationCovid19.ANTIPYRETIC, unknownDateTime()))
        //Kortikosteroide
        addCreateEntry(MediCovid19(patientRef, MedicationCovid19.ATAZANAVIR, unknownDateTime()))
        addCreateEntry(MediCovid19(patientRef, MedicationCovid19.DARUNAVIR, unknownDateTime()))
        addCreateEntry(MediCovid19(patientRef, MedicationCovid19.CHLOROQUINE, unknownDateTime()))
        addCreateEntry(MediCovid19(patientRef, MedicationCovid19.HYDROXYCHLOROQUINE, unknownDateTime()))
        addCreateEntry(MediCovid19(patientRef, MedicationCovid19.IVERMECTIN, unknownDateTime()))
        addCreateEntry(MediCovid19(patientRef, MedicationCovid19.LOPINAVIR_RITONAVIR, unknownDateTime()))
        addCreateEntry(MediCovid19(patientRef, MedicationCovid19.GANCICLOVIR, unknownDateTime()))
        addCreateEntry(MediCovid19(patientRef, MedicationCovid19.OSELTAMIVIR, unknownDateTime()))
        addCreateEntry(MediCovid19(patientRef, MedicationCovid19.REMDESIVIR, unknownDateTime()))
        addCreateEntry(MediCovid19(patientRef, MedicationCovid19.RIBAVIRIN, unknownDateTime()))
        addCreateEntry(MediCovid19(patientRef, MedicationCovid19.CAMOSTAT, unknownDateTime()))
        addCreateEntry(MediCovid19(patientRef, MedicationCovid19.FAVIPIRAVIR, unknownDateTime()))
        addCreateEntry(MediCovid19(patientRef, MedicationCovid19.CONVALESCENT_PLASMA, unknownDateTime()))
        addCreateEntry(MediCovid19(patientRef, MedicationCovid19.STEROIDS_GT_0_5_MG_PER_KG, unknownDateTime()))
        addCreateEntry(MediCovid19(patientRef, MedicationCovid19.STEROIDS_LT_0_5_MG_PER_KG, unknownDateTime()))
        addCreateEntry(MediCovid19(patientRef, MedicationCovid19.TOCILIZUMAB, unknownDateTime()))
        addCreateEntry(MediCovid19(patientRef, MedicationCovid19.SARILUMAB, unknownDateTime()))
        //CNI or mTOR inhibitor
        addCreateEntry(
            MediCovid19(patientRef, MedicationCovid19.TUMOR_NECROSIS_FACTOR_ALPHA_INHIBITOR, unknownDateTime())
        )
        addCreateEntry(MediCovid19(patientRef, MedicationCovid19.INTERLEUKIN_1_RECEPTOR_ANTAGONIST, unknownDateTime()))
        addCreateEntry(MediCovid19(patientRef, MedicationCovid19.RUXOLITINIB, unknownDateTime()))
        addCreateEntry(MediCovid19(patientRef, MedicationCovid19.COLCHICINE, unknownDateTime()))
        addCreateEntry(MediCovid19(patientRef, MedicationCovid19.INTERFERON, unknownDateTime()))
        //HydroxyvitaminD
        addCreateEntry(MediCovid19(patientRef, MedicationCovid19.ZINC, unknownDateTime()))
        //Other


        //TODO: Expand valueset
        addCreateEntry(
            MediACEInhibitor(patientRef, MedicationStatement.MedicationStatementStatus.STOPPED, unknownDateTime())
        )
        addCreateEntry(MediImmunoglobulins(patientRef, UNKNOWN, unknownDateTime()))
        addCreateEntry(MediAntiCoag(patientRef, AntiCoagulant.HEPARIN, unknownDateTime(), null)) //TODO
        addCreateEntry(MediAntiCoag(patientRef, AntiCoagulant.ARGATROBAN, unknownDateTime(), null))
//		addCreateEntry(MediAntiCoag(patientRef, AntiCoagulant., unknownDateTime(), null)) //TODO
        addCreateEntry(MediAntiCoag(patientRef, AntiCoagulant.DANAPAROID, unknownDateTime(), null))
        addCreateEntry(MediAntiCoag(patientRef, AntiCoagulant.PHENPROCOUMON, unknownDateTime(), null))
//		addCreateEntry(MediAntiCoag(patientRef, AntiCoagulant.D, DateTimeType(), null)) //TODO

        addCreateEntry(RespiratoryOutcome(patientRef, dateTimeOfDocumentation, ConditionVerificationStatus.REFUTED))
        addCreateEntry(TypeOfDischarge(patientRef, TypeOfDischarge.ALIVE))
        addCreateEntry(
            FollowUpSwipeResults(patientRef, DetectedNotDetectedInconclusive.INCONCLUSIVE, LocalDate.now())
        )

        addCreateEntry(StudyInclusionDueToCovid19(patientRef, YesNoUnknownOtherNa.UNKNOWN))
        addCreateEntry(InterventionalStudiesParticipation(patientRef, YesNoUnknownOtherNa.UNKNOWN))
        //TODO: Add fields in ORBIS form to capture study identifier?!

        //TODO: Im ORBIS-Formular sind nur 11 Symptome, diese Symptome sind im ValueSet
        addCreateEntry(Symptom(patientRef, Symptom.ABDOMINAL_PAIN, NO, dateTimeOfDocumentation))
//		addCreateEntry(Symptom(patientRef, Symptoms.ASTHENIA, NO))
        addCreateEntry(Symptom(patientRef, Symptom.ASYMPTOMATIC, NO, dateTimeOfDocumentation))
//		addCreateEntry(Symptom(patientRef, Symptoms.BLEEDING, NO))
//		addCreateEntry(Symptom(patientRef, Symptoms.CHEST_PAIN, NO))
//		addCreateEntry(Symptom(patientRef, Symptoms.CHILL, NO))
        addCreateEntry(Symptom(patientRef, Symptom.CLOUDED_CONSCIOUSNESS, NO, dateTimeOfDocumentation))
//		addCreateEntry(Symptom(patientRef, Symptoms.CONJUNCTIVITIS, NO))
        addCreateEntry(Symptom(patientRef, Symptom.COUGH, NO, dateTimeOfDocumentation))
        addCreateEntry(Symptom(patientRef, Symptom.DIARRHEA, NO, dateTimeOfDocumentation))
//		addCreateEntry(Symptom(patientRef, Symptoms.DISTURBANCE_OF_CONSCIOUSNESS, NO))
//		addCreateEntry(Symptom(patientRef, Symptoms.DRY_COUGH, NO))
        addCreateEntry(Symptom(patientRef, Symptom.DYSPNEA, NO, dateTimeOfDocumentation))
//		addCreateEntry(Symptom(patientRef, Symptoms.ERUPTION_OF_SKIN, NO))
//		addCreateEntry(Symptom(patientRef, Symptoms.FATIGUE, NO))
//		addCreateEntry(Symptom(patientRef, Symptoms.FEELING_FEVERISH, NO))
        addCreateEntry(Symptom(patientRef, Symptom.FEVER, NO, dateTimeOfDocumentation))
//		addCreateEntry(Symptom(patientRef, Symptoms.FEVER_GREATER_THAN_38_CELSIUS, NO))
        addCreateEntry(Symptom(patientRef, Symptom.HEADACHE, NO, dateTimeOfDocumentation))
//		addCreateEntry(Symptom(patientRef, Symptoms.HEMOPTYSIS, NO))
//		addCreateEntry(Symptom(patientRef, Symptoms.INDRAWING_OF_RIBS_DURING_RESPIRATION, NO))
//		addCreateEntry(Symptom(patientRef, Symptoms.JOINT_PAIN, NO))
//		addCreateEntry(Symptom(patientRef, Symptoms.LOSS_OF_APPETITE, NO))
        //TODO: Im ORBIS-Formular wird Geruchs und Geschmacksstörungen zusammen abgefragt.
        addCreateEntry(Symptom(patientRef, Symptom.LOSS_OF_SENSE_OF_SMELL, NO, dateTimeOfDocumentation))
        addCreateEntry(Symptom(patientRef, Symptom.LOSS_OF_TASTE, NO, dateTimeOfDocumentation))
        addCreateEntry(Symptom(patientRef, Symptom.LYMPHADENOPATHY, NO, dateTimeOfDocumentation))
//		addCreateEntry(Symptom(patientRef, Symptoms.MALAISE, NO))
//		addCreateEntry(Symptom(patientRef, Symptoms.MUSCLE_PAIN, NO))
//		addCreateEntry(Symptom(patientRef, Symptoms.NASAL_CONGESTION, NO))
//		addCreateEntry(Symptom(patientRef, Symptoms.NASAL_DISCHARGE, NO))
//		addCreateEntry(Symptom(patientRef, Symptoms.NAUSEA, NO))
//		addCreateEntry(Symptom(patientRef, Symptoms.PAIN, NO))
//		addCreateEntry(Symptom(patientRef, Symptoms.PAIN_IN_THROAT, NO))
//		addCreateEntry(Symptom(patientRef, Symptoms.PRODUCTIVE_COUGH, NO))
//		addCreateEntry(Symptom(patientRef, Symptoms.RIGOR, NO))
//		addCreateEntry(Symptom(patientRef, Symptoms.SEIZURE, NO))
//		addCreateEntry(Symptom(patientRef, Symptoms.SKIN_ULCER, NO))
//		addCreateEntry(Symptom(patientRef, Symptoms.UNABLE_TO_WALK, NO))
        addCreateEntry(Symptom(patientRef, Symptom.VOMITING, NO, dateTimeOfDocumentation))
//		addCreateEntry(Symptom(patientRef, Symptoms.WHEEZING, NO))
        //TODO: Andere Symptome

        //TODO: Add performedbegin/end datetime
        addCreateEntry(Therapy(patientRef, Therapies.DIALYSIS, NO))
        addCreateEntry(Therapy(patientRef, Therapies.APHARESIS, NO))
        addCreateEntry(Therapy(patientRef, Therapies.PRONE_POSITION, NO))
        addCreateEntry(Therapy(patientRef, Therapies.ECMO, NO))
        addCreateEntry(isPatientInIntensiveCareUnit(patientRef, NO, Date(), Date()))
        //TODO: Add field for ventilation type in orbis form
        addCreateEntry(VentilationType(patientRef, VentilationTypes.UNKNOWN, unknownDateTime(), unknownDateTime()))
        //TODO Vital Signs
    }.bundle.apply {
        meta.addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/gecco-bundle")
    }

    fhirContext.newJsonParser().encodeResourceToWriter(bundle, FileWriter("gecco-bundle.json"))


}