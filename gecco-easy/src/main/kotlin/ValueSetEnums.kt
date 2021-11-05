import org.hl7.fhir.r4.model.CodeableConcept
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Procedure
import org.hl7.fhir.r4.model.codesystems.ConditionClinical
import java.util.*

fun snomed(code: String, display: String) = Coding("http://snomed.info/sct", code, display)
fun loinc(code: String, display: String) = Coding("http://loinc.org", code, display)
fun icd10gm(code: String, display: String) = Coding("http://fhir.de/CodeSystem/dimdi/icd-10-gm", code, display)

//TODO: change to http://fhir.de/CodeSystem/bfarm/icd-10-gm
fun alpha_id(code: String, display: String) = Coding("http://fhir.de/CodeSystem/bfarm/alpha-id", code, display)
fun orphanet(code: String, display: String) = Coding("http://www.orpha.net", code, display)
fun atc(code: String, display: String) = Coding("http://fhir.de/CodeSystem/dimdi/atc", code, display)
fun dicom(code: String, display: String) = Coding("http://dicom.nema.org/resources/ontology/DCM", code, display)
fun iso3166_1_2(code: String, display: String) = Coding("urn:iso:std:iso:3166", code, display)
fun iso3155DE(code: String, display: String) = Coding("urn:iso:std:iso:3166-2:de", code, display)


interface CodeableEnum<T : Enum<T>> {
	val coding: Coding
}

inline fun <reified T> getByCoding(coding: Coding): T? where T : CodeableEnum<T>, T: Enum<T> {
	return enumValues<T>().find { it.coding.system ==  coding.system && it.coding.code == coding.code }
}

interface ConceptEnum<T: Enum<T>> {
	val codeableConcept: CodeableConcept
}

inline fun <reified T> getByCoding2(coding: Coding): T? where T : ConceptEnum<T>, T: Enum<T> {
	return enumValues<T>().find { it.codeableConcept.coding.any { it.system ==  coding.system && it.code == coding.code } }
}

enum class EthnicGroup(override val coding: Coding): CodeableEnum<EthnicGroup> {
	CAUCASIAN(snomed("14045001", "Caucasian (ethnic group)")),
	BLACK_AFRICAN(snomed("18167009", "Black African (ethnic group)")),
	ASIAN(snomed("315280000", "Asian - ethnic group (ethnic group)")),
	ARABS(snomed("90027003", "Arabs (ethnic group)")),
	OTHER(snomed("26242008", "Mixed (qualifier value)")),
	HISPANIC_OR_LATINO(Coding("urn:oid:2.16.840.1.113883.6.238", "2135-2", "Hispanic or Latino")),
}

enum class BirthSex(override val coding: Coding): CodeableEnum<EthnicGroup> {
	MALE(Coding("http://hl7.org/fhir/administrative-gender", "male", "Male")),
	FEMALE(Coding("http://hl7.org/fhir/administrative-gender", "female", "Female")),
//	OTHER(Coding("http://hl7.org/fhir/administrative-gender", "other", "Other")),
	UNKNOWN(Coding("http://hl7.org/fhir/administrative-gender", "unknown", "Unknown")),
	X(Coding("http://fhir.de/CodeSystem/gender-amtlich-de", "X", "unbestimmt")),
	D(Coding("http://fhir.de/CodeSystem/gender-amtlich-de", "D", "divers")),
}

private const val encounterAdditionsDE =
	"https://www.medizininformatik-initiative.de/fhir/core/CodeSystem/EncounterClassAdditionsDE"

enum class EncounterClasses(val coding: Coding) {
	BEGLEITPERSON(Coding(encounterAdditionsDE, "begleitperson", "Begleitperson")),
	VORSTAIONAER(Coding(encounterAdditionsDE, "vorstationaer", "Vorstationär")),
	NACHSTATIONAER(Coding(encounterAdditionsDE, "nachstationaer", "Nachstationär")),
	TEILSTATIONAER(Coding(encounterAdditionsDE, "teilstationaer", "Teilstationäre Behandlung")),
	NORMALSTATIONAER(Coding(encounterAdditionsDE, "normalstationaer", "Normalstationär")),
	INTENSIVSTATIONAER(Coding(encounterAdditionsDE, "intensivstationaer", "Intensivstationär")),
	TAGESKLINIK(Coding(encounterAdditionsDE, "tagesklinik", "Tagesklinische Behandlung")),
	UB(Coding(encounterAdditionsDE, "ub", "Untersuchung und Behandlung")),
	KONSIL(Coding(encounterAdditionsDE, "konsil", "Konsil")),
	STATIONSAEQUIVALENT(Coding(encounterAdditionsDE, "stationsaequivalent", "Stationsäquivalent")),
	OPERATION(Coding(encounterAdditionsDE, "operation", "Operation")),
	R(Coding("http://terminology.hl7.org/CodeSystem/v2-0004", "R", "Recurring patient")), //Wiederholungspatient
	B(Coding("http://terminology.hl7.org/CodeSystem/v2-0004", "B", "Obstetrics")), //Geburtshilfe
	IMP(Coding("http://terminology.hl7.org/CodeSystem/v3-ActCode", "IMP", "inpatient encounter")),
	AMB(Coding("http://terminology.hl7.org/CodeSystem/v3-ActCode", "AMB", "ambulatory")),
	EMER(Coding("http://terminology.hl7.org/CodeSystem/v3-ActCode", "EMER", "emergency")),
	FLD(Coding("http://terminology.hl7.org/CodeSystem/v3-ActCode", "FLD", "field")),
	HH(Coding("http://terminology.hl7.org/CodeSystem/v3-ActCode", "HH", "home health"))
}

//TODO: So kann das nur fuer SARS-COV2 benutzt werden.
enum class DetectedNotDetectedInconclusive(val codeableConcept: CodeableConcept, val displayDe: String) {
	DETECTED(CodeableConcept().apply {
		coding = listOf(snomed("260373001", "Detected (qualifier value)"))
		text = "SARS-CoV-2-RNA positiv"
	}, "positiv"),
	NOT_DETECTED(CodeableConcept().apply {
		coding = listOf(snomed("260415000", "Not detected (qualifier value)"))
		text = "SARS-CoV-2-RNA negativ"
	}, "negativ"),
	INCONCLUSIVE(CodeableConcept().apply {
		coding = listOf(snomed("419984006", "Inconclusive (qualifier value)"))
		text = "SARS-CoV-2-RNA nicht eindeutig"
	}, "nicht eindeutig"),
}

enum class ChronicLungDisease(val codeableConcept: CodeableConcept) {
	ASTHMA(CodeableConcept(snomed("195967001", "Asthma (disorder)"))),
	COPD(CodeableConcept(snomed("13645005", "Chronic obstructive lung disease (disorder)"))),
	FIBROSIS(CodeableConcept(snomed("51615001", "Fibrosis of lung (disorder)"))),
	PULMONARY_HYPERTENSION(CodeableConcept(snomed("70995007", "Pulmonary hypertension (disorder)"))),
	OHS(CodeableConcept(snomed("190966007", "Extreme obesity with alveolar hypoventilation (disorder)"))),
	SLEEP_APNEA(CodeableConcept(snomed("190966007", "Extreme obesity with alveolar hypoventilation (disorder)"))),
	OSAS(CodeableConcept(snomed("190966007", "Obstructive sleep apnea syndrome (disorder)"))),
	CYSTIC_FIBROSIS(CodeableConcept(snomed("190905008", "Cystic fibrosis (disorder)"))),
}

enum class CardiovascularDiseases(val codeableConcept: CodeableConcept) {
	HYPERTENSIVE_DISORDER_SYSTEMIC_ARTERIAL(
		CodeableConcept(
			snomed(
				"38341003",
				"Hypertensive disorder, systemic arterial (disorder)"
			)
		)
	),
	ZUSTAND_NACH_HERZINFARKT(
		CodeableConcept(
			snomed(
				"417662000:246090004=22298006",
				"|History of clinical finding in subject (situation)|:|Associated finding (attribute)|=|Myocardial infarction (disorder)|"
			)
		)
	),
	CARDIAC_ARRHYTHMIA(CodeableConcept(snomed("698247007", "Cardiac arrhythmia (disorder)"))),
	HEART_FAILURE(CodeableConcept(snomed("84114007", "Heart failure (disorder)"))),
	PERIPHERAL_ARTERIAL_OCCLUSIVE_DISEASE(
		CodeableConcept(
			snomed(
				"399957001",
				"Peripheral arterial occlusive disease (disorder)"
			)
		)
	),
	ZUSTAND_NACH_REVASKULARISATION(
		CodeableConcept(
			snomed(
				"416940007:363589002=81266008",
				"|Past history of procedure (situation)|:|Associated procedure (attribute)|=|Heart revascularization (procedure)|"
			)
		)
	),
	CORONARY_ARTERIOSCLEROSIS(CodeableConcept(snomed("53741008", "Coronary arteriosclerosis (disorder)"))),
	CAROTID_ARTERY_STENOSIS(CodeableConcept(snomed("64586002", "Carotid artery stenosis (disorder)"))),
}

enum class ChronicLiverDiseases(val codeableConcept: CodeableConcept) {
	STEATOSIS_OF_LIVER(CodeableConcept(snomed("197321007", "Steatosis of liver (disorder)"))),
	CIRRHOSIS_OF_LIVER(CodeableConcept(snomed("19943007", "Cirrhosis of liver (disorder)"))),
	CHRONIC_VIRAL_HEPATITIS(CodeableConcept(snomed("10295004", "Chronic viral hepatitis (disorder)"))),
	AUTOIMMUNE_LIVER_DISEASE(CodeableConcept(snomed("235890007", "Autoimmune liver disease (disorder)"))),
}

enum class RheumatologicalImmunologicalDiseases(val codeableConcept: CodeableConcept) {
	INFLAMMATORY_BOWEL_DISEASE(CodeableConcept(snomed("24526004", "Inflammatory bowel disease (disorder)"))),
	RHEUMATOID_ARTHRITIS(CodeableConcept(snomed("69896004", "Rheumatoid arthritis (disorder)"))),
	COLLAGENOSIS(CodeableConcept(snomed("105969002", "Disorder of connective tissue (disorder)"))),
	VASCULITIS(CodeableConcept(snomed("31996006", "Vasculitis (disorder)"))),
	CONGENITAL_IMMUNODEFICIENCY_DISEASE(
		CodeableConcept(
			snomed(
				"36138009",
				"Congenital immunodeficiency disease (disorder)"
			)
		)
	),
}

enum class ChronicNeurologicalMentalDisease(val codeableConcept: CodeableConcept) {
	CHRONIC_NERVOUS_SYSTEM_DISORDER(CodeableConcept(snomed("128283000", "Chronic nervous system disorder (disorder)"))),
	MENTAL_DISORDER(CodeableConcept(snomed("74732009", "Mental disorder (disorder)"))),
	ANXIETY_DISORDER(CodeableConcept(snomed("197480006", "Anxiety disorder (disorder)"))),
	DEPRESSIVE_DISORDER(CodeableConcept(snomed("35489007", "Depressive disorder (disorder)"))),
	PSYCHOTIC_DISORDER(CodeableConcept(snomed("69322001", "Psychotic disorder (disorder)"))),
	PARKINSONS_DISEASE(CodeableConcept(snomed("49049000", "Parkinson's disease (disorder)"))),
	DEMENTIA(CodeableConcept(snomed("52448006", "Dementia (disorder)"))),
	MULTIPLE_SCLEROSIS(CodeableConcept(snomed("24700007", "Multiple sclerosis (disorder)"))),
	COMBINED_DISORDER_OF_MUSCLE_AND_PERIPHERAL_NERVE(
		CodeableConcept(
			snomed(
				"257277002",
				"Combined disorder of muscle AND peripheral nerve (disorder)"
			)
		)
	),
	EPILEPSY(CodeableConcept(snomed("84757009", "Epilepsy (disorder)"))),
	MIGRAINE(CodeableConcept(snomed("37796009", "Migraine (disorder)"))),
	HISTORY_OF_CEREBROVASCULAR_ACCIDENT_WITH_RESIDUAL_DEFICIT(
		CodeableConcept(
			snomed(
				"440140008",
				"History of cerebrovascular accident with residual deficit (situation)"
			)
		)
	),
	HISTORY_OF_CEREBROVASCULAR_ACCIDENT_WITHOUT_RESIDUAL_DEFICITS(
		CodeableConcept(
			snomed(
				"429993008",
				"History of cerebrovascular accident without residual deficits (situation)"
			)
		)
	),
}

enum class ChronicKidneyDisease(val codeableConcept: CodeableConcept, val displayDe: String) {
	CHRONIC_KIDNEY_DISEASE_STAGE_1(CodeableConcept().apply {
		coding = listOf(snomed("431855005", "Chronic kidney disease stage 1 (disorder)"))
	}, displayDe = "Ja, Stadium 1"),
	CHRONIC_KIDNEY_DISEASE_STAGE_2(CodeableConcept().apply {
		coding = listOf(snomed("431856006", "Chronic kidney disease stage 2 (disorder)"))
	}, displayDe = "Ja, Stadium 2"),
	CHRONIC_KIDNEY_DISEASE_STAGE_3(CodeableConcept().apply {
		coding = listOf(snomed("433144002", "Chronic kidney disease stage 3 (disorder)"))
	}, displayDe = "Ja, Stadium 3"),
	CHRONIC_KIDNEY_DISEASE_STAGE_4(CodeableConcept().apply {
		coding = listOf(snomed("431857002", "Chronic kidney disease stage 4 (disorder)"))
	}, displayDe = "Ja, Stadium 4"),
	CHRONIC_KIDNEY_DISEASE_STAGE_5(CodeableConcept().apply {
		coding = listOf(snomed("433146000", "Chronic kidney disease stage 5 (disorder)"))
	}, displayDe = "Ja, Stadium 5 ohne Dialyse"),
	CHRONIC_KIDNEY_DISEASE_STAGE_5_ON_DIALYSIS(CodeableConcept().apply {
		coding = listOf(snomed("714152005", "Chronic kidney disease stage 5 on dialysis (disorder)"))
	}, displayDe = "Ja, Stadium 5 mit Dialyse"),
	CHRONIC_KIDNEY_DISEASE(CodeableConcept().apply {
		coding = listOf(snomed("709044004", "Chronic kidney disease (disorder)"))
	},displayDe = "Ja, Stadium unbekannt"),
	UNKNOWN(CodeableConcept(YesNoUnknown.UNKNOWN.coding), displayDe = "Unbekannt"),
	ABSENT(CodeableConcept(YesNoUnknown.NO.coding), displayDe = "Nein");
	//TODO: Add ICD-10

