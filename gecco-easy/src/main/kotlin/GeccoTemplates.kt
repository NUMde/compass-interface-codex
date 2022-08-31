import CardiovascularDiseases.*
import ChronicNeurologicalMentalDisease.*
import RheumatologicalImmunologicalDiseases.*
import YesNoUnknown.*
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.util.BundleBuilder
import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.r4.model.*
import org.hl7.fhir.r4.model.codesystems.DataAbsentReason
import java.io.FileWriter
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*


/**
 * Polyfill to stay compatible with HAPI 5.2.1 code
 */
fun BundleBuilder.addCreateEntry(resource: IBaseResource) {
	this.addTransactionCreateEntry(resource)
}

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
			AnaChronicLungDisease(
				patientRef,
				ChronicLungDisease.PULMONARY_HYPERTENSION,
				NO,
				dateTimeOfDocumentation
			)
		)
		addCreateEntry(AnaChronicLungDisease(patientRef, ChronicLungDisease.OHS, NO, dateTimeOfDocumentation))
		addCreateEntry(AnaChronicLungDisease(patientRef, ChronicLungDisease.SLEEP_APNEA, NO, dateTimeOfDocumentation))
		addCreateEntry(AnaChronicLungDisease(patientRef, ChronicLungDisease.OSAS, NO, dateTimeOfDocumentation))
		addCreateEntry(
			AnaChronicLungDisease(
				patientRef,
				ChronicLungDisease.CYSTIC_FIBROSIS,
				NO,
				dateTimeOfDocumentation
			)
		)
//		addCreateEntry(AnaChronicLungDisease(ChronicLungDisease.OTHER, NO)) //TODO
		addCreateEntry(
			AnaCardiovascular(
				patientRef,
				HYPERTENSIVE_DISORDER_SYSTEMIC_ARTERIAL,
				NO,
				dateTimeOfDocumentation
			)
		)
		addCreateEntry(AnaCardiovascular(patientRef, ZUSTAND_NACH_HERZINFARKT, NO, dateTimeOfDocumentation))
		addCreateEntry(AnaCardiovascular(patientRef, CARDIAC_ARRHYTHMIA, NO, dateTimeOfDocumentation))
		addCreateEntry(AnaCardiovascular(patientRef, HEART_FAILURE, NO, dateTimeOfDocumentation))
		addCreateEntry(
			AnaCardiovascular(
				patientRef,
				PERIPHERAL_ARTERIAL_OCCLUSIVE_DISEASE,
				NO,
				dateTimeOfDocumentation
			)
		)
		addCreateEntry(AnaCardiovascular(patientRef, ZUSTAND_NACH_REVASKULARISATION, NO, dateTimeOfDocumentation))
		addCreateEntry(AnaCardiovascular(patientRef, CORONARY_ARTERIOSCLEROSIS, NO, dateTimeOfDocumentation))
		addCreateEntry(AnaCardiovascular(patientRef, CAROTID_ARTERY_STENOSIS, NO, dateTimeOfDocumentation))
//		addCreateEntry(AnaDisorderCardiovascular(CardiovascularDiseases.OTHER, NO))
		addCreateEntry(
			AnaChronicLiver(
				patientRef,
				ChronicLiverDiseases.STEATOSIS_OF_LIVER,
				NO,
				dateTimeOfDocumentation
			)
		)
		addCreateEntry(
			AnaChronicLiver(
				patientRef,
				ChronicLiverDiseases.CIRRHOSIS_OF_LIVER,
				NO,
				dateTimeOfDocumentation
			)
		)
		addCreateEntry(
			AnaChronicLiver(
				patientRef,
				ChronicLiverDiseases.CHRONIC_VIRAL_HEPATITIS,
				NO,
				dateTimeOfDocumentation
			)
		)
		addCreateEntry(
			AnaChronicLiver(
				patientRef,
				ChronicLiverDiseases.AUTOIMMUNE_LIVER_DISEASE,
				NO,
				dateTimeOfDocumentation
			)
		)
//		addCreateEntry(AnaChronicLiver(ChronicLiverDiseases.OTHER, NO))
		addCreateEntry(AnaRheumaticImmunological(patientRef, INFLAMMATORY_BOWEL_DISEASE, NO, dateTimeOfDocumentation))
		addCreateEntry(AnaRheumaticImmunological(patientRef, RHEUMATOID_ARTHRITIS, NO, dateTimeOfDocumentation))
		addCreateEntry(AnaRheumaticImmunological(patientRef, COLLAGENOSIS, NO, dateTimeOfDocumentation))
		addCreateEntry(AnaRheumaticImmunological(patientRef, VASCULITIS, NO, dateTimeOfDocumentation))
		addCreateEntry(
			AnaRheumaticImmunological(
				patientRef,
				CONGENITAL_IMMUNODEFICIENCY_DISEASE,
				NO,
				dateTimeOfDocumentation
			)
		)