	companion object {
		fun from(findValue: CodeableConcept): ChronicKidneyDisease? = values()
			.firstOrNull{ value -> value.codeableConcept.coding.any{ coding -> findValue.hasCoding(coding.system, coding.code) } }
	}
}

enum class Imaging(val codeableConcept: CodeableConcept) {
	CT(CodeableConcept().apply {
		coding = listOf(
			snomed("77477000", "Computerized axial tomography (procedure)"),
			dicom("CT", "Computed Tomography"),
		)
		text = "Computed Tomography"
	}),
	XRAY(CodeableConcept().apply {
		coding = listOf(
			snomed("168537006", "Plain radiography"),
			dicom("RG", "Radiographic imaging"),
		)
		text = "Chest x-ray"
	}),
	ULTRASOUND(CodeableConcept().apply {
		coding = listOf(
			snomed("16310003", "Diagnostic ultrasonography (procedure)"),
			dicom("US", "Ultrasound")
		)
		text = "Ultrasound"
	})
}


enum class RadiologicFindings(val codeableConcept: CodeableConcept) {
	UNSPECIFIC(CodeableConcept().apply {
		coding = listOf(
			snomed(
				"118247008:363713009=373068000",
				"|Radiologic finding (finding)|:|Has interpretation (attribute)|=|Undetermined (qualifier value)|"
			)
		)
		text = "Unspezifischer Befund"
	}),
	COVID19(CodeableConcept().apply {
		coding = listOf(
			snomed(
				"118247008:{363713009=263654008,42752001=840539006}",
				"|Radiologic finding (finding)|:{|Has interpretation (attribute)|=|Abnormal (qualifier value)|,|Due to (attribute)|=|Disease caused by severe acute respiratory syndrome coronavirus 2 (disorder)|}"
			)
		)
		text = "COVID-19-typischer Befund"
	}),
	NORMAL(CodeableConcept().apply {
		coding = listOf(
			snomed(
				"118247008:363713009=17621005",
				"|Radiologic finding (finding)|:|Has interpretation (attribute)|=|Normal (qualifier value)|"
			)
		)
		text = "Normalbefund"
	});

	companion object {
		fun from(findValue: CodeableConcept): RadiologicFindings? = RadiologicFindings.values()
			.firstOrNull { value ->
				value.codeableConcept.coding.any { coding ->
					findValue.hasCoding(
						coding.system,
						coding.code
					)
				}
			}
	}

}

fun frailty_score(code: String, display: String) = Coding(
	"https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/frailty-score", code, display
)

enum class FrailityScore(override val codeableConcept: CodeableConcept) : ConceptEnum<FrailityScore> {
	VERY_FIT(CodeableConcept(frailty_score("1", "Very Fit"))),
	WELL(CodeableConcept(frailty_score("2", "Well"))),
	MANAGING_WELL(CodeableConcept(frailty_score("3", "Managing Well"))),
	VULNERABLE(CodeableConcept(frailty_score("4", "Vulnerable"))),
	MILDLY_FRAIL(CodeableConcept(frailty_score("5", "Mildly Frail"))),
	MODERATELY_FRAIL(CodeableConcept(frailty_score("6", "Moderately Frail"))),
	SEVERELY_FRAIL(CodeableConcept(frailty_score("7", "Severely Frail"))),
	VERY_SEVERELY_FRAIL(CodeableConcept(frailty_score("8", "Very Severely Frail"))),
	TERMINALLY_ILL(CodeableConcept(frailty_score("9", "Terminally Ill"))),
}


/**
 * Streng genommen kein ValueSet
 */
enum class ObservationCategory(val codeableConcept: CodeableConcept) {
	VITAL_SIGNS(CodeableConcept(org.hl7.fhir.r4.model.codesystems.ObservationCategory.VITALSIGNS.toCoding())),
	LAB(CodeableConcept().apply {
		coding = listOf(
			loinc("26436-6", "Laboratory studies (set)"),
			org.hl7.fhir.r4.model.codesystems.ObservationCategory.LABORATORY.toCoding()
		)
	}),
	SOCIAL_HISTORY(CodeableConcept(org.hl7.fhir.r4.model.codesystems.ObservationCategory.SOCIALHISTORY.toCoding())),
	SURVEY(CodeableConcept(org.hl7.fhir.r4.model.codesystems.ObservationCategory.SURVEY.toCoding()))

}

fun org.hl7.fhir.r4.model.codesystems.ObservationCategory.toCoding() =
	Coding(this.system, this.toCode(), this.display)


enum class PregnancyStatus(override val codeableConcept: CodeableConcept): ConceptEnum<PregnancyStatus> {
	NOT_PREGNANT(CodeableConcept().apply {
		coding = listOf(
			loinc("LA26683-5", "Not pregnant"),
			snomed("60001007", "Not pregnant (finding)")
		)
	}),
	PREGNANT(CodeableConcept().apply {
		coding = listOf(
			loinc("LA15173-0", "Pregnant"),
			snomed("77386006", "Pregnant (finding)")
		)
	}),
	UNKNOWN(CodeableConcept().apply {
		coding = listOf(
			loinc("LA4489-6", "Unknown"),
			snomed("261665006", "Unknown (qualifier value)")
		)
	})
}


enum class KnownExposureVS(val codeableConcept: CodeableConcept) {
	YES(CodeableConcept(snomed("840546002", "Exposure to severe acute respiratory syndrome coronavirus 2 (event)"))),
	NO(CodeableConcept(snomed("373067005", "No (qualifier value)")))
}

enum class SmokingStatus(override val codeableConcept: CodeableConcept): ConceptEnum<SmokingStatus> {
	SMOKER(CodeableConcept(loinc("LA18976-3", "Current every day smoker"))),
	FORMER_SMOKER(CodeableConcept(loinc("LA15920-4", "Former smoker"))),
	NEVER(CodeableConcept(loinc("LA18978-9", "Never smoker"))),
	UNKNOWN(CodeableConcept(loinc("LA18980-5", "Unknown if ever smoked")))
}


enum class ConditionVerificationStatus(val codeableConcept: CodeableConcept) {
	VERIFIED(CodeableConcept().apply {
		coding = listOf(
			org.hl7.fhir.r4.model.codesystems.ConditionVerStatus.CONFIRMED.let {
				Coding(it.system, it.toCode(), it.display)
			},
			Coding("http://snomed.info/sct", "410605003", "Confirmed present (qualifier value)")
		)
	}),
	REFUTED(CodeableConcept().apply {
		coding = listOf(
			org.hl7.fhir.r4.model.codesystems.ConditionVerStatus.REFUTED.let {
				Coding(it.system, it.toCode(), it.display)
			},
			snomed("410594000", "Definitely NOT present (qualifier value)")
		)
	})
}

enum class ComplicationsCovid19(val codeableConcept: CodeableConcept) {
	THROMBOSIS(CodeableConcept(snomed("439127006", "Thrombosis (disorder)"))),
	EMBOLISM(CodeableConcept(snomed("414086009", "Embolism (disorder)"))),
	INFECTIOUS_DISEASE_OF_LUNG(CodeableConcept(snomed("128601007", "Infectious disease of lung (disorder)"))),
	INFECTIOUS_AGENT_IN_BLOODSTREAM(CodeableConcept(snomed("434156008", "Infectious agent in bloodstream (finding)"))),
	VENOUS_THROMBOSIS(CodeableConcept(snomed("111293003", "Venous thrombosis (disorder)"))),
	PULMONARY_EMBOLISM(CodeableConcept(snomed("59282003", "Pulmonary embolism (disorder)"))),
	CEREBROVASCULAR_ACCIDENT(CodeableConcept(snomed("230690007", "Cerebrovascular accident (disorder)"))),
	MYOCARDIAL_INFARCTION(CodeableConcept(snomed("22298006", "Myocardial infarction (disorder)"))),
	PRE_RENAL_ACUTE_KINDEY_INJURY(
		CodeableConcept(
			snomed(
				"129561000119108",
				"Pre-renal acute kidney injury (disorder)"
			)
		)
	),
	//TODO: Add also ICD10 codes?
}

/**
 * See also https://github.com/hl7germany/forschungsnetz-covid19/issues/98
 */
enum class StageAtDiagnosis(override val codeableConcept: CodeableConcept): ConceptEnum<StageAtDiagnosis> {
	UNCOMPLICATED(CodeableConcept().apply {
		coding = listOf(
			snomed("255604002", "Mild (qualifier value)"),
//			snomed("371923003", "Mild to moderate (qualifier value)")
		)
		text = "Uncomplicated phase"
	}),
	COMPLICATED(CodeableConcept().apply {
		coding = listOf(
			snomed("6736007", "Moderate (severity modifier) (qualifier value)"),
//			snomed("371924009", "Moderate to severe (qualifier value)")
		)
		text = "Complicated phase"
	}),
	CRITICAL(CodeableConcept().apply {
		coding = listOf(
			snomed("24484000", "Severe (severity modifier) (qualifier value)"),
//			snomed("442452003", "Life threatening severity (qualifier value)")
		)
		text = "Critical phase"
	}),
	RECOVERY(CodeableConcept().apply {
		coding = listOf(snomed("277022003", "Remission phase (qualifier value)"))
		text = "Recovery phase"
	}),
	DEAD(CodeableConcept().apply {
		coding = listOf(snomed("399166001", "Fatal (qualifier value)"))
		text = "Dead"
	}),
	UNKNOWN(CodeableConcept().apply {
		coding = listOf(snomed("261665006", "Unknown (qualifier value)"))
		text = "Unknown"
	})
}

class LabCodes {
	enum class CRP(override val coding: Coding, val unit: GeccoUnits): CodeableEnum<CRP> {
		MASS_PER_VOLUME_IN_CAPILLARY_BLOOD(loinc("48421-2", "C reactive protein [Mass/volume] in Capillary blood"), GeccoUnits.MICROGRAM_PER_LITER),
		MASS_PER_VOLUME_IN_BLOOD_HIGH_SENSITIVITY(loinc("71426-1", "C reactive protein [Mass/volume] in Blood by High sensitivity method"), GeccoUnits.MILLIGRAM_PER_LITER),
		MOLES_PER_VOLUME_IN_SERUM_OR_PLASMA_HIGH_SENSITIVITY(
			loinc("76486-0", "C reactive protein [Moles/volume] in Serum or Plasma by High sensitivity method"), GeccoUnits.NANOMOLE_PER_LITER),
		MASS_PER_VOLUME_IN_SERUM_OR_PLASMA_HIGH_SENSITIVITY(loinc("30522-7", "C reactive protein [Mass/volume] in Serum or Plasma by High sensitivity method"), GeccoUnits.MILLIGRAM_PER_LITER),
		MASS_PER_VOLUME_IN_SERUM_OR_PLASMA(loinc("1988-5", "C reactive protein [Mass/volume] in Serum or Plasma"), GeccoUnits.MILLIGRAM_PER_LITER),
		MOLES_PER_VOLUME_IN_SERUM_OR_PLASMA(loinc("76485-2", "C reactive protein [Moles/volume] in Serum or Plasma"), GeccoUnits.NANOMOLE_PER_LITER)
	}

	enum class Ferritin(override val coding: Coding, val unit: GeccoUnits): CodeableEnum<Ferritin> {
		MASS_PER_VOLUME_IN_SERUM_OR_PLASMA_BY_IMMUNOASSAY(
			loinc("20567-4", "Ferritin [Mass/volume] in Serum or Plasma by Immunoassay"), GeccoUnits.NANOGRAM_PER_MILLILITER),
		GOAL_SERUM_OR_PLASMA(loinc("86914-9", "Ferritin goal [Mass/volume] Serum or Plasma"), GeccoUnits.NANOGRAM_PER_MILLILITER),
		IN_SERUM_OR_PLASMA(loinc("14723-1", "Ferritin [Moles/volume] in Serum or Plasma"), GeccoUnits.PICOMOLE_PER_LITER),
		IN_SERUM_OR_PLASMA_MASS(loinc("2276-4", "Ferritin [Mass/volume] in Serum or Plasma"), GeccoUnits.NANOGRAM_PER_MILLILITER),
		IN_BLOOD_MASS(loinc("24373-3", "Ferritin [Mass/volume] in Blood"), GeccoUnits.NANOGRAM_PER_MILLILITER),
	}

	enum class Bilirubin(override val coding: Coding, val unit: GeccoUnits): CodeableEnum<Bilirubin> {
		INDIRECT_MASS_PER_VOLUME_IN_SERUM_OR_PLASMA(
			loinc("1971-1", "Bilirubin.indirect [Mass/volume] in Serum or Plasma"), GeccoUnits.MILLIGRAM_PER_DECILITER),
		TOTAL_MASS_PER_VOLUME_IN_SERUM_OR_PLASMA(
			loinc("1975-2", "Bilirubin.total [Mass/volume] in Serum or Plasma"), GeccoUnits.MILLIGRAM_PER_DECILITER),
		TOTAL_MOLES_PER_VOLUME_IN_SERUM_OR_PLASMA(
			loinc("14631-6", "Bilirubin.total [Moles/volume] in Serum or Plasma"), GeccoUnits.MICROMOLE_PER_LITER),
		TOTAL_MASS_PER_VOLUME_IN_VENOUS_BLOOD(loinc("59828-4", "Bilirubin.total [Mass/volume] in Venous blood"), GeccoUnits.MILLIGRAM_PER_DECILITER),
		TOTAL_MASS_PER_VOLUME_IN_ARTERIAL_BLOOD(
			loinc("59827-6", "Bilirubin.total [Mass/volume] in Arterial blood"), GeccoUnits.MILLIGRAM_PER_DECILITER),
		TOTAL_MASS_PER_VOLUME_IN_BLOOD(loinc("42719-5", "Bilirubin.total [Mass/volume] in Blood"), GeccoUnits.MILLIGRAM_PER_DECILITER),
	}

	object DDimer {
		val FEU_MASS_PER_VOLUME_IN_PLATELET_POOR_PLASMA_BY_IMMUNOASSAY =
			loinc("48067-3", "Fibrin D-dimer FEU [Mass/volume] in Platelet poor plasma by Immunoassay")

		object ShortPanel {
			val SHORT_FEU_AND_DDU_PANEL_IN_PLATETLET_POOR_PLASMA =
				loinc("55398-2", "Short Fibrin D-dimer FEU and DDU panel - Platelet poor plasma")
			val FEU_MASS_PER_VOLUME_IN_PLATELET_POOR_PLASMA =
				loinc("48065-7", "Fibrin D-dimer FEU [Mass/volume] in Platelet poor plasma")
			val DDU_MASS_PER_VOLUME_IN_PLATELET_POOR_PLASMA =
				loinc("48066-5", "Fibrin D-dimer DDU [Mass/volume] in Platelet poor plasma")
		}

		val FEU_MASS_PER_VOLUME_IN_BLOOD_BY_IMMUNOASSAY =
			loinc("71427-9", "Fibrin D-dimer FEU [Mass/volume] in Blood by Immunoassay")
		val UNITS_PER_VOLUME_IN_PLATELET_POOR_PLASMA =
			loinc("7799-0", "Fibrin D-dimer [Units/volume] in Platelet poor plasma")
		val DDU_MASS_PER_VOLUME_IN_BLOOD_BY_IMMUNOASSAY =
			loinc("91556-1", "Fibrin D-dimer DDU [Mass/volume] in Blood by Immunoassay")
		val TITER_IN_PLATELET_POOR_PLASMA = loinc("38898-3", "Fibrin D-dimer [Titer] in Platelet poor plasma")
		val UNITS_PER_VOLUME_IN_PLATELET_POOR_PLASMA_BY_IMMUNOASSAY =
			loinc("3246-6", "Fibrin D-dimer [Units/volume] in Platelet poor plasma by Immunoassay")
		val DDU_MASS_PERVOLUME_IN_PLATELET_POOR_PLASMA_BY_IMMUNOASSAY =
			loinc("48058-2", "Fibrin D-dimer DDU [Mass/volume] in Platelet poor plasma by Immunoassay")
	}

	object GammaGlutamylTransferateAndAspartateAminotransferase {
		val GAMMA_GLUTAMYL_TRANSFERASE_OR_ASPARTATE_AMINOTRANSFERASE = loinc(
			"2325-9",
			"Gamma glutamyl transferase/Aspartate aminotransferase [Enzymatic activity ratio] in Serum or Plasma"
		)

		object GammaGlutamylTransferase {
			val ENZYMATIC_ACTIVITY_PER_VOLUME =
				loinc("2324-2", "Gamma glutamyl transferase [Enzymatic activity/volume] in Serum or Plasma")
		}

		enum class AspartateAminotransferase (override val coding: Coding): CodeableEnum<AspartateAminotransferase>{
			ENZYMATIC_ACTIVITY_PER_VOLUME_IN_SERUM_OR_PLASMA(
				loinc("1920-8", "Aspartate aminotransferase [Enzymatic activity/volume] in Serum or Plasma")),
			ENZYMATIC_ACTIVITY_PER_VOLUME_IN_SERUM_OR_PLASMA_WITH(loinc(
				"30239-8",
				"Aspartate aminotransferase [Enzymatic activity/volume] in Serum or Plasma by With P-5'-P"
			)),
			ENZYMATIC_ACTIVITY_PER_VOLUME_IN_SERUM_OR_PLASMA_WITHOUT(loinc(
				"88112-8",
				"Aspartate aminotransferase [Enzymatic activity/volume] in Serum or Plasma by No addition of P-5'-P"
			))
		}
	}

	enum class LactateDehydrogenase(override val coding: Coding, val unit: GeccoUnits): CodeableEnum<LactateDehydrogenase> {
		ENZYMATIC_ACTIVITY_PER_VOLUME_IN_SERUM_OR_PLASMA(
			loinc("2532-0", "Lactate dehydrogenase [Enzymatic activity/volume] in Serum or Plasma"), GeccoUnits.ENZYME_UNIT_PER_LITER),
		ENZYMATIC_ACTIVITY_PER_VOLUME_IN_SERUM_OR_PLASMA_BY_PYRUVATE_TO_LACTATE_REACTION(loinc(
			"14805-6",
			"Lactate dehydrogenase [Enzymatic activity/volume] in Serum or Plasma by Pyruvate to lactate reaction"
		), GeccoUnits.ENZYME_UNIT_PER_LITER),
		ENZYMATIC_ACTIVITY_PER_VOLUME_IN_SERUM_OR_PLASMA_BY_LACTATE_TO_PYRUVATE_REACTION(loinc(
			"14804-9",
			"Lactate dehydrogenase [Enzymatic activity/volume] in Serum or Plasma by Lactate to pyruvate reaction"
		),  GeccoUnits.ENZYME_UNIT_PER_LITER),
		ENZYMATIC_ACTIVITY_PER_VOLUME_IN_BODY_FLUID_BY_LACTATE_TO_PYRUVATE_REACTION(loinc(
			"14803-1",
			"Lactate dehydrogenase [Enzymatic activity/volume] in Body fluid by Lactate to pyruvate reaction"
		), GeccoUnits.ENZYME_UNIT_PER_LITER),
		ENZYMATIC_ACTIVITY_PER_VOLUME_IN_BODY_FLUID_BY_PYRUVATE_TO_LACTATE_REACTION(loinc(
			"60017-1",
			"Lactate dehydrogenase [Enzymatic activity/volume] in Body fluid by Pyruvate to lactate reaction"
		), GeccoUnits.ENZYME_UNIT_PER_LITER)
	}

	object Troponin {
		object TCardiac {
			enum class MassPerVolume(override val coding: Coding, val unit: GeccoUnits? = null): CodeableEnum<MassPerVolume> {
				IN_SERUM_OR_PLASMA_BY_HIGH_SENSITIVITY_METHOD(
					loinc("67151-1", "Troponin T.cardiac [Mass/volume] in Serum or Plasma by High sensitivity method"), GeccoUnits.NANOGRAM_PER_LITER),
				//Todo: in the loinc table the unit is ug/L;ng/mL... what should we use here?
				IN_SERUM_OR_PLASMA(loinc("6598-7", "Troponin T.cardiac [Mass/volume] in Serum or Plasma"), GeccoUnits.MICROGRAM_PER_LITER),
				IN_VENOUS_BLOOD(loinc("6597-9", "Troponin T.cardiac [Mass/volume] in Venous blood"), GeccoUnits.MICROGRAM_PER_LITER),
				IN_BLOOD(loinc("48425-3", "Troponin T.cardiac [Mass/volume] in Blood"), GeccoUnits.MICROGRAM_PER_LITER)
			}
		}

		object ICardiac {
			enum class MassPerVolume(override val coding: Coding, val unit: GeccoUnits? = null): CodeableEnum<MassPerVolume> {
				IN_SERUM_OR_PLASMA_BY_DETECTION_LIMIT_LOWER_THAN_0_01_ng_PER_mL(
					loinc(
						"49563-0",
						"Troponin I.cardiac [Mass/volume] in Serum or Plasma by Detection limit <= 0.01 ng/mL"
					), GeccoUnits.NANOGRAM_PER_MILLILITER),
				IN_SERUM_OR_PLASMA_BY_HIGH_SENSITIVITY_METHOD(
					loinc("89579-7", "Troponin I.cardiac [Mass/volume] in Serum or Plasma by High sensitivity method"), GeccoUnits.NANOGRAM_PER_LITER),
				IN_SERUM_OR_PLASMA(loinc("10839-9", "Troponin I.cardiac [Mass/volume] in Serum or Plasma"), GeccoUnits.NANOGRAM_PER_MILLILITER),
				IN_BLOOD(loinc("42757-5", "Troponin I.cardiac [Mass/volume] in Blood"), GeccoUnits.NANOGRAM_PER_MILLILITER)
			}

		}

	}

	object Hemoglobin {
		enum class MassPerVolume(override val coding: Coding, val unit: GeccoUnits? = null): CodeableEnum<MassPerVolume>  {
			IN_VENOUS_BLOOD_BY_OXIMETRY(loinc("76769-9", "Hemoglobin [Mass/volume] in Venous blood by Oximetry"), GeccoUnits.GRAM_PER_LITER),
			IN_BLOOD(loinc("718-7", "Hemoglobin [Mass/volume] in Blood"), GeccoUnits.GRAM_PER_DECILITER),
			IN_BLOOD_BY_CALCULATION(loinc("20509-6", "Hemoglobin [Mass/volume] in Blood by calculation"), GeccoUnits.GRAM_PER_DECILITER),
			IN_BLOOD_BY_OXIMETRY(loinc("55782-7", "Hemoglobin [Mass/volume] in Blood by Oximetry"), GeccoUnits.GRAM_PER_DECILITER),
			IN_ARTERIAL_BLOOD_BY_OXIMETRY(loinc("14775-1", "Hemoglobin [Mass/volume] in Arterial blood by Oximetry"),GeccoUnits.GRAM_PER_DECILITER),
			IN_ARTERIAL_BLOOD(loinc("30313-1", "Hemoglobin [Mass/volume] in Arterial blood"), GeccoUnits.GRAM_PER_DECILITER),
			IN_VENOUS_BLOOD(loinc("30350-3", "Hemoglobin [Mass/volume] in Venous blood"), GeccoUnits.GRAM_PER_DECILITER),
			IN_MIXED_VENOUS_BLOOD(loinc("30351-1", "Hemoglobin [Mass/volume] in Mixed venous blood"), GeccoUnits.GRAM_PER_DECILITER),
			IN_CAPILLARY_BLOOD(loinc("30352-9", "Hemoglobin [Mass/volume] in Capillary blood"), GeccoUnits.GRAM_PER_DECILITER),
			IN_MIXED_VENOUS_BLOOD_BY_OXIMETRY(loinc("76768-1", "Hemoglobin [Mass/volume] in Mixed venous blood by Oximetry"), GeccoUnits.GRAM_PER_LITER)
		}

		enum class MolesPerVolume(override val coding: Coding, val unit: GeccoUnits? = null): CodeableEnum<MolesPerVolume>  {
			IN_BLOOD(loinc("59260-0", "Hemoglobin [Moles/volume] in Blood"), GeccoUnits.MILLIMOLE_PER_LITER),
			IN_ARTERIAL_BLOOD(loinc("75928-2", "Hemoglobin [Moles/volume] in Arterial blood"), GeccoUnits.MILLIMOLE_PER_LITER)
		}
	}

	object Creatinine {
		enum class MassPerVolume(override val coding: Coding, val unit: GeccoUnits? = null): CodeableEnum<MassPerVolume>  {
			IN_ARTERIAL_BLOOD(loinc("21232-4", "Creatinine [Mass/volume] in Arterial blood"), GeccoUnits.MILLIGRAM_PER_DECILITER),
			IN_BLOOD(loinc("38483-4", "Creatinine [Mass/volume] in Blood"), GeccoUnits.MILLIGRAM_PER_DECILITER),
			IN_BODY_FLUID(loinc("12190-5", "Creatinine [Mass/volume] in Body fluid"), GeccoUnits.MILLIGRAM_PER_DECILITER),
			IN_SERUM_OR_PLASMA(loinc("2160-0", "Creatinine [Mass/volume] in Serum or Plasma"), GeccoUnits.MILLIGRAM_PER_DECILITER)
		}

		enum class MolesPerVolume(override val coding: Coding, val unit: GeccoUnits? = null): CodeableEnum<MolesPerVolume> {
			IN_BLOOD(loinc("59826-8", "Creatinine [Moles/volume] in Blood"), GeccoUnits.MICROMOLE_PER_LITER),
			IN_BODY_FLUID(loinc("25386-4", "Creatinine [Moles/volume] in Body fluid"), GeccoUnits.MICROMOLE_PER_LITER),
			IN_SERUM_OR_PLASMA(loinc("14682-9", "Creatinine [Moles/volume] in Serum or Plasma"), GeccoUnits.MICROMOLE_PER_LITER)
		}
	}

	object Lactate {
		enum class MassPerVolume(override val coding: Coding, val unit: GeccoUnits? = null): CodeableEnum<MassPerVolume> {
			IN_SERUM_OR_PLASMA(loinc("14118-4", "Lactate [Mass/volume] in Serum or Plasma"), GeccoUnits.MILLIGRAM_PER_DECILITER),
			IN_BLOOD(loinc("59032-3", "Lactate [Mass/volume] in Blood"), GeccoUnits.MILLIGRAM_PER_DECILITER),
			IN_ARTERIAL_BLOOD(loinc("30242-2", "Lactate [Mass/volume] in Arterial blood"), GeccoUnits.MILLIGRAM_PER_DECILITER),
			IN_CEREBRAL_SPINE_FLUID(loinc("27941-4", "Lactate [Mass/volume] in Cerebral spinal fluid"), GeccoUnits.MILLIGRAM_PER_DECILITER)
		}