//		addCreateEntry(RheumatologicalImmunologicalDiseases(RheumatologicalImmunologicalDiseases.OTHER, NO))

		addCreateEntry(AnaTransplant(patientRef, NO, dateTimeOfDocumentation, OrgansForTransplant.BLOOD_VESSEL_PART))
		//TODO: Im ORBIS- und RedCap-Formular ist das beides eins
		addCreateEntry(
			AnaTransplant(
				patientRef,
				NO,
				dateTimeOfDocumentation,
				OrgansForTransplant.ENTIRE_SMALL_INTESTINE
			)
		)
		addCreateEntry(
			AnaTransplant(
				patientRef,
				NO,
				dateTimeOfDocumentation,
				OrgansForTransplant.ENTIRE_LARGE_INTESTINE
			)
		)
		addCreateEntry(
			AnaTransplant(
				patientRef,
				NO,
				dateTimeOfDocumentation,
				OrgansForTransplant.EAR_OSSICLE_STRUCTURE
			)
		)
		addCreateEntry(AnaTransplant(patientRef, NO, dateTimeOfDocumentation, OrgansForTransplant.SKIN_PART))
		addCreateEntry(AnaTransplant(patientRef, NO, dateTimeOfDocumentation, OrgansForTransplant.ENTIRE_HEART))
		addCreateEntry(AnaTransplant(patientRef, NO, dateTimeOfDocumentation, OrgansForTransplant.ENTIRE_HEART_VALVE))
		addCreateEntry(
			AnaTransplant(
				patientRef,
				NO,
				dateTimeOfDocumentation,
				OrgansForTransplant.CEREBRAL_MENINGES_STRUCTURE
			)
		)
		addCreateEntry(
			AnaTransplant(
				patientRef,
				NO,
				dateTimeOfDocumentation,
				OrgansForTransplant.BONE_TISSUE_STRUCTURE
			)
		)
		addCreateEntry(AnaTransplant(patientRef, NO, dateTimeOfDocumentation, OrgansForTransplant.CARTILAGE_TISSUE))
		addCreateEntry(AnaTransplant(patientRef, NO, dateTimeOfDocumentation, OrgansForTransplant.ENTIRE_LIVER))
		addCreateEntry(AnaTransplant(patientRef, NO, dateTimeOfDocumentation, OrgansForTransplant.ENTIRE_LUNG))
		addCreateEntry(AnaTransplant(patientRef, NO, dateTimeOfDocumentation, OrgansForTransplant.ENTIRE_KIDNEY))
		addCreateEntry(AnaTransplant(patientRef, NO, dateTimeOfDocumentation, OrgansForTransplant.TENDON_STRUCTURE))

		addCreateEntry(AnaChronicNeurologicalMental(patientRef, PARKINSONS_DISEASE, NO, dateTimeOfDocumentation))
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


		addCreateEntry(Complications(patientRef, ComplicationsCovid19.THROMBOSIS, NO, dateTimeOfDocumentation))
		addCreateEntry(Complications(patientRef, ComplicationsCovid19.EMBOLISM, NO, dateTimeOfDocumentation))
		addCreateEntry(Complications(patientRef, ComplicationsCovid19.INFECTIOUS_DISEASE_OF_LUNG, NO, dateTimeOfDocumentation))
		addCreateEntry(Complications(patientRef, ComplicationsCovid19.INFECTIOUS_AGENT_IN_BLOODSTREAM, NO, dateTimeOfDocumentation))
		addCreateEntry(Complications(patientRef, ComplicationsCovid19.VENOUS_THROMBOSIS, NO, dateTimeOfDocumentation))
		addCreateEntry(Complications(patientRef, ComplicationsCovid19.PULMONARY_EMBOLISM, NO, dateTimeOfDocumentation))
		addCreateEntry(Complications(patientRef, ComplicationsCovid19.CEREBROVASCULAR_ACCIDENT, NO, dateTimeOfDocumentation))
		addCreateEntry(Complications(patientRef, ComplicationsCovid19.MYOCARDIAL_INFARCTION, NO, dateTimeOfDocumentation))
		addCreateEntry(Complications(patientRef, ComplicationsCovid19.PRE_RENAL_ACUTE_KINDEY_INJURY, NO, dateTimeOfDocumentation))

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
			MediCovid19(
				patientRef,
				MedicationCovid19.TUMOR_NECROSIS_FACTOR_ALPHA_INHIBITOR,
				unknownDateTime()
			)
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
		for (i in 1 until 1) {
			addCreateEntry(
				FollowUpSwipeResults(patientRef, DetectedNotDetectedInconclusive.INCONCLUSIVE, LocalDate.now())
			)
		}

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



fun GeccoPatient(
	patientId: IdType,
	ethnicGroup: EthnicGroup? = null,
	ageInYears: BigDecimal? = null,
	dateTimeOfDocumentation: DateTimeType
): Patient {
	fun AgeInYears(yearsOfLife: BigDecimal) = Age().apply {
		system = "http://unitsofmeasure.org"
		unit = "years"
		code = "a"
		value = yearsOfLife
	}


	fun EthnicGroupExtension(ethnicGroup: EthnicGroup) = Extension(
		"https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/ethnic-group",
		ethnicGroup.coding
	)

	fun AgeExtension(ageInYears: BigDecimal, dateTimeOfDocumentation: DateTimeType) = Extension().apply {
		url = "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/age"
		extension.add(Extension("dateTimeOfDocumentation", dateTimeOfDocumentation)) //TODO
		extension.add(Extension("age", AgeInYears(ageInYears))) //TODO
	}

	return Patient().apply {
		meta = Meta().apply {
			profile = listOf(
				CanonicalType("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/Patient"),
			)
		}
		setId(patientId)
		identifier = listOf()
		if (ageInYears != null) {
			extension.add(AgeExtension(ageInYears, dateTimeOfDocumentation))
		}
		if (ethnicGroup != null) {
			extension.add(EthnicGroupExtension(ethnicGroup))
		}
	}


}