		enum class MolesPerVolume(override val coding: Coding, val unit: GeccoUnits? = null): CodeableEnum<MolesPerVolume> {
			IN_BLOOD(loinc("32693-4", "Lactate [Moles/volume] in Blood"), GeccoUnits.MILLIMOLE_PER_LITER),
			IN_CEREBRAL_SPINE_FLUID(loinc("2520-5", "Lactate [Moles/volume] in Cerebral spinal fluid"), GeccoUnits.MILLIMOLE_PER_LITER),
			IN_CAPILLARY_BLOOD(loinc("19239-3", "Lactate [Moles/volume] in Capillary blood"), GeccoUnits.MILLIMOLE_PER_LITER),
			IN_MIXED_VENOUS_BLOOD(loinc("19240-1", "Lactate [Moles/volume] in Mixed venous blood"), GeccoUnits.MILLIMOLE_PER_LITER),
			IN_ARTERIAL_BLOOD(loinc("2518-9", "Lactate [Moles/volume] in Arterial blood"), GeccoUnits.MILLIMOLE_PER_LITER),
			IN_VENOUS_BLOOD(loinc("2519-7", "Lactate [Moles/volume] in Venous blood"), GeccoUnits.MILLIMOLE_PER_LITER),
			IN_SERUM_OR_PLASMA(loinc("2524-7", "Lactate [Moles/volume] in Serum or Plasma"), GeccoUnits.MILLIMOLE_PER_LITER),
		}
	}

	object Leukocytes {
		enum class CountPerVolume(override val coding: Coding, val unit: GeccoUnits? = null): CodeableEnum<CountPerVolume> {
			IN_BLOOD_BY_ESTIMATE(loinc("49498-9", "Leukocytes [#/volume] in Blood by Estimate"), GeccoUnits.THOUSANDS_PER_MICROLITER),
			IN_BLOOD_BY_AUTOMATED_COUNT(loinc("6690-2", "Leukocytes [#/volume] in Blood by Automated count"), GeccoUnits.THOUSANDS_PER_MICROLITER),
			IN_BLOOD_BY_MANUAL_COUNT(loinc("804-5", "Leukocytes [#/volume] in Blood by Manual count"), GeccoUnits.THOUSANDS_PER_MICROLITER),
			IN_BLOOD(loinc("26464-8", "Leukocytes [#/volume] in Blood"), GeccoUnits.THOUSANDS_PER_MICROLITER),
		}
	}

	object Lymphocytes {
		enum class CountPerVolume(override val coding: Coding, val unit: GeccoUnits? = null): CodeableEnum<Bilirubin> {
			IN_BLOOD_BY_MANUAL_COUNT(loinc("732-8", "Lymphocytes [#/volume] in Blood by Manual count"), GeccoUnits.THOUSANDS_PER_MICROLITER),
			IN_BLOOD_BY_AUTOMATED_COUNT(loinc("731-0", "Lymphocytes [#/volume] in Blood by Automated count"), GeccoUnits.THOUSANDS_PER_MICROLITER),
			IN_BLOOD_BY_FLOW_CYTOMETRY(loinc("30364-4", "Lymphocytes [#/volume] in Blood by Flow cytometry (FC)"), GeccoUnits.THOUSANDS_PER_MICROLITER),
			IN_BLOOD(loinc("26474-7", "Lymphocytes [#/volume] in Blood"), GeccoUnits.THOUSANDS_PER_MICROLITER)
		}
	}

	object Neutrophils {
		enum class CountPerVolume(override val coding: Coding, val unit: GeccoUnits? = null): CodeableEnum<CountPerVolume> {
			IN_BLOOD_BY_MANUAL_COUNT(loinc("753-4", "Neutrophils [#/volume] in Blood by Manual count"), GeccoUnits.THOUSANDS_PER_MICROLITER),
			IN_BLOOD_BY_AUTOMATED_COUNT(loinc("751-8", "Neutrophils [#/volume] in Blood by Automated count"), GeccoUnits.THOUSANDS_PER_MICROLITER),
			IN_BLOOD(loinc("26499-4", "Neutrophils [#/volume] in Blood"), GeccoUnits.THOUSANDS_PER_MICROLITER)
		}

	}

	enum class PartialThromboplastinTime(override val coding: Coding, val unit: GeccoUnits? = null): CodeableEnum<PartialThromboplastinTime> {
		IN_BLOOD_BY_COAGULATION_SALINE(loinc("16631-4", "aPTT in Blood by Coagulation 1:1 saline"), GeccoUnits.S),
		IN_PLATELET_POOR_PLASMA_BY_COAGULATION_SALINE(
			loinc("43734-3", "aPTT in Platelet poor plasma by Coagulation 1:1 saline"), GeccoUnits.S),
		IN_PLATELET_POOR_PLASMA_BY_COAGULATION_ASSAY(
			loinc("14979-9", "aPTT in Platelet poor plasma by Coagulation assay"), GeccoUnits.S),
		IN_BLOOD_BY_COAGULATION_ASSAY(loinc("3173-2", "aPTT in Blood by Coagulation assay"), GeccoUnits.S)
	}
	object Platelets {
		enum class CountPerVolume(override val coding: Coding, val unit: GeccoUnits? = null): CodeableEnum<CountPerVolume> {
			IN_PLATELET_RICH_PLASMA_BY_AUTOMATED_COUNT(
				loinc("74775-8", "Platelets [#/volume] in Platelet rich plasma by Automated count"), GeccoUnits.THOUSANDS_PER_MICROLITER),
			IN_PLASMA_BY_AUTOMATED_COUNT(loinc("13056-7", "Platelets [#/volume] in Plasma by Automated count"), GeccoUnits.THOUSANDS_PER_MICROLITER),
			IN_PLASMA(loinc("26516-5", "Platelets [#/volume] in Plasma"), GeccoUnits.THOUSANDS_PER_MICROLITER),
			IN_CAPILLARY_BLOOD_BY_MANUAL_COUNT(
				loinc("74464-9", "Platelets [#/volume] in Capillary blood by Manual count"), GeccoUnits.BILLION_PER_LITER),
			IN_BLOOD_BY_MANUAL_COUNT(loinc("778-1", "Platelets [#/volume] in Blood by Manual count"), GeccoUnits.THOUSANDS_PER_MICROLITER),
			IN_BLOOD_BY_ESTIMATE(loinc("49497-1", "Platelets [#/volume] in Blood by Estimate"), GeccoUnits.THOUSANDS_PER_MICROLITER),
			IN_BLOOD_BY_AUTOMATED_COUNT(loinc("777-3", "Platelets [#/volume] in Blood by Automated count"), GeccoUnits.THOUSANDS_PER_MICROLITER),
			IN_BLOOD(loinc("26515-7", "Platelets [#/volume] in Blood"), GeccoUnits.THOUSANDS_PER_MICROLITER)
		}
	}
    enum class INR(override val coding: Coding, val unit: GeccoUnits? = null): CodeableEnum<INR> {
	    IN_PLATELET_POOR_PLASMA_BY_COAGULATION_ASSAY(loinc("6301-6", "INR in Platelet poor plasma by Coagulation assay"), GeccoUnits.INR),
	    IN_CAPILLARY_BLOOD_BY_COAGULATION_ASSAY(loinc("46418-0", "INR in Capillary blood by Coagulation assay"), GeccoUnits.INR),
	    IN_BLOOD_BY_COAGULATION_ASSAY(loinc("34714-6", "INR in Blood by Coagulation assay"), GeccoUnits.INR)
    }
	object Albumin {
		enum class MassPerVolume(override val coding: Coding, val unit: GeccoUnits? = null): CodeableEnum<MassPerVolume>  {
			IN_BLOOD_BY_BCP(
				loinc("76631-1", "Albumin [Mass/volume] in Blood by Bromocresol purple (BCP) dye binding method"), GeccoUnits.GRAM_PER_LITER),
			IN_SERUM_OR_PLASMA_BY_BCP(loinc(
				"61152-5",
				"Albumin [Mass/volume] in Serum or Plasma by Bromocresol purple (BCP) dye binding method"
			), GeccoUnits.GRAM_PER_DECILITER),
			IN_SERUM_OR_PLASMA_BY_BCG(loinc(
				"61151-7",
				"Albumin [Mass/volume] in Serum or Plasma by Bromocresol green (BCG) dye binding method"
			), GeccoUnits.GRAM_PER_DECILITER),
			IN_SERUM_OR_PLASMA_BY_ELECTROPHORESIS(
				loinc("2862-1", "Albumin [Mass/volume] in Serum or Plasma by Electrophoresis"), GeccoUnits.GRAM_PER_DECILITER),
			IN_SERUM_OR_PLASMA(loinc("1751-7", "Albumin [Mass/volume] in Serum or Plasma"), GeccoUnits.GRAM_PER_DECILITER)
		}

		enum class MolesPerVolume(override val coding: Coding, val unit: GeccoUnits? = null): CodeableEnum<MolesPerVolume> {
			IN_SERUM_OR_PLASMA_BY_BCG(loinc(
				"62235-7",
				"Albumin [Moles/volume] in Serum or Plasma by Bromocresol green (BCG) dye binding method"
			), GeccoUnits.MICROMOLE_PER_LITER),
			IN_SERUM_OR_PLASMA_BY_BCP(loinc(
				"62234-0",
				"Albumin [Moles/volume] in Serum or Plasma by Bromocresol purple (BCP) dye binding method"
			), GeccoUnits.MICROMOLE_PER_LITER),
			IN_SERUM_OR_PLASMA(loinc("54347-0", "Albumin [Moles/volume] in Serum or Plasma"), GeccoUnits.MICROMOLE_PER_LITER)
		}
	}
	enum class Antithrombin(override val coding: Coding, val unit: GeccoUnits? = null): CodeableEnum<Antithrombin> {
		//TODO moles per what volume? no value in loinc table
		MOLES_PER_VOLUME_IN_PLATELET_POOR_PLASMA_BY_CHROMOGENIC_METHOD(
			loinc("3176-5", "Antithrombin [Moles/volume] in Platelet poor plasma by Chromogenic method")),
		BY_CHROMO_NO_ADDITION_OF_HEPARIN(
			loinc("91120-6", "Antithrombin in Platelet poor plasma by Chromo.no addition of heparin"), GeccoUnits.PERCENT),
		UNITS_PER_VOLUME_IN_PLATELET_POOR_PLASMA_BY_CHROMOGENIC_METHOD(
			loinc("3174-0", "Antithrombin [Units/volume] in Platelet poor plasma by Chromogenic method"), GeccoUnits.INTERNATIONAL_UNIT_PER_MILLILITER),
		ACTUAL_IN_PLATELET_POOR_PLASMA_BY_CHROMOGENIC_METHOD(
			loinc("27811-9", "Antithrombin actual/normal in Platelet poor plasma by Chromogenic method"), GeccoUnits.PERCENT),
		AG_ACTUAL_IN_PLATELET_POOR_PLASMA_BY_IMMUNOASSAY(
			loinc("27812-7", "Antithrombin Ag actual/normal in Platelet poor plasma by Immunoassay"), GeccoUnits.PERCENT)
	}
	enum class Procalcitonin(override val coding: Coding, val unit: GeccoUnits? = null): CodeableEnum<Procalcitonin> {
		IN_SERUM_OR_PLASMA_BY_IMMUNOASSAY(
			loinc("75241-0", "Procalcitonin [Mass/volume] in Serum or Plasma by Immunoassay"), GeccoUnits.NANOGRAM_PER_MILLILITER),
		IN_SERUM_OR_PLASMA(loinc("33959-8", "Procalcitonin [Mass/volume] in Serum or Plasma"), GeccoUnits.NANOGRAM_PER_MILLILITER),
	}
	object Interleukin6 {
		object Presence {
			val IN_SERUM_OR_PLASMA = loinc("44322-6", "Interleukin 6 [Presence] in Serum or Plasma")
		}

		enum class MassPerVolume(override val coding: Coding, val unit: GeccoUnits? = null): CodeableEnum<MassPerVolume> {
			IN_SERUM_OR_PLASMA(loinc("26881-3", "Interleukin 6 [Mass/volume] in Serum or Plasma"), GeccoUnits.PICOGRAM_PER_MILLILITER),
			IN_CEREBRAL_SPINE_FLUID(loinc("49919-4", "Interleukin 6 [Mass/volume] in Cerebral spinal fluid"), GeccoUnits.PICOGRAM_PER_MILLILITER),
			IN_BODY_FLUID(loinc("49732-1", "Interleukin 6 [Mass/volume] in Body fluid"), GeccoUnits.PICOGRAM_PER_MILLILITER)
		}

	}
	enum class NatriureticPeptideB(override val coding: Coding, val unit: GeccoUnits? = null): CodeableEnum<NatriureticPeptideB>  {
		IN_SERUM_OR_PLASMA(loinc("33762-6", "Natriuretic peptide.B prohormone N-Terminal [Mass/volume] in Serum or Plasma"), GeccoUnits.PICOGRAM_PER_MILLILITER)
	}
	object Fibrinogen {
		enum class Presence(override val coding: Coding): CodeableEnum<Presence> {
			IN_PLATELET_POOR_PLASMA(loinc("16859-1", "Fibrinogen [Presence] in Platelet poor plasma"))
		}

		enum class MassPerVolume(override val coding: Coding, val unit: GeccoUnits? = null): CodeableEnum<MassPerVolume> {
			IN_PLATELET_POOR_PLASMA_BY_HEAT_DENATURATION(
				loinc("30902-1", "Fibrinogen [Mass/volume] in Platelet poor plasma by Heat denaturation"), GeccoUnits.GRAM_PER_LITER),
			IN_PLATELET_POOR_PLASMA_BY_COAGULATION_DERIVED(
				loinc("48664-7", "Fibrinogen [Mass/volume] in Platelet poor plasma by Coagulation.derived"), GeccoUnits.GRAM_PER_LITER),
			IN_PLATELET_POOR_PLASMA_BY_COAGULATION_ASSAY(
				loinc("3255-7", "Fibrinogen [Mass/volume] in Platelet poor plasma by Coagulation assay"), GeccoUnits.MILLIGRAM_PER_DECILITER)
		}

	}
}

enum class GeccoUnits(val code:String, val display: String): CodeableEnum<GeccoUnits> {
	NANOGRAM_PER_MILLILITER("ng/mL", "nanogram per millliiter"),
	MILLIGRAM_PER_DECILITER("mg/dL", "milligram per deciliter"),
	THOUSANDS_PER_MICROLITER("10*3/uL","Thousands Per MicroLiter"),
	MICROMOLE_PER_LITER("umol/L", "micromole per liter"),
	PICOMOLE_PER_LITER("pmol/L","picomole per liter"),
	GRAM_PER_DECILITER("g/dL","gram per deciliter"),
	ENZYME_UNIT_PER_LITER("U/L", "enzyme unit per liter"),
	S("s", ""),
	MILLIMOLE_PER_LITER("mmol/L","millimole per liter"),
	MILLIGRAM_PER_LITER("mg/L","milligram per liter"),
	PICOGRAM_PER_MILLILITER("pg/mL","picogram per milliliter"),
	PERCENT("%","percent"),
	PER_MICROLITER("/uL", "per microliter"),
	GRAM_PER_LITER("g/L", "gram per liter"),
	INTERNATIONAL_UNIT_PER_MILLILITER("[IU]/mL", "international unit per milliliter"),
	ARBITRARY_UNIT_PER_MILLILITER("[arb'U]/mL", "arbitrary unit per milliliter"),
	INR("{INR}", "international normalized ratio"),
	TITER("{titer}","titer"),
	MICROGRAM_PER_LITER_DDU("ug/L{DDU}","microgram / liter DDU"),
	NANOGRAM_FEU_PER_MILLILITER("ng{FEU}/mL", "nanogram FEU per milliliter"),
	MILLIGRAM_FEU_PER_MILLILITER("mg{FEU}/L", "milligram FEU per milliliter"),
	MICROGRAM_PER_MILLILITER_FEU("ug/mL{FEU}", "microgram / milliliter FEU"),
	MICROGRAM_PER_LITER("ug/L", "microgram per liter"),
	NANOGRAM_PER_LITER("ng/L","nanogram per liter"),
	MICROGRAM_PER_MILLILITER("ug/mL", "microgram per milliliter"),
	MILLIGRAM_FEU_PER_LITER("mg{FEU}/L", "milligram FEU per liter"),
	BILLION_PER_LITER("10*9/L","billion per liter"),
	NANOMOLE_PER_LITER("nmol/L", "nanomole per liter");

	override val coding: Coding
		get() = Coding("http://unitsofmeasure.org/", this.name, this.display)
}


enum class TypeOfDischarge(val codeableConcept: CodeableConcept) {
	ALIVE(CodeableConcept(snomed("371827001", "Patient discharged alive (finding)"))),
	REFERRAL(CodeableConcept(snomed("3457005", "Patient referral (procedure)"))),
	PALLIATIVE_DISCHARGE(CodeableConcept(snomed("306237005", "Referral to palliative care service (procedure)"))),
	UNKNOWN(CodeableConcept(snomed("261665006", "Unknown (qualifier value)"))),
	HOSPITAL_ADMISSION(CodeableConcept(snomed("32485007", "Hospital admission (procedure)"))),
	DEATH(CodeableConcept(snomed("419099009", "Dead (finding)"))),
}

enum class SymptomSeverity(override val coding: Coding): CodeableEnum<SymptomSeverity> {
	MILD(snomed("255604002", "Mild (qualifier value)")),
	MODERATE(snomed("6736007", "Moderate (severity modifier) (qualifier value)")),
	SEVERE(snomed("24484000", "Severe (severity modifier) (qualifier value)")),
	LIFE_THREATENING(snomed("442452003", "Life threatening severity (qualifier value)"))
}

enum class OrgansForTransplant(val snomed: Coding, val icd10: Coding?) {
	ENTIRE_HEART(
		snomed("302509004", "Entire heart (body structure)"),
		icd10gm("Z94.1", "Zustand nach Herztransplantation")
	),
	ENTIRE_LUNG(
		snomed("181216001", "Entire lung (body structure)"),
		icd10gm("Z94.2", "Zustand nach Lungentransplantation")
	),
	ENTIRE_LIVER(
		snomed("181268008", "Entire liver (body structure)"),
		icd10gm("Z94.4", "Zustand nach Lebertransplantation")
	),
	ENTIRE_KIDNEY(
		snomed("181414000", "Entire kidney (body structure)"),
		icd10gm("Z94.0", "Zustand nach Nierentransplantation")
	),
	ENTIRE_PANCREAS(
		snomed("181277001", "Entire pancreas (body structure)"),
		icd10gm("Z94.88", "Zustand nach sonstiger Organ- oder Gewebetransplantation Inkl.: Darm Pankreas")
	),
	INTESTINAL_STRUCTURE(snomed("113276009", "Intestinal structure (body structure)"), null),
	ENTIRE_SMALL_INTESTINE(snomed("181250005", "Entire small intestine (body structure)"), null),
	ENTIRE_LARGE_INTESTINE(snomed("181254001", "Entire large intestine (body structure)"), null),
	SKIN_PART(snomed("119181002", "Skin part (body structure)"), icd10gm("Z94.5", "Zustand nach Hauttransplantation")),
	ENTIRE_CORNEA(
		snomed("181162001", "Entire cornea (body structure)"),
		icd10gm("Z94.7", "Zustand nach Keratoplastik")
	),
	EAR_OSSICLE_STRUCTURE(snomed("41845008", "Ear ossicle structure (body structure)"), null),
	ENTIRE_HEART_VALVE(snomed("181285005", "Entire heart valve (body structure)"), null),
	BLOOD_VESSEL_PART(snomed("119206002", "Blood vessel part (body structure)"), null),
	CEREBRAL_MENINGES_STRUCTURE(snomed("8935007", "Cerebral meninges structure (body structure)"), null),
	BONE_TISSUE_STRUCTURE(snomed("3138006", "Bone (tissue) structure (body structure)"), null),
	CARTILAGE_TISSUE(snomed("309312004", "Cartilage tissue (body structure)"), null),
	TENDON_STRUCTURE(snomed("13024002", "Tendon structure (body structure)"), null),
	//TODO: Add and map remaning icd10-codes
}

enum class Symptom(override val codeableConcept: CodeableConcept): ConceptEnum<Symptom> {
	ABDOMINAL_PAIN(CodeableConcept().apply {
		coding = listOf(snomed("21522001", "Abdominal pain (finding)"))
		text = "Bauchschmerzen"
	}),
	ASYMPTOMATIC(CodeableConcept().apply {
		coding = listOf(snomed("84387000", "Asymptomatic (finding)"))
		text = "Asymptomatisch"
	}),
	BLEEDING(CodeableConcept().apply {
		coding = listOf(snomed("131148009", "Bleeding (finding)"))
		text = "Blutung"
	}),
	CHEST_PAIN(CodeableConcept().apply {
		coding = listOf(snomed("29857009", "Chest pain (finding)"))
		text = "Brustschmerzen"
	}),
	CHILL(CodeableConcept().apply {
		coding = listOf(snomed("43724002", "Chill (finding)"))
		text = "Schüttelfrost"
	}),
	CONJUNCTIVITIS(CodeableConcept().apply {
		coding = listOf(snomed("9826008", "Conjunctivitis (disorder)"))
		text = "Konjunktivitis"
	}),
	COUGH(CodeableConcept().apply {
		coding = listOf(snomed("49727002", "Cough (finding)"))
		text = "Husten"
	}),
	DIARRHEA(CodeableConcept().apply {
		coding = listOf(snomed("62315008", "Diarrhea (finding)"))
		text = "Durchfall"
	}),
	DISTURBANCE_OF_CONSCIOUSNESS(CodeableConcept().apply {
		coding = listOf(snomed("3006004", "Disturbance of consciousness (finding)"))
		text = "Bewusstseinsstörung"
	}),
	DYSPNEA(CodeableConcept().apply {
		coding = listOf(snomed("267036007", "Dyspnea (finding)"))
		text = "Atemnot"
	}),
	ERUPTION_OF_SKIN(CodeableConcept().apply {
		coding = listOf(snomed("271807003", "Eruption of skin (disorder)"))
		text = "Hautausschlag"
	}),
	FATIGUE(CodeableConcept().apply {
		coding = listOf(snomed("84229001", "Fatigue (finding)"))
		text = "Müdigkeit"
	}),
	FEELING_FEVERISH(CodeableConcept().apply {
		coding = listOf(snomed("103001002", "Feeling feverish (finding)"))
		text = "Fieberigkeit"
	}),
	FEVER(CodeableConcept().apply {
		coding = listOf(snomed("386661006", "Fever (finding)"))
		text = "Fieber"
	}),
	HEADACHE(CodeableConcept().apply {
		coding = listOf(snomed("25064002", "Headache (finding)"))
		text = "Kopfschmerzen"
	}),
	HEMOPTYSIS(CodeableConcept().apply {
		coding = listOf(snomed("66857006", "Hemoptysis (finding)"))
		text = "Bluthusten"
	}),
	INDRAWING_OF_RIBS_DURING_RESPIRATION(CodeableConcept().apply {
		coding = listOf(snomed("248567008", "Indrawing of ribs during respiration (finding)"))
		text = "Hauteinziehungen des Brustkorbs bei der Einatmung"
	}),
	JOINT_PAIN(CodeableConcept().apply {
		coding = listOf(snomed("57676002", "Joint pain (finding)"))
		text = "Gelenkschmerz"
	}),
	LOSS_OF_APPETITE(CodeableConcept().apply {
		coding = listOf(snomed("79890006", "Loss of appetite (finding)"))
		text = "Appetitverlust"
	}),
	LOSS_OF_SENSE_OF_SMELL(CodeableConcept().apply {
		coding = listOf(snomed("44169009", "Loss of sense of smell (finding)"))
		text = "Geruchverlust"
	}),
	LOSS_OF_TASTE(CodeableConcept().apply {
		coding = listOf(snomed("36955009", "Loss of taste (finding)"))
		text = "Geschmackverlust"
	}),
	LYMPHADENOPATHY(CodeableConcept().apply {
		coding = listOf(snomed("30746006", "Lymphadenopathy (disorder)"))
		text = "Lymphadenopathie"
	}),
	MALAISE(CodeableConcept().apply {
		coding = listOf(snomed("367391008", "Malaise (finding)"))
		text = "Unwohlsein"
	}),
	MUSCLE_PAIN(CodeableConcept().apply {
		coding = listOf(snomed("68962001", "Muscle pain (finding)"))
		text = "Muskelschmerzen"
	}),
	NASAL_CONGESTION(CodeableConcept().apply {
		coding = listOf(snomed("68235000", "Nasal congestion (finding)"))
		text = "Verstopfte Nase"
	}),
	NASAL_DISCHARGE(CodeableConcept().apply {
		coding = listOf(snomed("64531003", "Nasal discharge (finding)"))
		text = "Laufende Nase"
	}),
	NAUSEA(CodeableConcept().apply {
		coding = listOf(snomed("422587007", "Nausea (finding)"))
		text = "Übelkeit"
	}),
	PAIN_IN_THROAT(CodeableConcept().apply {
		coding = listOf(snomed("162397003", "Pain in throat (finding)"))
		text = "Halsschmerzen"
	}),
	RIGOR(CodeableConcept().apply {
		coding = listOf(snomed("38880002", "Rigor (finding)"))
		text = "Fieber" //TODO Das ist nicht Fieber
	}),
	SEIZURE(CodeableConcept().apply {
		coding = listOf(snomed("91175000", "Seizure (finding)"))
		text = "Starre" //TODO Krampfanfall
	}),
	SKIN_ULCER(CodeableConcept().apply {
		coding = listOf(snomed("46742003", "Skin ulcer (disorder)"))
		text = "Hautgeschwüre"
	}),
	UNABLE_TO_WALK(CodeableConcept().apply {
		coding = listOf(snomed("282145008", "Unable to walk (finding)"))
		text = "Unfähig zu gehen"
	}),
	VOMITING(CodeableConcept().apply {
		coding = listOf(snomed("422400008", "Vomiting (disorder)"))
		text = "Erbrechen"
	}),
	WHEEZING(CodeableConcept().apply {
		coding = listOf(snomed("56018004", "Wheezing (finding)"))
		text = "Keuchen"
	}),
	FEVER_GREATER_THAN_38_CELSIUS(CodeableConcept().apply {
		coding = listOf(snomed("426000000", "Fever greater than 100.4 Fahrenheit / 38° Celsius (finding)"))
		text = "Fieber über 38° Celsius"
	}),
	ASTHENIA(CodeableConcept().apply {
		coding = listOf(snomed("13791008", "Asthenia (finding)"))
		text = "Schwächegefühl"
	}),
	PAIN(CodeableConcept().apply {
		coding = listOf(snomed("22253000", "Pain (finding)"))
		text = "Schmerzen"
	}),
	PRODUCTIVE_COUGH(CodeableConcept().apply {
		coding = listOf(snomed("28743005", "Productive cough (finding)"))
		text = "Produktiver Husten"
	}),
	DRY_COUGH(CodeableConcept().apply {
		coding = listOf(snomed("11833005", "Dry cough (finding)"))
		text = "Trockener Husten"
	}),
	CLOUDED_CONSCIOUSNESS(CodeableConcept().apply {
		coding = listOf(snomed("40917007", "Clouded consciousness (finding)"))
		text = "Bewusstseinstrübung"
	})

}