fun Anamnese(
	patientRef: Reference,
	codeableConcept: CodeableConcept,
	yesNoUnknown: YesNoUnknown,
	recordedDate: DateTimeType
) = Condition().apply {
	code = codeableConcept
	subject = patientRef
	recordedDateElement = recordedDate

	when (yesNoUnknown) {
		YES -> {
			verificationStatus = ConditionVerificationStatus.VERIFIED.codeableConcept
			clinicalStatus = ConditionClinicalStatus.ACTIVE.codeableConcept
		}
		NO -> {
			verificationStatus = ConditionVerificationStatus.REFUTED.codeableConcept
			clinicalStatus = null
		}
		else -> {
			modifierExtension = listOf(uncertainityOfPresence())
		}
	}
}


fun AnaChronicLungDisease(
	patientRef: Reference,
	chronicLungDisease: ChronicLungDisease,
	yesNoUnknown: YesNoUnknown,
	recordedDate: DateTimeType
) =
	Anamnese(patientRef, chronicLungDisease.codeableConcept, yesNoUnknown, recordedDate).apply {
		meta =
			Meta().addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/chronic-lung-diseases") //TODO: FIXME
		category = listOf(CodeableConcept(snomed("418112009", "Pulmonary medicine")))
	}

fun AnaCardiovascular(
	patientRef: Reference,
	cardiovascularDisease: CardiovascularDiseases,
	yesNoUnknown: YesNoUnknown,
	recordedDate: DateTimeType,
) =
	Anamnese(patientRef, cardiovascularDisease.codeableConcept, yesNoUnknown, recordedDate).apply {
		meta =
			Meta().addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/cardiovascular-diseases") //TODO: FIXME
		category = listOf(CodeableConcept(snomed("722414000", "Vascular medicine")))
	}

fun AnaChronicLiver(
	patientRef: Reference,
	chronicLiverDisease: ChronicLiverDiseases,
	yesNoUnknown: YesNoUnknown,
	recordedDate: DateTimeType
) =
	Anamnese(patientRef, chronicLiverDisease.codeableConcept, yesNoUnknown, recordedDate).apply {
		meta =
			Meta().addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/chronic-liver-diseases")
		category = listOf(CodeableConcept(snomed("408472002", "Hepatology")))
	}

fun AnaRheumaticImmunological(
	patientRef: Reference,
	rheumatologicalImmunologicalDisease: RheumatologicalImmunologicalDiseases,
	yesNoUnknown: YesNoUnknown,
	recordedDate: DateTimeType
) = Anamnese(patientRef, rheumatologicalImmunologicalDisease.codeableConcept, yesNoUnknown, recordedDate).apply {
	meta =
		Meta().addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/rheumatological-immunological-diseases")
	category = listOf(CodeableConcept().apply {
		coding = listOf(snomed("394810000", "Rheumatology"), snomed("408480009", "Clinical immunology"))
	})
}

fun AnaChronicNeurologicalMental(
	patientRef: Reference,
	disease: ChronicNeurologicalMentalDisease,
	yesNoUnknown: YesNoUnknown,
	recordedDate: DateTimeType
) = Anamnese(patientRef, disease.codeableConcept, yesNoUnknown, recordedDate).apply {
	meta =
		Meta().addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/chronic-neurological-mental-diseases")
	category = listOf(CodeableConcept().apply {
		coding = listOf(snomed("394591006", "Neurology"), snomed("394587001", "Psychiatry"))
	})
}

fun AnaHIV(patientRef: Reference, yesNoUnknown: YesNoUnknown, recordedDate: DateTimeType) =
	//TODO: Theoretisch sind auch andere Codes zugelassen
	Anamnese(
		patientRef,
		CodeableConcept(snomed("86406008", "Human immunodeficiency virus infection")),
		yesNoUnknown,
		recordedDate,
	).apply {
		meta =
			Meta().addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/human-immunodeficiency-virus-infection")
		category = listOf(CodeableConcept(snomed("394807007", "Infectious diseases (specialty)")))
	}

fun AnaSmoking(patientRef: Reference, smokingStatus: SmokingStatus, recordedDate: DateTimeType) = Observation().apply {
	meta = Meta().addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/smoking-status")
	status = Observation.ObservationStatus.FINAL
	category = listOf(ObservationCategory.SOCIAL_HISTORY.codeableConcept)
	code = CodeableConcept(loinc("72166-2", "Tobacco smoking status"))
	subject = patientRef
	value = smokingStatus.codeableConcept
	effective = recordedDate
}

fun ObservationLab(patientRef: Reference, coding: Coding, quantity: Quantity, recordedDate: DateTimeType) = Observation().apply {
	meta = Meta().addProfile("https://www.medizininformatik-initiative.de/fhir/core/modul-labor/StructureDefinition/ObservationLab")
	status = Observation.ObservationStatus.FINAL
	code = CodeableConcept(coding)
	subject = patientRef
	value = quantity
	effective = recordedDate
}


fun AnaGastrointestinalUlcers(patientRef: Reference, yesNoUnknown: YesNoUnknown, recordedDate: DateTimeType) =
	Anamnese(
		patientRef,
		CodeableConcept(snomed("40845000", "Gastrointestinal ulcer")),
		yesNoUnknown,
		recordedDate
	).apply {
		meta =
			Meta().addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/gastrointestinal-ulcers")
		category = listOf(CodeableConcept(snomed("394584008", "Gastroenterology")))
	}