val therapeuticProcedure = CodeableConcept(snomed("277132007", "Therapeutic procedure (procedure)"))
val positioningAndSupport =
	CodeableConcept(snomed("225287004", "Procedures relating to positioning and support (procedure)"))

enum class Therapies(val codeableConcept: CodeableConcept, val profile: String, val category: CodeableConcept) {
	DIALYSIS(CodeableConcept().apply {
		coding = listOf(snomed("108241001", "Dialysis procedure (procedure)"))
		text = "Dialysis"
	}, "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/dialysis", therapeuticProcedure),
	APHARESIS(CodeableConcept().apply {
		coding = listOf(snomed("127788007", "Apheresis (procedure)"))
		text = "Apheresis"
	}, "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/apheresis", therapeuticProcedure),
	ECMO(
		CodeableConcept().apply {
			coding = listOf(snomed("233573008", "Extracorporeal membrane oxygenation (procedure)"))
			text = "ECMO"
		},
		"https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/extracorporeal-membrane-oxygenation",
		therapeuticProcedure
	),
	PRONE_POSITION(CodeableConcept().apply {
		coding = listOf(snomed("431182000", "Placing subject in prone position (procedure)"))
		text = "Prone position"
	}, "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/prone-position", positioningAndSupport),
}

class YesNoUnknownWithSymptomSeverity(
	var yesNoUnknown: YesNoUnknown? = null,
	var severity: SymptomSeverity? = null
)
class YesNoUnknownWithDate(
	var yesNoUnknown: YesNoUnknown? = null,
	var date: Date? = null
)

enum class YesNoUnknownOtherNa(override val coding: Coding): CodeableEnum<YesNoUnknownOtherNa> {
	YES(snomed("373066001", "Yes (qualifier value)")),
	NO(snomed("373067005", "No (qualifier value)")),
	UNKNOWN(snomed("261665006", "Unknown (qualifier value)")),
	OTHER(snomed("74964007", "Other (qualifier value)")),
	NA(snomed("385432009", "Not applicable (qualifier value)"))
}

enum class VentilationTypes(val status: Procedure.ProcedureStatus,val code: Coding, val used: Coding? = null, override val coding: Coding): CodeableEnum<VentilationTypes> {
	NASAL_HIGH_FLOW_OXYGEN(
		Procedure.ProcedureStatus.INPROGRESS,
		snomed("371907003", "Oxygen administration by nasal cannula (procedure)"),
		snomed("426854004", "High flow oxygen nasal cannula (physical object)"),
		Coding("https://num-compass.science/fhir/CodeSystem/VentilationTypes", "NASAL_HIGH_FLOW_OXYGEN", "Nasal High-Flow-Oxygen-Therapy")
	),
	NON_INVASIVE_VENTILATION(
		Procedure.ProcedureStatus.INPROGRESS,
		snomed("428311008", "Noninvasive ventilation (procedure)"),
		null,
		Coding("https://num-compass.science/fhir/CodeSystem/VentilationTypes", "NON_INVASIVE_VENTILATION", "Non-invasive ventilation")
	),
	INVASIVE_VENTILATION_OROTRACHEAL(
		Procedure.ProcedureStatus.INPROGRESS,
		snomed("40617009", "Artificial respiration (procedure)"),
		snomed("26412008", "Endotracheal tube, device (physical object)"),
		Coding("https://num-compass.science/fhir/CodeSystem/VentilationTypes", "INVASIVE_VENTILATION_OROTRACHEAL", "Invasive ventilation (orotracheal)")

	),
	INVASIVE_VENTILATION_TRACHEOTOMY(
		Procedure.ProcedureStatus.INPROGRESS,
		snomed("40617009", "Artificial respiration (procedure)"),
		snomed("129121000", "Tracheostomy tube, device (physical object)"),
		Coding("https://num-compass.science/fhir/CodeSystem/VentilationTypes", "INVASIVE_VENTILATION_TRACHEOTOMY", "Invasive ventilation (Tracheotomy)" )
	),
	NO(
		Procedure.ProcedureStatus.NOTDONE,
		snomed("40617009", "Artificial respiration (procedure)"),
		null,
		Coding("https://num-compass.science/fhir/CodeSystem/VentilationTypes", "NONE", "None")
	),
	UNKNOWN(
		Procedure.ProcedureStatus.UNKNOWN,
		snomed("40617009", "Artificial respiration (procedure)"),
		null,
		Coding("https://num-compass.science/fhir/CodeSystem/VentilationTypes", "UNKNOWN", "Unknown")
	),
}

enum class Covid19Vaccine(override val coding: Coding, val snomed: Coding):CodeableEnum<Covid19Vaccine> {
	COMIRNATY(Coding("https://num-compass.science/fhir/CodeSystem/Covid19Vaccine", "COMIRNATY", "Comirnaty® BioNTech/Pfizer"),
	snomed("1119349007", "Vaccine product containing only Severe acute respiratory syndrome coronavirus 2 messenger ribonucleic acid (medicinal product)")),
	MODERNA(Coding("https://num-compass.science/fhir/CodeSystem/Covid19Vaccine", "MODERNA", "COVID-19 Vaccine Moderna®"), snomed("1119349007", "Vaccine product containing only Severe acute respiratory syndrome coronavirus 2 messenger ribonucleic acid (medicinal product)")),
	VAXZEVRIA(Coding("https://num-compass.science/fhir/CodeSystem/Covid19Vaccine", "VAXZEVRIA", "Vaxzevria® AstraZeneca"),
		snomed("1119305005", "Vaccine product containing only Severe acute respiratory syndrome coronavirus 2 antigen (medicinal product)")),
	JANSSEN(Coding("https://num-compass.science/fhir/CodeSystem/Covid19Vaccine","JANSSEN", "Janssen® Johnson & Johnson"),
		snomed("1119305005", "Vaccine product containing only Severe acute respiratory syndrome coronavirus 2 antigen (medicinal product)"))

}

enum class Diabetes(override val codeableConcept: CodeableConcept): ConceptEnum<Diabetes> {
	TYPE1(CodeableConcept(snomed("46635009", "Diabetes mellitus type 1 (disorder)"))),
	TYPE2(CodeableConcept(snomed("44054006", "Diabetes mellitus type 2 (disorder)"))),
	TYPE2_INSULIN(CodeableConcept(snomed("237599002", "Insulin treated type 2 diabetes mellitus (disorder)"))),
	TYPE3(CodeableConcept(snomed("8801005", "Secondary diabetes mellitus (disorder)"))),
}

enum class CancerStatus(override val coding: Coding): CodeableEnum<CancerStatus> {
	ACTIVE(Coding("http://terminology.hl7.org/CodeSystem/condition-clinical", "active", "Aktiv")),
	REMISSION(Coding("http://terminology.hl7.org/CodeSystem/condition-clinical", "remission", "In Remission")),
	NO(Coding("http://terminology.hl7.org/CodeSystem/v2-0136", "N", "No")),
	UNKNOWN(Coding("http://terminology.hl7.org/CodeSystem/data-absent-reason", "asked-unknown", "unknown")),
}

enum class ConditionClinicalStatus(val enum: ConditionClinical) {
	ACTIVE(ConditionClinical.ACTIVE),
	REMISSION(ConditionClinical.REMISSION),
	RESOLVED(ConditionClinical.RESOLVED);

	val codeableConcept: CodeableConcept
		get() = CodeableConcept(
			Coding(
				"http://terminology.hl7.org/CodeSystem/condition-clinical",
				enum.toCode(),
				enum.display
			)
		)
}



enum class ImmunizationDisease(val targetDisease: CodeableConcept?, val vaccineCode: CodeableConcept?) {
	INFLUENZA(
		CodeableConcept(snomed("6142004", "Influenza")),
		CodeableConcept(snomed("46233009", "Influenza virus vaccine"))
	),
	PNEUMOKOKKEN(
		CodeableConcept(snomed("16814004", "Pneumococcal infectious disease")),
		CodeableConcept(snomed("333598008", "Pneumococcal vaccine"))
	),
	BCG(
		CodeableConcept(snomed("56717001", "Tuberculosis")),
		CodeableConcept(snomed("420538001", "Tuberculosos vaccine"))
	),
	COVID19_ANTIGEN(
		CodeableConcept(
			snomed(
				"840539006",
				"Disease caused by Severe acute respiratory syndrome coronavirus 2 (disorder)"
			)
		),
		CodeableConcept(
			snomed(
				"1119305005",
				"Vaccine product containing only Severe acute respiratory syndrome coronavirus 2 antigen (medicinal product)"
			)
		)
	), //TODO ?!
	COVID19_RNA(
		CodeableConcept(
			snomed(
				"840539006",
				"Disease caused by Severe acute respiratory syndrome coronavirus 2 (disorder)"
			)
		),
		CodeableConcept(
			snomed(
				"1119349007",
				"Vaccine product containing only Severe acute respiratory syndrome coronavirus 2 messenger ribonucleic acid (medicinal product)"
			)
		)
	), //TODO ?!
	OTHER(null, null) //TODO?!
}

enum class Resuscitation(override val coding: Coding, val displayDe: String): CodeableEnum<Resuscitation> {
	YES(snomed("304252001", "For resuscitation (finding)"), "Wiederbeleben"),
	NO(snomed("304253006", "Not for resuscitation (finding)"), "Nicht wiederbeleben"),
	UNKNOWN(snomed("261665006", "Unknown (qualifier value)"), "Unbekannt")
}

enum class MedicationCovid19(val snomed: Coding? = null, val num: Coding? = null) {
	ANTIPYRETIC(
		num = Coding(
			"https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/ecrf-parameter-codes",
			"antipyretic",
			"Product containing antipyretic"
		)
	),
	STEROID(snomed = snomed("768759001", "Product containing steroid (product)")), //TODO: gleich Kortikosteroide?
	ATAZANAVIR(snomed = snomed("413591007", "Product containing atazanavir (medicinal product)")),
	DARUNAVIR(snomed = snomed("424096001", "Product containing darunavir (medicinal product)")),
	CHLOROQUINE(snomed = snomed("14728000", "Product containing chloroquine (medicinal product)")),
	HYDROXYCHLOROQUINE(snomed = snomed("83490000", "Product containing hydroxychloroquine (medicinal product)")),
	IVERMECTIN(snomed = snomed("96138006", "Product containing ivermectin (medicinal product)")),
	LOPINAVIR_RITONAVIR(snomed = snomed("134573001", "Product containing lopinavir and ritonavir (medicinal product)")),
	GANCICLOVIR(snomed = snomed("78025001", "Product containing ganciclovir (medicinal product)")),
	OSELTAMIVIR(snomed = snomed("386142008", "Product containing oseltamivir (medicinal product)")),
	REMDESIVIR(snomed = snomed("870518005", "Product containing remdesivir (medicinal product)")),
	RIBAVIRIN(snomed = snomed("35063004", "Product containing ribavirin (medicinal product)")),
	CAMOSTAT(
		num = Coding(
			"https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/ecrf-parameter-codes",
			"camostat",
			"Product containing camostat"
		)
	),
	FAVIPIRAVIR(
		num = Coding(
			"https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/ecrf-parameter-codes",
			"favipiravir",
			"Product containing favipiravir"
		)
	),
	CONVALESCENT_PLASMA(
		num = Coding(
			"https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/ecrf-parameter-codes",
			"convalescent-plasma",
			"Convalescent plasma"
		)
	),
	STEROIDS_LT_0_5_MG_PER_KG(
		num = Coding(
			"https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/ecrf-parameter-codes",
			"steroids-gt",
			"Steroids (> 0.5 mg/kg prednisone equivalents)"
		)
	),
	STEROIDS_GT_0_5_MG_PER_KG(
		num = Coding(
			"https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/ecrf-parameter-codes",
			"steroids-lt",
			"Steroids (<= 0.5 mg/kg prednisone equivalents)"
		)
	),
	TOCILIZUMAB(snomed = snomed("444649004", "Product containing tocilizumab (medicinal product)")),
	SARILUMAB(snomed = snomed("763522001", "Product containing sarilumab (medicinal product)")),
	CALCINEURIN_INHIBITOR(
		snomed = snomed(
			"416587008",
			"Product containing calcineurin inhibitor (product)"
		)
	), //TODO gleich CNI, aber was ist mit mTor?
	TUMOR_NECROSIS_FACTOR_ALPHA_INHIBITOR(
		snomed = snomed(
			"416897008",
			"Product containing tumor necrosis factor alpha inhibitor (product)"
		)
	),
	INTERLEUKIN_1_RECEPTOR_ANTAGONIST(
		snomed = snomed(
			"430817009",
			"Product containing interleukin 1 receptor antagonist (product)"
		)
	),
	RUXOLITINIB(snomed = snomed("703779004", "Product containing ruxolitinib (medicinal product)")),
	COLCHICINE(snomed = snomed("73133000", "Product containing colchicine (medicinal product)")),
	INTERFERON(snomed = snomed("768865007", "Product containing interferon (product)")),

	/**
	 * 25-Hydroxy-Vitamin-D3
	 */
	CALCIFEDIOL(snomed = snomed("88519001", "Product containing calcifediol (medicinal product)")),
	ZINC(snomed = snomed("764877006", "Product containing zinc (medicinal product)")),
}

enum class TherapeuticIntent(val codeableConcept: CodeableConcept) {
//	ADJUNCT(CodeableConcept(snomed("421974008", "Adjunct - intent (qualifier value)"))),
//	ADJUVANT(CodeableConcept(snomed("373846009", "Adjuvant - intent (qualifier value)"))),
	CURATIVE(CodeableConcept(snomed("373808002", "Curative - procedure intent (qualifier value)"))),
//	NEO_ADJUVANT(CodeableConcept(snomed("373847000", "Neo-adjuvant - intent (qualifier value)"))),
	PROPHYLAXIS(CodeableConcept(snomed("360271000", "Prophylaxis - procedure intent (qualifier value)"))),
//	SUPPORTIVE(CodeableConcept(snomed("399707004", "Supportive - procedure intent (qualifier value)"))),
}