fun AnaRespiratoryTherapy(patientRef: Reference, yesNoUnknown: YesNoUnknown, performedDt: DateTimeType) =
	Procedure().apply {
		meta =
			Meta().addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/respiratory-therapies")
		category = CodeableConcept(snomed("277132007", "Therapeutic procedure (procedure)"))
		code = CodeableConcept(snomed("53950000", "Respiratory therapy (procedure)"))
		subject = patientRef
		//TODO: Process actual value, this is my guess on how to process but in the Implementation Guide, nothing is actually stated.
		status = when (yesNoUnknown) {
			YES -> Procedure.ProcedureStatus.INPROGRESS
			NO -> Procedure.ProcedureStatus.NOTDONE
			UNKNOWN -> Procedure.ProcedureStatus.UNKNOWN
		}
		performed = performedDt
}

fun AnaChronicKidneyDisease(
	patientRef: Reference,
	chronicKidneyDisease: ChronicKidneyDisease,
	recordedDate: DateTimeType
) = Condition().apply {
	meta =
		Meta().addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/chronic-kidney-diseases")
	category = listOf(CodeableConcept(snomed("394589003", "Nephrology (qualifier value)")))
	subject = patientRef

	code = if(chronicKidneyDisease !in listOf(ChronicKidneyDisease.ABSENT, ChronicKidneyDisease.UNKNOWN)) {
		chronicKidneyDisease.codeableConcept
	} else {
		ChronicKidneyDisease.CHRONIC_KIDNEY_DISEASE.codeableConcept
	}
	when (chronicKidneyDisease) {
		ChronicKidneyDisease.UNKNOWN -> extension = listOf(uncertainityOfPresence())
		ChronicKidneyDisease.ABSENT -> verificationStatus = ConditionVerificationStatus.REFUTED.codeableConcept
		else -> verificationStatus = ConditionVerificationStatus.VERIFIED.codeableConcept
	}
	recordedDateElement = recordedDate
}

fun AnaTransplant(
	patientRef: Reference,
	yesNoUnknown: YesNoUnknown,
	recordedDate: DateTimeType,
	organ: OrgansForTransplant?
) =
	Anamnese(
		patientRef,
		CodeableConcept(snomed("161663000", "History of being a tissue or organ recipient")),
		yesNoUnknown,
		recordedDate
	).apply {
		meta =
			Meta().addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/organ-recipient")
		category = listOf(CodeableConcept(snomed("788415003", "Transplant medicine")))
		//TODO: Wie ist das gemeint? Alle transplantierten Organe in eine Ressource oder für jedes Organ eine eigene?
		if (organ != null) {
			bodySite = listOf(CodeableConcept(organ.snomed))
		}
	}

fun AnaDiabetes(patientRef: Reference, diabetes: Diabetes, yesNoUnknown: YesNoUnknown, recordedDate: DateTimeType) =
	Anamnese(patientRef, diabetes.codeableConcept, yesNoUnknown, recordedDate).apply {
		meta =
			Meta().addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/diabetes-mellitus")
		category = listOf(CodeableConcept(snomed("408475000", "Diabetic medicine")))
	}

fun AnaCancer(patientRef: Reference, cancerStatus: CancerStatus, recordedDate: DateTimeType) = Condition().apply {
	meta =
		Meta().addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/malignant-neoplastic-disease")
	category = listOf(CodeableConcept(snomed("394593009", "Medical oncology (qualifier value)")))
	code = CodeableConcept(snomed("363346000", "Malignant neoplastic disease"))
	subject = patientRef
	recordedDateElement = recordedDate
	when (cancerStatus) {
		CancerStatus.ACTIVE -> {
			verificationStatus = ConditionVerificationStatus.VERIFIED.codeableConcept
			clinicalStatus = ConditionClinicalStatus.ACTIVE.codeableConcept
		}
		CancerStatus.REMISSION -> {
			verificationStatus = ConditionVerificationStatus.VERIFIED.codeableConcept
			clinicalStatus = ConditionClinicalStatus.REMISSION.codeableConcept
		}
		CancerStatus.NO -> {
			verificationStatus = ConditionVerificationStatus.REFUTED.codeableConcept
		}
		CancerStatus.UNKNOWN -> {
			extension.add(uncertainityOfPresence())
		}
	}

}

fun AnaHistoryOfTravel(patientRef: Reference,
					   yesNoUnknownOtherNa: YesNoUnknownOtherNa,
					   country: Countries? = null,
					   federalState: FederalStates? = null,
					   city: String? = null,
					   from: Date? = null,
					   till: Date? = null) = Observation().apply {
	meta = Meta().addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/history-of-travel")
	status = Observation.ObservationStatus.FINAL
	category = listOf(ObservationCategory.SOCIAL_HISTORY.codeableConcept)
	code = CodeableConcept().apply {
		coding = listOf(
			loinc("8691-8", "History of Travel"),
			snomed("443846001", "Detail of history of travel (observable entity)")
		)
		text = "History of travel"
	}
	subject = patientRef
	value = CodeableConcept(yesNoUnknownOtherNa.coding)

	if (yesNoUnknownOtherNa == YesNoUnknownOtherNa.YES) {
		if (from != null) {
			addComponent().apply {
				code = CodeableConcept(loinc("82752-7", "Date travel started")).setText("Travel start date")
				value = DateTimeType(from)
			}
		}
		if (country != null) {
			addComponent().apply {
				code = CodeableConcept(loinc("94651-7", "Country of travel")).setText("Country of travel")
				value = CodeableConcept(country.coding)
			}
		}
		if (federalState != null) {
			addComponent().apply {
				code = CodeableConcept(loinc("82754-3", "State of travel")).setText("State of travel")
				value = CodeableConcept(federalState.coding)
			}
		}
		if (city != null) {
			addComponent().apply {
				code = CodeableConcept(loinc("94653-3", "City of travel")).setText("City of travel")
				value = StringType(city)
			}
		}
		if (till != null) {
			addComponent().apply {
				code = CodeableConcept(loinc("91560-3", "Date of departure from travel destination")).setText("Travel end date")
				value = DateTimeType(till)
			}
		}
	}
}

fun AnaDNR(patientRef: Reference, yesNoUnknown: YesNoUnknown) = AnaDNR(patientRef, when(yesNoUnknown) {
	YES -> Resuscitation.YES
	NO -> Resuscitation.NO
	UNKNOWN -> Resuscitation.UNKNOWN
})

fun AnaDNR(patientRef: Reference, resuscitation: Resuscitation) = Consent().apply {
	//TODO: Laut Github soll dies hier von Consent nach Observatin migriert werden, aber ich sehe keine Änderung im
	meta =
		Meta().addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/do-not-resuscitate-order")

	status = Consent.ConsentState.ACTIVE
	scope =
		CodeableConcept(Coding("http://terminology.hl7.org/CodeSystem/consentscope", "adr", "Advanced Care Directive"))
	patient = patientRef

	category = listOf(
		CodeableConcept(Coding("http://terminology.hl7.org/CodeSystem/consentcategorycodes", "dnr", "Do Not Resuscitate"))
	)
	policy = listOf(Consent.ConsentPolicyComponent().apply {
		uri = "https://www.aerzteblatt.de/archiv/65440/DNR-Anordnungen-Das-fehlende-Bindeglied"
	})
	provision = Consent.provisionComponent().apply {
		code = listOf(CodeableConcept(resuscitation.coding))
	}

}

fun Vaccination(patientRef: Reference, disease: ImmunizationDisease, occurenceDt: DateTimeType) = Immunization().apply {
	meta = Meta().addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/immunization")
	occurrence = occurenceDt
	status = Immunization.ImmunizationStatus.COMPLETED
	patient = patientRef
	protocolApplied = listOf(Immunization.ImmunizationProtocolAppliedComponent().apply {
		doseNumberStringType.extension = listOf(dataAbsentReasonUnknown())
		targetDisease = listOf(disease.targetDisease)
	})
	vaccineCode = disease.vaccineCode
	//TODO: protocolApplied ist optional und vaccineCode ist Pflicht aber soll ggf mit dataAbsentReason auf uknown gesetzt werden
}

fun VaccinationCovid19(patientRef: Reference, vaccine: Covid19Vaccine, occurenceDt: DateTimeType) = Immunization().apply {
	meta = Meta().addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/immunization")
	occurrence = occurenceDt
	status = Immunization.ImmunizationStatus.COMPLETED
	patient = patientRef
	protocolApplied = listOf(Immunization.ImmunizationProtocolAppliedComponent().apply {
		doseNumberStringType.extension = listOf(dataAbsentReasonUnknown())
		targetDisease = listOf(CodeableConcept(snomed("840539006", "Disease caused by Severe acute respiratory syndrome coronavirus 2 (disorder)")))
	})
	vaccineCode = CodeableConcept(vaccine.snomed).addCoding(vaccine.coding)
	//TODO: protocolApplied ist optional und vaccineCode ist Pflicht aber soll ggf mit dataAbsentReason auf uknown gesetzt werden
}

fun NoKnownVaccinations(patientRef: Reference) = Immunization().apply {
	meta = Meta().addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/immunization")
	occurrence = unknownDateTime()
	status = Immunization.ImmunizationStatus.NOTDONE
	patient = patientRef
	vaccineCode = CodeableConcept(
		Coding(
			"http://hl7.org/fhir/uv/ips/CodeSystem/absent-unknown-uv-ips",
			"no-known-immunizations",
			"No known immunizations"
		)
	)
}



//TODO: Werden Antikoerpertests am UKM überhaupt eingesetzt?
fun ErregernachweisPCR(patientRef: Reference, detectedInconclusive: DetectedNotDetectedInconclusive) =
	Observation().apply { //TODO: Extract elements from MII LabratoryObservation profile and create a template
		meta =
			Meta().addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/sars-cov-2-rt-pcr")
		identifier = listOf(
			Identifier().apply {
				type = CodeableConcept(
					Coding("http://terminology.hl7.org/CodeSystem/v2-0203", "OBI", "Observation Instance Identifier")
				)
				system = "https://ukmuenster.de/ORBIS/"
				value = "TODO" //TODO
				assigner = Reference() //TODO
			}
		)
		status = Observation.ObservationStatus.FINAL
		category = listOf(ObservationCategory.LAB.codeableConcept)
		code = CodeableConcept().apply {
			coding = listOf(
				loinc(
					"94500-6",
					"SARS-CoV-2 (COVID-19) RNA [Presence] in Respiratory specimen by NAA with probe detection"
				)
			)
			text = "SARS-CoV-2-RNA (PCR)"
		}
		subject = patientRef
		effective = DateTimeType() //TODO
		value = detectedInconclusive.codeableConcept
	}


fun FrailtyScore(patientRef: Reference, frailityScore: FrailityScore) = Observation().apply {
	meta = Meta().apply {
		profile =
			listOf(CanonicalType("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/frailty-score"))
	}
	status = Observation.ObservationStatus.FINAL
	category = listOf(ObservationCategory.SURVEY.codeableConcept)
	code = CodeableConcept().apply {
		coding = listOf(snomed("763264000", "Canadian Study of Health and Aging Clinical Frailty Scale score"))
		text = "Frailty Scale Score"
	}
	value = frailityScore.codeableConcept
	subject = patientRef
	//method =
}