enum class AntiCoagulant(val code: Coding) {
	HEPARINGRUPPE(atc("B01AB", "Heparingruppe")),
	HEPARIN(atc("B01AB01", "Heparin")),
	ANTITHROMBIN_III_ANTITHROMBIN_ALFA(atc("B01AB02", "Antithrombin III, Antithrombin alfa")),
	DALTEPARIN(atc("B01AB04", "Dalteparin")),
	ENOXAPARIN(atc("B01AB05", "Enoxaparin")),
	NADROPARIN(atc("B01AB06", "Nadroparin")),
	PARNAPARIN(atc("B01AB07", "Parnaparin")),
	REVIPARIN(atc("B01AB08", "Reviparin")),
	DANAPAROID(atc("B01AB09", "Danaparoid")),
	TINZAPARIN(atc("B01AB10", "Tinzaparin")),
	SULODEXID(atc("B01AB11", "Sulodexid")),
	BEMIPARIN(atc("B01AB12", "Bemiparin")),
	CERTOPARIN(atc("B01AB13", "Certoparin")),
	HEPARIN_KOMBINATIONEN(atc("B01AB51", "Heparin, Kombinationen")),
	CERTOPARIN_KOMBINATIONEN(atc("B01AB63", "Certoparin, Kombinationen")),
	ARGATROBAN(atc("B01AE03", "Argatroban")),
	THROMBOZYTENAGGREGATIONSHEMMER_EXKL_HEPARIN(atc("B01AC", "Thrombozytenaggregationshemmer, exkl. Heparin")),
	DITAZOL(atc("B01AC01", "Ditazol")),
	CLORICROMEN(atc("B01AC02", "Cloricromen")),
	PICOTAMID(atc("B01AC03", "Picotamid")),
	CLOPIDOGREL(atc("B01AC04", "Clopidogrel")),
	TICLOPIDIN(atc("B01AC05", "Ticlopidin")),
	ACETYLSALICYLSAEURE(atc("B01AC06", "Acetylsalicylsäure")),
	DIPYRIDAMOL(atc("B01AC07", "Dipyridamol")),
	CARBASALAT_CALCIUM(atc("B01AC08", "Carbasalat calcium")),
	EPOPROSTENOL(atc("B01AC09", "Epoprostenol")),
	INDOBUFEN(atc("B01AC10", "Indobufen")),
	ILOPROST(atc("B01AC11", "Iloprost")),
	SULFINPYRAZON(atc("B01AC12", "Sulfinpyrazon")),
	ABCIXIMAB(atc("B01AC13", "Abciximab")),
	ALOXIPRIN(atc("B01AC15", "Aloxiprin")),
	EPTIFIBATID(atc("B01AC16", "Eptifibatid")),
	TIROFIBAN(atc("B01AC17", "Tirofiban")),
	TRIFLUSAL(atc("B01AC18", "Triflusal")),
	BERAPROST(atc("B01AC19", "Beraprost")),
	TREPROSTINIL(atc("B01AC21", "Treprostinil")),
	PRASUGREL(atc("B01AC22", "Prasugrel")),
	CILOSTAZOL(atc("B01AC23", "Cilostazol")),
	TICAGRELOR(atc("B01AC24", "Ticagrelor")),
	CANGRELOR(atc("B01AC25", "Cangrelor")),
	VORAPAXAR(atc("B01AC26", "Vorapaxar")),
	SELEXIPAG(atc("B01AC27", "Selexipag")),
	KOMBINATIONEN(atc("B01AC30", "Kombinationen")),
	CLOPIDOGREL_UND_ACETYLSALICYLSAEURE(atc("B01AC34", "Clopidogrel und Acetylsalicylsäure")),
	DIPYRIDAMOL_UND_ACETYLSALICYLSAEURE(atc("B01AC36", "Dipyridamol und Acetylsalicylsäure")),
	ACETYLSALICYLSAEURE_KOMBINATIONEN_MIT_PROTONENPUMPENHEMMERN(
		atc(
			"B01AC56",
			"Acetylsalicylsäure, Kombinationen mit Protonenpumpenhemmern"
		)
	),
	ACETYLSALICYLSAEURE_UND_ESOMEPRAZOL(atc("B01AC86", "Acetylsalicylsäure und Esomeprazol")),
	PHENPROCOUMON(atc("B01AA04", "Phenprocoumon")),
	DIREKTE_FAKTOR_XA_INHIBITOREN(atc("B01AF", "Direkte Faktor-Xa-Inhibitoren")),
	RIVAROXABAN(atc("B01AF01", "Rivaroxaban")),
	APIXABAN(atc("B01AF02", "Apixaban")),
	EDOXABAN(atc("B01AF03", "Edoxaban")),
	BETRIXABAN(atc("B01AF04", "Betrixaban")),
	DIREKTE_THROMBININHIBITOREN(atc("B01AE", "Direkte Thrombininhibitoren")),
	DESIRUDIN(atc("B01AE01", "Desirudin")),
	LEPIRUDIN(atc("B01AE02", "Lepirudin")),

	//	ARGATROBAN(atc("B01AE03", "Argatroban")), TODO: Issue erstellen
	MELAGATRAN(atc("B01AE04", "Melagatran")),
	XIMELAGATRAN(atc("B01AE05", "Ximelagatran")),
	BIVALIRUDIN(atc("B01AE06", "Bivalirudin")),
	DABIGATRANETEXILAT(atc("B01AE07", "Dabigatranetexilat")),

}

enum class FederalStates(override val coding: Coding): CodeableEnum<FederalStates>{
	BADEN_WUERTTEMBERG(iso3155DE("DE-BW", "Baden-Württemberg")),
	BAYERN(iso3155DE("DE-BY", "Bayern")),
	BERLIN(iso3155DE("DE-BE", "Berlin")),
	BRANDENBURG(iso3155DE("DE-BB", "Brandenburg")),
	BREMEN(iso3155DE("DE-HB", "Bremen")),
	HAMBURG(iso3155DE("DE-HH", "Hamburg")),
	HESSEN(iso3155DE("DE-HE", "Hessen")),
	MECKLENBURG_VORPOMMERN(iso3155DE("DE-MV", "Mecklenburg-Vorpommern")),
	NIEDERSACHSEN(iso3155DE("DE-NI", "Niedersachsen")),
	NORDRHEIN_WESTFAHLEN(iso3155DE("DE-NW", "Nordrhein-Westfalen")),
	RHEINLAND_PFALZ(iso3155DE("DE-RP", "Rheinland-Pfalz")),
	SAARLAND(iso3155DE("DE-SL", "Saarland")),
	SACHSEN(iso3155DE("DE-SN", "Sachsen")),
	SACHSEN_ANHALT(iso3155DE("DE-ST", "Sachsen-Anhalt")),
	SCHLESWIG_HOLSTEIN(iso3155DE("DE-SH", "Schleswig-Holstein")),
	THUERINGEN(iso3155DE("DE-TH", "Thüringen"))
}