fun BodyWeight(patientRef: Reference, weightInKg: Double, effectiveDt: Period) = Observation().apply {
	meta = Meta().addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/body-weight")
	status = Observation.ObservationStatus.FINAL
	category = listOf(ObservationCategory.VITAL_SIGNS.codeableConcept)
	code = CodeableConcept().apply {
		text = "Body weight"
		coding = listOf(
			loinc("29463-7", "Body weight"),
			snomed("27113001", "Body weight (observable entity)")
		)
	}
	subject = patientRef
	effective = effectiveDt
	value = Quantity(null, weightInKg, "http://unitsofmeasure.org", "kg", "kilogram")
}

fun BodyHeight(patientRef: Reference, heightInCm: Double, effectiveDt: Period) = Observation().apply {
	meta = Meta().addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/body-height")
	status = Observation.ObservationStatus.FINAL
	category = listOf(ObservationCategory.VITAL_SIGNS.codeableConcept)
	code = CodeableConcept().apply {
		text = "Body height"
		coding = listOf(
			loinc("8302-2", "Body height"),
			snomed("50373000", "Body height measure (observable entity)")
		)
	}
	subject = patientRef
	effective = effectiveDt
	value = Quantity(null, heightInCm, "http://unitsofmeasure.org", "cm", "centimeter")
}


fun PregnancyStatus(patientRef: Reference, yesNoUnknown: YesNoUnknown, effectiveDt: DateTimeType) =
	 PregnancyStatus(patientRef, when (yesNoUnknown) {
		YES -> PregnancyStatus.PREGNANT
		NO -> PregnancyStatus.NOT_PREGNANT
		UNKNOWN -> PregnancyStatus.UNKNOWN
	}, effectiveDt)

fun PregnancyStatus(patientRef: Reference, pregnancyStatus: PregnancyStatus, effectiveDt: DateTimeType) =
	Observation().apply {
		meta =
			Meta().addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/pregnancy-status")
		status = Observation.ObservationStatus.FINAL
		code = CodeableConcept(loinc("82810-3", "Pregnancy status"))
		subject = patientRef
		effective = effectiveDt
		value = pregnancyStatus.codeableConcept

	}

fun KnownExposure(patientRef: Reference, yesNoUnknown: YesNoUnknown) = Observation().apply {
	meta = Meta().addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/known-exposure")
	category = listOf(ObservationCategory.SOCIAL_HISTORY.codeableConcept)
	code = CodeableConcept(loinc("88636-6", "Known exposure [Identifier]"))
	status = Observation.ObservationStatus.FINAL
	subject = patientRef
	when (yesNoUnknown) {
		YES -> value = KnownExposureVS.YES.codeableConcept
		NO -> value = KnownExposureVS.NO.codeableConcept
		UNKNOWN -> dataAbsentReason =
			CodeableConcept(DataAbsentReason.UNKNOWN.let { Coding(it.system, it.toCode(), it.display) })
	}
}

fun Complications(
	patientRef: Reference,
	complicationsCovid19: ComplicationsCovid19,
	yesNoUnknown: YesNoUnknown,
	recordedDate: DateTimeType
) = Condition().apply {
	meta =
		Meta().addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/complications-covid-19")
	code = complicationsCovid19.codeableConcept
	recordedDateElement = recordedDate
	category = listOf(CodeableConcept(snomed("116223007", "Complication (disorder)")))
	subject = patientRef
	when (yesNoUnknown) {
		NO -> verificationStatus = ConditionVerificationStatus.REFUTED.codeableConcept
		YES -> verificationStatus = ConditionVerificationStatus.VERIFIED.codeableConcept
		UNKNOWN -> extension = listOf(uncertainityOfPresence())
	}
}

fun DiagnosisCovid19(patientRef: Reference, stageAtDiagnosis: StageAtDiagnosis, recordedDate: DateTimeType) =
	Condition().apply {
		meta =
			Meta().addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/diagnosis-covid-19")
		//TODO is this shared with some other element?
		category = listOf(CodeableConcept(snomed("394807007", "Infectious diseases (specialty) (qualifier value)")))
		code = CodeableConcept(
			snomed("840539006", "Disease caused by Severe acute respiratory syndrome coronavirus 2 (disorder)")
		)
		stage = listOf(Condition.ConditionStageComponent().apply {
			summary = stageAtDiagnosis.codeableConcept
			type = CodeableConcept(loinc("88859-4", "Disease stage score for risk calculation"))
		})
		recordedDateElement = recordedDate
		subject = patientRef
}

fun Symptom(
	patientRef: Reference,
	symptom: Symptom,
	response: YesNoUnknown,
	recordedDate: DateTimeType,
	severity: SymptomSeverity? = null
) = Condition().apply {
	meta =
		Meta().addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/symptoms-covid-19")
	category = listOf(CodeableConcept(loinc("75325-1", "Symptom")))
	code = symptom.codeableConcept
	recordedDateElement = recordedDate
	//onset =
	subject = patientRef
	this.severity = severity?.let { CodeableConcept(it.coding) }
	when (response) {
		NO -> verificationStatus = ConditionVerificationStatus.REFUTED.codeableConcept
		YES -> verificationStatus = ConditionVerificationStatus.VERIFIED.codeableConcept
		UNKNOWN -> modifierExtension = listOf(uncertainityOfPresence())
	}
}



fun RespiratoryOutcome(patientRef: Reference, recordedDate: DateTimeType, response: ConditionVerificationStatus) =
	Condition().apply {
		meta =
			Meta().addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/dependence-on-ventilator")
		category = listOf(CodeableConcept(snomed("404989005", "Ventilation status (observable entity)")))
		code = CodeableConcept(snomed("444932008", "Dependence on ventilator (finding)"))
		recordedDateElement = recordedDate
		subject = patientRef
		verificationStatus = response.codeableConcept
	}

fun TypeOfDischarge(patientRef: Reference, typeOfDischarge: TypeOfDischarge) = Observation().apply {
	meta =
		Meta().addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/discharge-disposition")
	status = Observation.ObservationStatus.FINAL
	category = listOf(ObservationCategory.SOCIAL_HISTORY.codeableConcept)
	code = CodeableConcept().apply {
		coding = listOf(loinc("55128-3", "Discharge disposition"))
		text = "Type of discharge"
	}
	//effective = (optional)
	subject = patientRef
	value = typeOfDischarge.codeableConcept
}


fun Therapy(patientRef: Reference, therapy: Therapies, yesNoUnknown: YesNoUnknown) = Procedure().apply {
	meta = Meta().addProfile(therapy.profile)
	category = therapy.category
	code = therapy.codeableConcept
	subject = patientRef

	performed = DateTimeType().apply {
		extension = listOf(
			Extension(
				"http://hl7.org/fhir/StructureDefinition/data-absent-reason", CodeType(
					when (yesNoUnknown) {
						YES, UNKNOWN -> DataAbsentReason.UNKNOWN.toCode()
						NO -> DataAbsentReason.NOTPERFORMED.toCode()
					}
				)
			)
		)
	}
	status = when (yesNoUnknown) {
		YES -> Procedure.ProcedureStatus.INPROGRESS
		NO -> Procedure.ProcedureStatus.NOTDONE
		UNKNOWN -> Procedure.ProcedureStatus.UNKNOWN
	}

}

fun isPatientInIntensiveCareUnit(patientRef: Reference, yesNoUnknown: YesNoUnknown, start: Date, end: Date) =
	Observation().apply {
		meta = Meta().addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/patient-in-icu")
		category = listOf(ObservationCategory.SOCIAL_HISTORY.codeableConcept)
		status = Observation.ObservationStatus.FINAL
		subject = patientRef
		code = CodeableConcept().apply {
			coding = listOf(
				Coding(
					"https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/ecrf-parameter-codes",
					"01",
					"Is the patient in the intensive care unit?"
				)
			)
		}
		this.effective =
			Period().apply { //TODO: Nachfragen, ob es nicht mehr Sinn macht, diese Angabe nur bei ICU-Aufenthalt zu setzen?
				this.start = start
				this.end = end
			}
		value = CodeableConcept(
			when (yesNoUnknown) {
				YES -> YesNoUnknownOtherNa.YES
				NO -> YesNoUnknownOtherNa.NO
				UNKNOWN -> YesNoUnknownOtherNa.UNKNOWN
			}.coding
		)

	}

fun VentilationType(patientRef: Reference, ventilationTypes: VentilationTypes, begin: DateTimeType, end: DateTimeType) =
	Procedure().apply {
		meta =
			Meta().addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/respiratory-therapies")
		status = ventilationTypes.status
		category = CodeableConcept(snomed("277132007", "Therapeutic procedure (procedure)"))
		code = CodeableConcept(ventilationTypes.code)
		usedCode = ventilationTypes.used?.let { listOf(CodeableConcept(it)) }
		subject = patientRef
		performed = Period().apply {
			startElement = begin
			endElement = end
		}
	}


fun FollowUpSwipeResults(
	patientRef: Reference,
	detectedInconclusive: DetectedNotDetectedInconclusive,
	date: LocalDate
) = ErregernachweisPCR(patientRef, detectedInconclusive)
//TODO: Nachfragen, wo die Unterschiede sind

fun MediCovid19(patientRef: Reference, medicationCovid19: MedicationCovid19, effectiveDt: DateTimeType) =
	MedicationStatement().apply {
		meta =
			Meta().addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/pharmacological-therapy")
		status = MedicationStatement.MedicationStatementStatus.ACTIVE
		medication = CodeableConcept().apply {
			coding = listOf(medicationCovid19.snomed ?: medicationCovid19.num)
		}
		subject = patientRef
		effective = effectiveDt
	}


fun MediACEInhibitor(
	patientRef: Reference,
	pStatus: MedicationStatement.MedicationStatementStatus,
	effectiveDt: DateTimeType
) = MedicationStatement().apply {
	meta =
		Meta().addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/pharmacological-therapy")
	status = pStatus
	medication = CodeableConcept().apply {
		coding = listOf(
			snomed("41549009", "Product containing angiotensin-converting enzyme inhibitor (product)"),
			atc("C09A", "ACE-HEMMER, REIN")
		)
		text = "ACE inhibitors"
	}
	subject = patientRef
	effective = effectiveDt
}

fun MediImmunoglobulins(patientRef: Reference, yesNoUnknown: YesNoUnknown, effectiveDt: DateTimeType) =
	MedicationStatement().apply {
		meta =
			Meta().addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/pharmacological-therapy")
		status = when (yesNoUnknown) {
			YES -> MedicationStatement.MedicationStatementStatus.ACTIVE
			NO -> MedicationStatement.MedicationStatementStatus.NOTTAKEN
			UNKNOWN -> MedicationStatement.MedicationStatementStatus.UNKNOWN
		}
		medication = CodeableConcept().apply {
			text = "immunoglobulins"
			coding = listOf(
				snomed("333710000", "Product containing immunoglobulin (product)"),
				atc("J06B", "IMMUNGLOBULINE") //TODO: Add ATC codes for specific immunglobulines
			)
		}
		subject = patientRef
		effective = effectiveDt
	}