enum class Countries(override val coding: Coding): CodeableEnum<Countries>{
	//TODO: Support also 3 digit one
	ANDORRA (iso3166_1_2("AD", "Andorra")),
	UNITED_ARAB_EMIRATES(iso3166_1_2("AE", "United Arab Emirates")),
	AFGHANISTAN(iso3166_1_2("AF", "Afghanistan")),
	ANTIGUAAND_BARBUDA(iso3166_1_2("AG", "Antigua and Barbuda")),
	ANGUILLA(iso3166_1_2("AI", "Anguilla")),
	ALBANIA(iso3166_1_2("AL", "Albania")),
	ARMENIA(iso3166_1_2("AM", "Armenia")),
	ANGOLA(iso3166_1_2("AO", "Angola")),
	ANTARCTICA(iso3166_1_2("AQ", "Antarctica")),
	ARGENTINA(iso3166_1_2("AR", "Argentina")),
	AMERICAN_SAMOA(iso3166_1_2("AS", "American Samoa")),
	AUSTRIA(iso3166_1_2("AT", "Austria")),
	AUSTRALIA(iso3166_1_2("AU", "Australia")),
	ARUBA(iso3166_1_2("AW", "Aruba")),
	ALAND_ISLANDS(iso3166_1_2("AX", "Åland Islands")),
	AZERBAIJAN(iso3166_1_2("AZ", "Azerbaijan")),
	BOSNIAAND_HERZEGOVINA(iso3166_1_2("BA", "Bosnia and Herzegovina")),
	BARBADOS(iso3166_1_2("BB", "Barbados")),
	BANGLADESH(iso3166_1_2("BD", "Bangladesh")),
	BELGIUM(iso3166_1_2("BE", "Belgium")),
	BURKINA_FASO(iso3166_1_2("BF", "Burkina Faso")),
	BULGARIA(iso3166_1_2("BG", "Bulgaria")),
	BAHRAIN(iso3166_1_2("BH", "Bahrain")),
	BURUNDI(iso3166_1_2("BI", "Burundi")),
	BENIN(iso3166_1_2("BJ", "Benin")),
	SAINT_BARTHELEMY(iso3166_1_2("BL", "Saint Barthélemy")),
	BERMUDA(iso3166_1_2("BM", "Bermuda")),
	BRUNEI_DARUSSALAM(iso3166_1_2("BN", "Brunei Darussalam")),
	BOLIVIA_PLURINATIONAL_STATEOF(iso3166_1_2("BO", "Bolivia, Plurinational State of")),
	BONAIRE_SINT_EUSTATIUSAND_SABA(iso3166_1_2("BQ", "Bonaire, Sint Eustatius and Saba")),
	BRAZIL(iso3166_1_2("BR", "Brazil")),
	BAHAMAS(iso3166_1_2("BS", "Bahamas")),
	BHUTAN(iso3166_1_2("BT", "Bhutan")),
	BOUVET_ISLAND(iso3166_1_2("BV", "Bouvet Island")),
	BOTSWANA(iso3166_1_2("BW", "Botswana")),
	BELARUS(iso3166_1_2("BY", "Belarus")),
	BELIZE(iso3166_1_2("BZ", "Belize")),
	CANADA(iso3166_1_2("CA", "Canada")),
	COCOS_KEELING_ISLANDS(iso3166_1_2("CC", "Cocos (Keeling) Islands")),
	CONGOTHE_DEMOCRATIC_REPUBLICOFTHE(iso3166_1_2("CD", "Congo, the Democratic Republic of the")),
	CENTRAL_AFRICAN_REPUBLIC(iso3166_1_2("CF", "Central African Republic")),
	CONGO(iso3166_1_2("CG", "Congo")),
	SWITZERLAND(iso3166_1_2("CH", "Switzerland")),
	COTED_IVOIRE(iso3166_1_2("CI", "Côte d''Ivoire")),
	COOK_ISLANDS(iso3166_1_2("CK", "Cook Islands")),
	CHILE(iso3166_1_2("CL", "Chile")),
	CAMEROON(iso3166_1_2("CM", "Cameroon")),
	CHINA(iso3166_1_2("CN", "China")),
	COLOMBIA(iso3166_1_2("CO", "Colombia")),
	COSTA_RICA(iso3166_1_2("CR", "Costa Rica")),
	CUBA(iso3166_1_2("CU", "Cuba")),
	CABO_VERDE(iso3166_1_2("CV", "Cabo Verde")),
	CURACAO(iso3166_1_2("CW", "Curaçao")),
	CHRISTMAS_ISLAND(iso3166_1_2("CX", "Christmas Island")),
	CYPRUS(iso3166_1_2("CY", "Cyprus")),
	CZECHIA(iso3166_1_2("CZ", "Czechia")),
	GERMANY(iso3166_1_2("DE", "Germany")),
	DJIBOUTI(iso3166_1_2("DJ", "Djibouti")),
	DENMARK(iso3166_1_2("DK", "Denmark")),
	DOMINICA(iso3166_1_2("DM", "Dominica")),
	DOMINICAN_REPUBLIC(iso3166_1_2("DO", "Dominican Republic")),
	ALGERIA(iso3166_1_2("DZ", "Algeria")),
	ECUADOR(iso3166_1_2("EC", "Ecuador")),
	ESTONIA(iso3166_1_2("EE", "Estonia")),
	EGYPT(iso3166_1_2("EG", "Egypt")),
	WESTERN_SAHARA(iso3166_1_2("EH", "Western Sahara")),
	ERITREA(iso3166_1_2("ER", "Eritrea")),
	SPAIN(iso3166_1_2("ES", "Spain")),
	ETHIOPIA(iso3166_1_2("ET", "Ethiopia")),
	FINLAND(iso3166_1_2("FI", "Finland")),
	FIJI(iso3166_1_2("FJ", "Fiji")),
	FALKLAND_ISLANDS_MALVINAS(iso3166_1_2("FK", "Falkland Islands (Malvinas)")),
	MICRONESIA_FEDERATED_STATESOF(iso3166_1_2("FM", "Micronesia, Federated States of")),
	FAROE_ISLANDS(iso3166_1_2("FO", "Faroe Islands")),
	FRANCE(iso3166_1_2("FR", "France")),
	GABON(iso3166_1_2("GA", "Gabon")),
	UNITED_KINGDOMOF_GREAT_BRITAINAND_NORTHERN_IRELAND(iso3166_1_2("GB", "United Kingdom of Great Britain and Northern Ireland")),
	GRENADA(iso3166_1_2("GD", "Grenada")),
	GEORGIA(iso3166_1_2("GE", "Georgia")),
	FRENCH_GUIANA(iso3166_1_2("GF", "French Guiana")),
	GUERNSEY(iso3166_1_2("GG", "Guernsey")),
	GHANA(iso3166_1_2("GH", "Ghana")),
	GIBRALTAR(iso3166_1_2("GI", "Gibraltar")),
	GREENLAND(iso3166_1_2("GL", "Greenland")),
	GAMBIA(iso3166_1_2("GM", "Gambia")),
	GUINEA(iso3166_1_2("GN", "Guinea")),
	GUADELOUPE(iso3166_1_2("GP", "Guadeloupe")),
	EQUATORIAL_GUINEA(iso3166_1_2("GQ", "Equatorial Guinea")),
	GREECE(iso3166_1_2("GR", "Greece")),
	SOUTH_GEORGIAANDTHE_SOUTH_SANDWICH_ISLANDS(iso3166_1_2("GS", "South Georgia and the South Sandwich Islands")),
	GUATEMALA(iso3166_1_2("GT", "Guatemala")),
	GUAM(iso3166_1_2("GU", "Guam")),
	GUINEA__BISSAU(iso3166_1_2("GW", "Guinea-Bissau")),
	GUYANA(iso3166_1_2("GY", "Guyana")),
	HONG_KONG(iso3166_1_2("HK", "Hong Kong")),
	HEARD_ISLANDAND_MC_DONALD_ISLANDS(iso3166_1_2("HM", "Heard Island and McDonald Islands")),
	HONDURAS(iso3166_1_2("HN", "Honduras")),
	CROATIA(iso3166_1_2("HR", "Croatia")),
	HAITI(iso3166_1_2("HT", "Haiti")),
	HUNGARY(iso3166_1_2("HU", "Hungary")),
	INDONESIA(iso3166_1_2("ID", "Indonesia")),
	IRELAND(iso3166_1_2("IE", "Ireland")),
	ISRAEL(iso3166_1_2("IL", "Israel")),
	ISLEOF_MAN(iso3166_1_2("IM", "Isle of Man")),
	INDIA(iso3166_1_2("IN", "India")),
	BRITISH_INDIAN_OCEAN_TERRITORY(iso3166_1_2("IO", "British Indian Ocean Territory")),
	IRAQ(iso3166_1_2("IQ", "Iraq")),
	IRAN_ISLAMIC_REPUBLICOF(iso3166_1_2("IR", "Iran, Islamic Republic of")),
	ICELAND(iso3166_1_2("IS", "Iceland")),
	ITALY(iso3166_1_2("IT", "Italy")),
	JERSEY(iso3166_1_2("JE", "Jersey")),
	JAMAICA(iso3166_1_2("JM", "Jamaica")),
	JORDAN(iso3166_1_2("JO", "Jordan")),
	JAPAN(iso3166_1_2("JP", "Japan")),
	KENYA(iso3166_1_2("KE", "Kenya")),
	KYRGYZSTAN(iso3166_1_2("KG", "Kyrgyzstan")),
	CAMBODIA(iso3166_1_2("KH", "Cambodia")),
	KIRIBATI(iso3166_1_2("KI", "Kiribati")),
	COMOROS(iso3166_1_2("KM", "Comoros")),
	SAINT_KITTSAND_NEVIS(iso3166_1_2("KN", "Saint Kitts and Nevis")),
	KOREA_DEMOCRATIC_PEOPLES_REPUBLICOF(iso3166_1_2("KP", "Korea, Democratic People''s Republic of")),
	KOREA_REPUBLICOF(iso3166_1_2("KR", "Korea, Republic of")),
	KUWAIT(iso3166_1_2("KW", "Kuwait")),
	CAYMAN_ISLANDS(iso3166_1_2("KY", "Cayman Islands")),
	KAZAKHSTAN(iso3166_1_2("KZ", "Kazakhstan")),
	LAO_PEOPLES_DEMOCRATIC_REPUBLIC(iso3166_1_2("LA", "Lao People''s Democratic Republic")),
	LEBANON(iso3166_1_2("LB", "Lebanon")),
	SAINT_LUCIA(iso3166_1_2("LC", "Saint Lucia")),
	LIECHTENSTEIN(iso3166_1_2("LI", "Liechtenstein")),
	SRI_LANKA(iso3166_1_2("LK", "Sri Lanka")),
	LIBERIA(iso3166_1_2("LR", "Liberia")),
	LESOTHO(iso3166_1_2("LS", "Lesotho")),
	LITHUANIA(iso3166_1_2("LT", "Lithuania")),
	LUXEMBOURG(iso3166_1_2("LU", "Luxembourg")),
	LATVIA(iso3166_1_2("LV", "Latvia")),
	LIBYA(iso3166_1_2("LY", "Libya")),
	MOROCCO(iso3166_1_2("MA", "Morocco")),
	MONACO(iso3166_1_2("MC", "Monaco")),
	MOLDOVA_REPUBLICOF(iso3166_1_2("MD", "Moldova, Republic of")),
	MONTENEGRO(iso3166_1_2("ME", "Montenegro")),
	SAINT_MARTIN__FRENCHPART(iso3166_1_2("MF", "Saint Martin (French part)")),
	MADAGASCAR(iso3166_1_2("MG", "Madagascar")),
	MARSHALL_ISLANDS(iso3166_1_2("MH", "Marshall Islands")),
	MACEDONIATHEFORMER_YUGOSLAV_REPUBLICOF(iso3166_1_2("MK", "Macedonia, the former Yugoslav Republic of")),
	MALI(iso3166_1_2("ML", "Mali")),
	MYANMAR(iso3166_1_2("MM", "Myanmar")),
	MONGOLIA(iso3166_1_2("MN", "Mongolia")),
	MACAO(iso3166_1_2("MO", "Macao")),
	NORTHERN_MARIANA_ISLANDS(iso3166_1_2("MP", "Northern Mariana Islands")),
	MARTINIQUE(iso3166_1_2("MQ", "Martinique")),
	MAURITANIA(iso3166_1_2("MR", "Mauritania")),
	MONTSERRAT(iso3166_1_2("MS", "Montserrat")),
	MALTA(iso3166_1_2("MT", "Malta")),
	MAURITIUS(iso3166_1_2("MU", "Mauritius")),
	MALDIVES(iso3166_1_2("MV", "Maldives")),
	MALAWI(iso3166_1_2("MW", "Malawi")),
	MEXICO(iso3166_1_2("MX", "Mexico")),
	MALAYSIA(iso3166_1_2("MY", "Malaysia")),
	MOZAMBIQUE(iso3166_1_2("MZ", "Mozambique")),
	NAMIBIA(iso3166_1_2("NA", "Namibia")),
	NEW_CALEDONIA(iso3166_1_2("NC", "New Caledonia")),
	NIGER(iso3166_1_2("NE", "Niger")),
	NORFOLK_ISLAND(iso3166_1_2("NF", "Norfolk Island")),
	NIGERIA(iso3166_1_2("NG", "Nigeria")),
	NICARAGUA(iso3166_1_2("NI", "Nicaragua")),
	NETHERLANDS(iso3166_1_2("NL", "Netherlands")),
	NORWAY(iso3166_1_2("NO", "Norway")),
	NEPAL(iso3166_1_2("NP", "Nepal")),
	NAURU(iso3166_1_2("NR", "Nauru")),
	NIUE(iso3166_1_2("NU", "Niue")),
	NEW_ZEALAND(iso3166_1_2("NZ", "New Zealand")),
	OMAN(iso3166_1_2("OM", "Oman")),
	PANAMA(iso3166_1_2("PA", "Panama")),
	PERU(iso3166_1_2("PE", "Peru")),
	FRENCH_POLYNESIA(iso3166_1_2("PF", "French Polynesia")),
	PAPUA_NEW_GUINEA(iso3166_1_2("PG", "Papua New Guinea")),
	PHILIPPINES(iso3166_1_2("PH", "Philippines")),
	PAKISTAN(iso3166_1_2("PK", "Pakistan")),
	POLAND(iso3166_1_2("PL", "Poland")),
	SAINT_PIERREAND_MIQUELON(iso3166_1_2("PM", "Saint Pierre and Miquelon")),
	PITCAIRN(iso3166_1_2("PN", "Pitcairn")),
	PUERTO_RICO(iso3166_1_2("PR", "Puerto Rico")),
	PALESTINE_STATEOF(iso3166_1_2("PS", "Palestine, State of")),
	PORTUGAL(iso3166_1_2("PT", "Portugal")),
	PALAU(iso3166_1_2("PW", "Palau")),
	PARAGUAY(iso3166_1_2("PY", "Paraguay")),
	QATAR(iso3166_1_2("QA", "Qatar")),
	RéUNION(iso3166_1_2("RE", "Réunion")),
	ROMANIA(iso3166_1_2("RO", "Romania")),
	SERBIA(iso3166_1_2("RS", "Serbia")),
	RUSSIAN_FEDERATION(iso3166_1_2("RU", "Russian Federation")),
	RWANDA(iso3166_1_2("RW", "Rwanda")),
	SAUDI_ARABIA(iso3166_1_2("SA", "Saudi Arabia")),
	SOLOMON_ISLANDS(iso3166_1_2("SB", "Solomon Islands")),
	SEYCHELLES(iso3166_1_2("SC", "Seychelles")),
	SUDAN(iso3166_1_2("SD", "Sudan")),
	SWEDEN(iso3166_1_2("SE", "Sweden")),
	SINGAPORE(iso3166_1_2("SG", "Singapore")),
	SAINT_HELENA_ASCENSIONAND_TRISTANDA_CUNHA(iso3166_1_2("SH", "Saint Helena, Ascension and Tristan da Cunha")),
	SLOVENIA(iso3166_1_2("SI", "Slovenia")),
	SVALBARDAND_JAN_MAYEN(iso3166_1_2("SJ", "Svalbard and Jan Mayen")),
	SLOVAKIA(iso3166_1_2("SK", "Slovakia")),
	SIERRA_LEONE(iso3166_1_2("SL", "Sierra Leone")),
	SAN_MARINO(iso3166_1_2("SM", "San Marino")),
	SENEGAL(iso3166_1_2("SN", "Senegal")),
	SOMALIA(iso3166_1_2("SO", "Somalia")),
	SURINAME(iso3166_1_2("SR", "Suriname")),
	SOUTH_SUDAN(iso3166_1_2("SS", "South Sudan")),
	SAO_TOMEAND_PRINCIPE(iso3166_1_2("ST", "Sao Tome and Principe")),
	EL_SALVADOR(iso3166_1_2("SV", "El Salvador")),
	SINT_MAARTEN__DUTCHPART(iso3166_1_2("SX", "Sint Maarten (Dutch part)")),
	SYRIAN_ARAB_REPUBLIC(iso3166_1_2("SY", "Syrian Arab Republic")),
	SWAZILAND(iso3166_1_2("SZ", "Swaziland")),
	TURKSAND_CAICOS_ISLANDS(iso3166_1_2("TC", "Turks and Caicos Islands")),
	CHAD(iso3166_1_2("TD", "Chad")),
	FRENCH_SOUTHERN_TERRITORIES(iso3166_1_2("TF", "French Southern Territories")),
	TOGO(iso3166_1_2("TG", "Togo")),
	THAILAND(iso3166_1_2("TH", "Thailand")),
	TAJIKISTAN(iso3166_1_2("TJ", "Tajikistan")),
	TOKELAU(iso3166_1_2("TK", "Tokelau")),
	TIMOR__LESTE(iso3166_1_2("TL", "Timor-Leste")),
	TURKMENISTAN(iso3166_1_2("TM", "Turkmenistan")),
	TUNISIA(iso3166_1_2("TN", "Tunisia")),
	TONGA(iso3166_1_2("TO", "Tonga")),
	TURKEY(iso3166_1_2("TR", "Turkey")),
	TRINIDADAND_TOBAGO(iso3166_1_2("TT", "Trinidad and Tobago")),
	TUVALU(iso3166_1_2("TV", "Tuvalu")),
	TAIWAN_PROVINCEOF_CHINA(iso3166_1_2("TW", "Taiwan, Province of China")),
	TANZANIA_UNITED_REPUBLICOF(iso3166_1_2("TZ", "Tanzania, United Republic of")),
	UKRAINE(iso3166_1_2("UA", "Ukraine")),
	UGANDA(iso3166_1_2("UG", "Uganda")),
	UNITED_STATES_MINOR_OUTLYING_ISLANDS(iso3166_1_2("UM", "United States Minor Outlying Islands")),
	UNITED_STATESOF_AMERICA(iso3166_1_2("US", "United States of America")),
	URUGUAY(iso3166_1_2("UY", "Uruguay")),
	UZBEKISTAN(iso3166_1_2("UZ", "Uzbekistan")),
	HOLY_SEE(iso3166_1_2("VA", "Holy See")),
	SAINT_VINCENTANDTHE_GRENADINES(iso3166_1_2("VC", "Saint Vincent and the Grenadines")),
	VENEZUELA_BOLIVARIAN_REPUBLICOF(iso3166_1_2("VE", "Venezuela, Bolivarian Republic of")),
	VIRGIN_ISLANDS_BRITISH(iso3166_1_2("VG", "Virgin Islands, British")),
	VIRGIN_ISLANDS(iso3166_1_2("VI", "Virgin Islands,")),
	VIET_NAM(iso3166_1_2("VN", "Viet Nam")),
	VANUATU(iso3166_1_2("VU", "Vanuatu")),
	WALLISAND_FUTUNA(iso3166_1_2("WF", "Wallis and Futuna")),
	SAMOA(iso3166_1_2("WS", "Samoa")),
	YEMEN(iso3166_1_2("YE", "Yemen")),
	MAYOTTE(iso3166_1_2("YT", "Mayotte")),
	SOUTH_AFRICA(iso3166_1_2("ZA", "South Africa")),
	ZAMBIA(iso3166_1_2("ZM", "Zambia")),
	ZIMBABWE(iso3166_1_2("ZW", "Zimbabwe")),
}