fun MediAntiCoag(patientRef: Reference, medi: AntiCoagulant, effectiveDt: DateTimeType, intent: TherapeuticIntent?) =
	MedicationStatement().apply {
		meta =
			Meta().addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/pharmacological-therapy")
		status = MedicationStatement.MedicationStatementStatus.ACTIVE
		medication = CodeableConcept().apply {
			coding = listOf(
				snomed("81839001", "Medicinal product acting as anticoagulant agent (product)"),
				medi.code
			)
		}
		subject = patientRef
		effective = effectiveDt
		//TODO: Intent fehlt im ORBIS-Formular
		if (intent != null) {
			reasonCode = listOf(intent.codeableConcept)
		}
	}

fun StudyInclusionDueToCovid19(patientRef: Reference, yesNoUnknown: YesNoUnknownOtherNa) = Observation().apply {
	meta =
		Meta().addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/study-inclusion-covid-19")
	category = listOf(ObservationCategory.SURVEY.codeableConcept)
	code = CodeableConcept().apply {
		coding = listOf(
			Coding(
				"https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/ecrf-parameter-codes",
				"02",
				"Study inclusion due to Covid-19"
			)
		)
		text = "Confirmed Covid-19 diagnosis as main reason for enrolment in the study"
	}
	status = Observation.ObservationStatus.FINAL
	subject = patientRef
	value = CodeableConcept().apply {
		coding = listOf(yesNoUnknown.coding)
		text = when (yesNoUnknown) {
			YesNoUnknownOtherNa.YES -> "Covid-19 as primary reason for study inclusion"
			YesNoUnknownOtherNa.NO -> "Covid-19 not the primary reason for study inclusion"
			YesNoUnknownOtherNa.UNKNOWN -> "Unknown if Covid-19 is primary reason for study inclusion"
			else -> null
		}
	}
}

fun ImagingProcedure(patientRef: Reference, imaging: Imaging, performed: DateTimeType) = Procedure().apply {
	meta =
		Meta().addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/radiology-procedures")
	status = Procedure.ProcedureStatus.COMPLETED
	category = CodeableConcept(snomed("103693007", "Diagnostic procedure (procedure)"))
	code = imaging.codeableConcept
	subject = patientRef
	setPerformed(performed)
	bodySite = listOf(CodeableConcept(snomed("39607008", "Lung structure (body structure)")))
}

fun ImagingFinding(patientRef: Reference, finding: RadiologicFindings) = DiagnosticReport().apply {
	meta =
		Meta().addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/diagnostic-report-radiology")
	status = DiagnosticReport.DiagnosticReportStatus.FINAL
	category = listOf(
		CodeableConcept(
			loinc(
				"18726-0",
				"Radiology studies (set)"
			)
		).addCoding(Coding("http://terminology.hl7.org/CodeSystem/v2-0074", "RAD", "Radiology"))
	)
	code = CodeableConcept(loinc("18748-4", "Diagnostic imaging study"))
	subject = patientRef
	conclusion = finding.codeableConcept.text
	conclusionCode = listOf(finding.codeableConcept)
}

fun InterventionalStudiesParticipation(
	patientRef: Reference,
	yesNoUnknown: YesNoUnknownOtherNa,
	eudraCT: String? = null,
	nct: String? = null
) = Observation().apply {
	meta =
		Meta().addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/interventional-clinical-trial-participation")
	category = listOf(ObservationCategory.SURVEY.codeableConcept)
	code = CodeableConcept().apply {
		coding = listOf(
			Coding(
				"https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/ecrf-parameter-codes",
				"03",
				"Participation in interventional clinical trials"
			)
		)
		text = "Has the patient participated in one or more interventional clinical trials?"
	}
	status = Observation.ObservationStatus.FINAL
	subject = patientRef
	value = CodeableConcept().apply {
		coding = listOf(yesNoUnknown.coding)
		text = when (yesNoUnknown) {
			YesNoUnknownOtherNa.YES -> "Patient is enrolled in other studies"
			YesNoUnknownOtherNa.NO -> "Patient is not enrolled in other studies"
			YesNoUnknownOtherNa.UNKNOWN -> "Unknown if patient is enrolled in other studies"
			YesNoUnknownOtherNa.NA -> "Not applicable"
			else -> null
		}
	}
	if (eudraCT != null) {
		addComponent(Observation.ObservationComponentComponent().apply {
			code = CodeableConcept().apply {
				coding = listOf(
					Coding(
						"https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/ecrf-parameter-codes",
						"04",
						"EudraCT Number"
					)
				)
				text = "EudraCT (European Union Drug Regulating Authorities Clinical Trials) registration number"
			}
			value = StringType(eudraCT)
		})
	}

	if (nct != null) {
		addComponent(Observation.ObservationComponentComponent().apply {
			code = CodeableConcept().apply {
				coding = listOf(
					Coding(
						"https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/ecrf-parameter-codes",
						"05",
						"NCT number"
					)
				)
				text = "A unique identification code given to each clinical study registered on ClinicalTrials.gov"
			}
			value = StringType(nct)
		})
	}
}