import CardiovascularDiseases.*
import OrgansForTransplant.*
import RheumatologicalImmunologicalDiseases.*
import ca.uhn.fhir.context.FhirContext
import org.hl7.fhir.r4.model.*
import org.hl7.fhir.r4.model.Quantity
import java.io.FileReader
import java.io.FileWriter
import java.time.LocalDate
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import java.time.Period as JodaPeriod

fun main() {
    val ctx = FhirContext.forR4()
    val parser = ctx.newJsonParser().setPrettyPrint(true)
    val questionnaire = parser.parseResource(FileReader("./././questionnaire.json")) as Questionnaire

    val fileContent = LogicalModel::class.java.getResource("/generated-response.json").readText()
    val qr = parser.parseResource(fileContent) as QuestionnaireResponse
    addExtensions(qr, toQuestionnaire())
    val logicalModel = toLogicalModel(questionnaire, qr)
    println(logicalModel.toString())
    val q =
        parser.parseResource(FileReader("C:\\Users\\oehmj\\IdeaProjects\\gecco-questionnaire\\questionnaire.json")) as Questionnaire
    val bundle = logicalModelToGeccoProfile(
        logicalModel,
        IdType.newRandomUuid().withServerBase("https://sample.com/fhir", "Patient"),
        DateTimeType.now(),
        ValidationServerBundleBuilder(Author(), App(), q)
    )
    println(parser.encodeResourceToString(bundle))
    parser.encodeResourceToWriter(bundle, FileWriter("gecco-bundle.json"))

}

fun logicalModelToGeccoProfile(
    logicalModel: LogicalModel,
    patientId: IdType,
    recordedDate: DateTimeType,
    bundleBuilder: GeccoBundleBuilder
): Bundle {
    val patientRef = Reference(patientId)
    return bundleBuilder.apply {
        val now = LocalDate.now()
        val ageInYears = logicalModel.demographics.ageInYears ?: logicalModel.demographics.ageInMonth?.floorDiv(12) ?: logicalModel.demographics.dateOfBirth?.let{ JodaPeriod.between(it, now).years }

        val patient = GeccoPatient(patientId, logicalModel.demographics.ethnicGroup, ageInYears?.toBigDecimal(), recordedDate)

        if (ageInYears != null && logicalModel.demographics.dateOfBirth == null) {
            patient.birthDateElement = logicalModel.demographics.dateOfBirth?.toFhir()
                ?: logicalModel.demographics.ageInMonth?.let {
                    DateType(recordedDate.toLocalDateTime().minusMonths(it.toLong()).toString().substring(0, 7))
                } ?: DateType((recordedDate.year - ageInYears).toString())
        } else if (logicalModel.demographics.dateOfBirth != null) {
            patient.birthDateElement = logicalModel.demographics.dateOfBirth!!.toFhir()
        }

        add(patient)


        if (logicalModel.anamnesis.hasChronicLungDiseases != null) {
            if (logicalModel.anamnesis.hasChronicLungDiseases!! == YesNoUnknown.YES) {
                addChronicLungDiseases(logicalModel.anamnesis.chronicLungDiseases, patientRef, recordedDate)
            } else if (logicalModel.anamnesis.hasChronicLungDiseases!! == YesNoUnknown.NO) {
                for (chronicLungDisease in ChronicLungDisease.values()) {
                    add(AnaChronicLungDisease(patientRef, chronicLungDisease, YesNoUnknown.NO, recordedDate))
                }
            } else if (logicalModel.anamnesis.hasChronicLungDiseases!! == YesNoUnknown.UNKNOWN) {
                for (chronicLungDisease in ChronicLungDisease.values()) {
                    add(AnaChronicLungDisease(patientRef, chronicLungDisease, YesNoUnknown.UNKNOWN, recordedDate))
                }
            }
        }

        if (logicalModel.anamnesis.hasCardiovascularDiseases != null) {
            if (logicalModel.anamnesis.hasCardiovascularDiseases!! == YesNoUnknown.YES) {
                addCardiovascularDiseases(logicalModel.anamnesis.cardiovascularDiseases, patientRef, recordedDate)
            } else if (logicalModel.anamnesis.hasCardiovascularDiseases!! == YesNoUnknown.NO) {
                for (value in CardiovascularDiseases.values()) {
                    add(AnaCardiovascular(patientRef, value, YesNoUnknown.NO, recordedDate))
                }
            } else if (logicalModel.anamnesis.hasCardiovascularDiseases!! == YesNoUnknown.UNKNOWN) {
                for (value in CardiovascularDiseases.values()) {
                    add(AnaCardiovascular(patientRef, value, YesNoUnknown.UNKNOWN, recordedDate))
                }
            }
        }


        if (logicalModel.anamnesis.hasChronicLiverDiseases != null) {
            if (logicalModel.anamnesis.hasChronicLiverDiseases!! == YesNoUnknown.YES) {
                addChronicLiverDiseases(logicalModel.anamnesis.chronicLiverDiseases, patientRef, recordedDate)
            } else if (logicalModel.anamnesis.hasChronicLiverDiseases!! == YesNoUnknown.NO) {
                for (chronicLiverDisease in ChronicLiverDiseases.values()) {
                    add(AnaChronicLiver(patientRef, chronicLiverDisease, YesNoUnknown.NO, recordedDate))
                }
            } else if (logicalModel.anamnesis.hasChronicLiverDiseases!! == YesNoUnknown.UNKNOWN) {
                for (chronicLiverDisease in ChronicLiverDiseases.values()) {
                    add(AnaChronicLiver(patientRef, chronicLiverDisease, YesNoUnknown.UNKNOWN, recordedDate))
                }
            }
        }


        if (logicalModel.anamnesis.hasRheumatologicalImmunologicalDiseases != null) {
            if (logicalModel.anamnesis.hasRheumatologicalImmunologicalDiseases!! == YesNoUnknown.YES) {
                addRheumatologicalImmunologicalDiseases(
                    logicalModel.anamnesis.rheumatologicalImmunologicalDiseases,
                    patientRef,
                    recordedDate
                )
            } else if (logicalModel.anamnesis.hasRheumatologicalImmunologicalDiseases!! == YesNoUnknown.NO) {
                for (value in RheumatologicalImmunologicalDiseases.values()) {
                    add(AnaRheumaticImmunological(patientRef, value, YesNoUnknown.NO, recordedDate))
                }
            } else if (logicalModel.anamnesis.hasRheumatologicalImmunologicalDiseases!! == YesNoUnknown.UNKNOWN) {
                for (value in RheumatologicalImmunologicalDiseases.values()) {
                    add(AnaRheumaticImmunological(patientRef, value, YesNoUnknown.UNKNOWN, recordedDate))
                }
            }
        }


        if (logicalModel.anamnesis.hasHivInfection != null) add(
            AnaHIV(patientRef, logicalModel.anamnesis.hasHivInfection!!, recordedDate)
        )

        if (logicalModel.anamnesis.hasHistoryOfBeingATissueOrOrganRecipient != null) {
            if (logicalModel.anamnesis.hasHistoryOfBeingATissueOrOrganRecipient!! == YesNoUnknown.YES) {
                addHistoryOfBeingATissueOrOrganRecipient(
                    logicalModel.anamnesis.historyOfBeingATissueOrOrganRecipient,
                    patientRef,
                    recordedDate
                )
            } else if (logicalModel.anamnesis.hasHistoryOfBeingATissueOrOrganRecipient!! == YesNoUnknown.NO) {
                for (organ in OrgansForTransplant.values()) {
                    add(AnaTransplant(patientRef, YesNoUnknown.NO, recordedDate, organ))
                }
            } else if (logicalModel.anamnesis.hasHistoryOfBeingATissueOrOrganRecipient!! == YesNoUnknown.UNKNOWN) {
                for (organ in OrgansForTransplant.values()) {
                    add(AnaTransplant(patientRef, YesNoUnknown.NO, recordedDate, organ))
                }
            }
        }

        if (logicalModel.anamnesis.hasDiabetesMellitus != null) {
            if(logicalModel.anamnesis.hasDiabetesMellitus!! == YesNoUnknown.YES){
                if(logicalModel.anamnesis.diabetesMellitus != null){
                    when (logicalModel.anamnesis.diabetesMellitus) {
                        Diabetes.TYPE1 -> {
                            add(AnaDiabetes(patientRef, Diabetes.TYPE1, YesNoUnknown.YES, recordedDate))
                            add(AnaDiabetes(patientRef, Diabetes.TYPE2, YesNoUnknown.NO, recordedDate))
                            add(AnaDiabetes(patientRef, Diabetes.TYPE2_INSULIN, YesNoUnknown.NO, recordedDate))
                            add(AnaDiabetes(patientRef, Diabetes.TYPE3, YesNoUnknown.NO, recordedDate))
                        }

                        Diabetes.TYPE2 -> {
                            add(AnaDiabetes(patientRef, Diabetes.TYPE1, YesNoUnknown.NO, recordedDate))
                            add(AnaDiabetes(patientRef, Diabetes.TYPE2, YesNoUnknown.YES, recordedDate))
                            add(AnaDiabetes(patientRef, Diabetes.TYPE2_INSULIN, YesNoUnknown.NO, recordedDate))
                            add(AnaDiabetes(patientRef, Diabetes.TYPE3, YesNoUnknown.NO, recordedDate))
                        }

                        Diabetes.TYPE2_INSULIN -> {
                            add(AnaDiabetes(patientRef, Diabetes.TYPE1, YesNoUnknown.NO, recordedDate))
                            add(AnaDiabetes(patientRef, Diabetes.TYPE2, YesNoUnknown.NO, recordedDate))
                            add(AnaDiabetes(patientRef, Diabetes.TYPE2_INSULIN, YesNoUnknown.YES, recordedDate))
                            add(AnaDiabetes(patientRef, Diabetes.TYPE3, YesNoUnknown.NO, recordedDate))
                        }

                        Diabetes.TYPE3 -> {
                            add(AnaDiabetes(patientRef, Diabetes.TYPE1, YesNoUnknown.NO, recordedDate))
                            add(AnaDiabetes(patientRef, Diabetes.TYPE2, YesNoUnknown.NO, recordedDate))
                            add(AnaDiabetes(patientRef, Diabetes.TYPE2_INSULIN, YesNoUnknown.NO, recordedDate))
                            add(AnaDiabetes(patientRef, Diabetes.TYPE3, YesNoUnknown.YES, recordedDate))
                        }

                        else -> {}
                    }

                }
            } else  if (logicalModel.anamnesis.hasDiabetesMellitus!! != YesNoUnknown.YES) {
                for (diabetesType in Diabetes.values()) {
                    add(AnaDiabetes(patientRef, diabetesType, logicalModel.anamnesis.hasDiabetesMellitus!!, recordedDate))
                }
            }
        }


        if (logicalModel.anamnesis.malignantNeoplasticDiseases != null) {
            add(AnaCancer(patientRef, logicalModel.anamnesis.malignantNeoplasticDiseases!!, recordedDate))
        }


        if (logicalModel.anamnesis.tobaccoSmokingStatus != null) {
            add(AnaSmoking(patientRef, logicalModel.anamnesis.tobaccoSmokingStatus!!, recordedDate))
        }


        if (logicalModel.anamnesis.hasChronicNeurologicalOrMentalDiseases != null) {
            if (logicalModel.anamnesis.hasChronicNeurologicalOrMentalDiseases!! == YesNoUnknown.YES) {
                addChronicNeurologicalOrMentalDiseases(
                    logicalModel.anamnesis.chronicNeurologicalOrMentalDiseases,
                    patientRef,
                    recordedDate
                )
            } else if (logicalModel.anamnesis.hasChronicNeurologicalOrMentalDiseases!! == YesNoUnknown.NO) {
                for (disease in ChronicNeurologicalMentalDisease.values()) {
                    add(AnaChronicNeurologicalMental(patientRef, disease, YesNoUnknown.NO, recordedDate))
                }
            } else if (logicalModel.anamnesis.hasChronicNeurologicalOrMentalDiseases!! == YesNoUnknown.UNKNOWN) {
                for (disease in ChronicNeurologicalMentalDisease.values()) {
                    add(AnaChronicNeurologicalMental(patientRef, disease, YesNoUnknown.UNKNOWN, recordedDate))
                }
            }
        }


        if (logicalModel.anamnesis.hasHadOxygenOrRespiratoryTherapyBeforeCurrentIllness != null) {
            add(
                AnaRespiratoryTherapy(patientRef, logicalModel.anamnesis.hasHadOxygenOrRespiratoryTherapyBeforeCurrentIllness!!, recordedDate)
            )
        }


        if (logicalModel.anamnesis.chronicKidneyDisease != null) {
            add(AnaChronicKidneyDisease(patientRef, logicalModel.anamnesis.chronicKidneyDisease!!, recordedDate))
        }

        //Todo: History of travel with countries and federals states als gecco profile?
        if (logicalModel.anamnesis.hasTravelled != null) {
            when (logicalModel.anamnesis.hasTravelled) {
                YesNoUnknown.YES -> add(AnaHistoryOfTravel(
                    patientRef,
                    YesNoUnknownOtherNa.YES,
                    logicalModel.anamnesis.historyOfTravel!!.country,
                    logicalModel.anamnesis.historyOfTravel!!.federalState,
                    logicalModel.anamnesis.historyOfTravel!!.city,
                    logicalModel.anamnesis.historyOfTravel!!.from?.toUtilDate(),
                    logicalModel.anamnesis.historyOfTravel!!.till?.toUtilDate()
                ))

                YesNoUnknown.NO -> add(AnaHistoryOfTravel(patientRef, YesNoUnknownOtherNa.NO))
                YesNoUnknown.UNKNOWN -> add(AnaHistoryOfTravel(patientRef, YesNoUnknownOtherNa.UNKNOWN))
                null -> {}
            }
        }


        if (logicalModel.anamnesis.hasGastrointestinalUclers != null) {
            add(AnaGastrointestinalUlcers(patientRef, logicalModel.anamnesis.hasGastrointestinalUclers!!, recordedDate))
        }

        if (logicalModel.anamnesis.immunizationStatus.influenza != null && logicalModel.anamnesis.immunizationStatus.influenza!!.yesNoUnknown != null) {
            val vaccineDate = logicalModel.anamnesis.immunizationStatus.influenza!!.date?.let { DateTimeType(it) } ?: unknownDateTime()
            add(Vaccination(patientRef, ImmunizationDisease.INFLUENZA, vaccineDate))
        }
        if (logicalModel.anamnesis.immunizationStatus.pneumococcal != null && logicalModel.anamnesis.immunizationStatus.pneumococcal!!.yesNoUnknown == YesNoUnknown.YES) {
            val vaccineDate = logicalModel.anamnesis.immunizationStatus.pneumococcal!!.date?.let { DateTimeType(it) } ?: unknownDateTime()
            add(Vaccination(patientRef, ImmunizationDisease.PNEUMOKOKKEN, vaccineDate))
        }
        if (logicalModel.anamnesis.immunizationStatus.bcg != null && logicalModel.anamnesis.immunizationStatus.bcg!!.yesNoUnknown == YesNoUnknown.YES) {
            val vaccineDate = logicalModel.anamnesis.immunizationStatus.bcg!!.date?.let { DateTimeType(it) } ?: unknownDateTime()
            add(Vaccination(patientRef, ImmunizationDisease.BCG, vaccineDate))
        }
        addCovid19Vaccine(logicalModel.anamnesis.immunizationStatus.covid19_first, patientRef)
        addCovid19Vaccine(logicalModel.anamnesis.immunizationStatus.covid19_second, patientRef)
        addCovid19Vaccine(logicalModel.anamnesis.immunizationStatus.covid19_third, patientRef)
        //Todo: immunization status other? how to implement? Text answer?

//            if (logicalModel.anamnesis.hasImmunization!! == YesNoUnknown.NO) { //TODO
//                add(NoKnownVaccinations(patientRef))
//            }

        if (logicalModel.anamnesis.resuscitateOrder != null) {
            add(AnaDNR(patientRef, logicalModel.anamnesis.resuscitateOrder!!))
        }

        if (logicalModel.imaging.hasHadImagingProcedures != null) {
            if (logicalModel.imaging.hasHadImagingProcedures == YesNoUnknown.YES) {
                if (logicalModel.imaging.imagingProcedures != null) {
                    if (logicalModel.imaging.imagingProcedures!!.computedTomography == YesNoUnknown.YES) {
                        //Todo : resolve conflict between gecco easy Imaging and LogicalModel.Imaging
                        // add(ImagingProcedure(patientRef, Imaging.CT, unknownDateTime()))
                    }
                    if (logicalModel.imaging.imagingProcedures!!.radiographicImaging == YesNoUnknown.YES) {
                        // add(ImagingProcedure(patientRef, Imaging.XRAY, unknownDateTime()))
                    }
                    if (logicalModel.imaging.imagingProcedures!!.ultrasound == YesNoUnknown.YES) {
                        // add(ImagingProcedure(patientRef, Imaging.ULTRASOUND, unknownDateTime()))
                    }

                }
            }
        }
        if (logicalModel.imaging.hasRadiologicalFindings != null) {
            if (logicalModel.imaging.hasRadiologicalFindings == YesNoUnknown.YES) {
                if (logicalModel.imaging.radiologicalFindings != null) {
                    add(ImagingFinding(patientRef, logicalModel.imaging.radiologicalFindings!!))
                }
            }
        }

        if (logicalModel.demographics.bodyHeight != null) {
            add(BodyHeight(patientRef, logicalModel.demographics.bodyHeight!!.toDouble(), unknownPeriod()))
        }
        if (logicalModel.demographics.bodyWeight != null) {
            add(BodyWeight(patientRef, logicalModel.demographics.bodyWeight!!.toDouble(), unknownPeriod()))
        }
        if (logicalModel.demographics.pregnancyStatus != null) {
            add(PregnancyStatus(patientRef, logicalModel.demographics.pregnancyStatus!!, unknownDateTime()))
        }


        if (logicalModel.demographics.frailityScore != null) {
            add(FrailtyScore(patientRef, logicalModel.demographics.frailityScore!!))
        }
        if (logicalModel.epidemiologicalFactors.knownCovid19Exposure != null) {
            add(KnownExposure(patientRef, logicalModel.epidemiologicalFactors.knownCovid19Exposure!!))
        }
        if (logicalModel.complications.hasHadThromboembolicComplications != null) {
            if (logicalModel.complications.hasHadThromboembolicComplications == YesNoUnknown.YES) {
                val tc = logicalModel.complications.thromboembolicComplications
                tc.embolism?.let { add(Complications(patientRef, ComplicationsCovid19.EMBOLISM, it, recordedDate)) }
                tc.thrombosis?.let { add(Complications(patientRef, ComplicationsCovid19.THROMBOSIS, it, recordedDate)) }
                tc.venousThrombosis?.let {
                    add(Complications(patientRef, ComplicationsCovid19.VENOUS_THROMBOSIS, it, recordedDate))
                }
                tc.pulmonaryEmbolism?.let {
                    add(Complications(patientRef, ComplicationsCovid19.PULMONARY_EMBOLISM, it, recordedDate))
                }
                tc.cerebrovascularAccident?.let {add(
                    Complications(patientRef, ComplicationsCovid19.CEREBROVASCULAR_ACCIDENT, it, recordedDate)
                )}
                tc.myocardialInfarction?.let {
                    add(Complications(patientRef, ComplicationsCovid19.MYOCARDIAL_INFARCTION, it, recordedDate))
                }
            } else if(logicalModel.complications.hasHadThromboembolicComplications == YesNoUnknown.NO) {
                add(Complications(patientRef, ComplicationsCovid19.EMBOLISM, YesNoUnknown.NO, recordedDate))
                add(Complications(patientRef, ComplicationsCovid19.THROMBOSIS, YesNoUnknown.NO, recordedDate))
                add(Complications(patientRef, ComplicationsCovid19.VENOUS_THROMBOSIS, YesNoUnknown.NO, recordedDate))
                add(Complications(patientRef, ComplicationsCovid19.PULMONARY_EMBOLISM, YesNoUnknown.NO, recordedDate))
                add(Complications(patientRef, ComplicationsCovid19.CEREBROVASCULAR_ACCIDENT, YesNoUnknown.NO, recordedDate))
                add(Complications(patientRef, ComplicationsCovid19.MYOCARDIAL_INFARCTION, YesNoUnknown.NO, recordedDate))
            } else {
                add(Complications(patientRef, ComplicationsCovid19.EMBOLISM, YesNoUnknown.UNKNOWN, recordedDate))
                add(Complications(patientRef, ComplicationsCovid19.THROMBOSIS, YesNoUnknown.UNKNOWN, recordedDate))
                add(Complications(patientRef, ComplicationsCovid19.VENOUS_THROMBOSIS, YesNoUnknown.UNKNOWN, recordedDate))
                add(Complications(patientRef, ComplicationsCovid19.PULMONARY_EMBOLISM, YesNoUnknown.UNKNOWN, recordedDate))
                add(Complications(patientRef, ComplicationsCovid19.CEREBROVASCULAR_ACCIDENT, YesNoUnknown.UNKNOWN, recordedDate))
                add(Complications(patientRef, ComplicationsCovid19.MYOCARDIAL_INFARCTION, YesNoUnknown.UNKNOWN, recordedDate))
            }

        }
        if (logicalModel.complications.infectiousDiseaseOfLung != null) {
            add(
                Complications(
                    patientRef, ComplicationsCovid19.INFECTIOUS_DISEASE_OF_LUNG,
                    logicalModel.complications.infectiousDiseaseOfLung!!, recordedDate
                )
            )
        }
        if (logicalModel.complications.infectiousAgentInBloodstream != null) {
            add(
                Complications(
                    patientRef, ComplicationsCovid19.INFECTIOUS_AGENT_IN_BLOODSTREAM,
                    logicalModel.complications.infectiousAgentInBloodstream!!, recordedDate
                )
            )
        }
        if (logicalModel.complications.acuteRenalFailureSyndrome != null) {
            add(
                Complications(
                    patientRef, ComplicationsCovid19.PRE_RENAL_ACUTE_KINDEY_INJURY,
                    logicalModel.complications.acuteRenalFailureSyndrome!!, recordedDate
                )
            )
        }

        if (logicalModel.onsetOfIllnessOrAdmission.stageAtDiagnosis != null) {
            add(
                DiagnosisCovid19(
                    patientRef,
                    logicalModel.onsetOfIllnessOrAdmission.stageAtDiagnosis!!,
                    recordedDate
                )
            )
        }

        //Todo: lab values

        if(logicalModel.medication.hadCovid19Therapy != null){
            if(logicalModel.medication.hadCovid19Therapy == YesNoUnknown.YES){
                if(logicalModel.medication.covid19Therapy.productContainingSteroid == YesNoUnknown.YES){
                    add(MediCovid19(patientRef, MedicationCovid19.STEROID, unknownDateTime()))
                }
                if(logicalModel.medication.covid19Therapy.productContainingAtazanavir == YesNoUnknown.YES){
                    add(MediCovid19(patientRef, MedicationCovid19.ATAZANAVIR, unknownDateTime()))
                }
                if(logicalModel.medication.covid19Therapy.productContainingDarunavir == YesNoUnknown.YES){
                    add(MediCovid19(patientRef, MedicationCovid19.DARUNAVIR, unknownDateTime()))
                }
                if(logicalModel.medication.covid19Therapy.productContainingChloroquine == YesNoUnknown.YES){
                    add(MediCovid19(patientRef, MedicationCovid19.CHLOROQUINE, unknownDateTime()))
                }
                if(logicalModel.medication.covid19Therapy.productContainingHydroxychloroquine == YesNoUnknown.YES){
                    add(MediCovid19(patientRef, MedicationCovid19.HYDROXYCHLOROQUINE, unknownDateTime()))
                }
                if(logicalModel.medication.covid19Therapy.productContainingIvermectin == YesNoUnknown.YES){
                    add(MediCovid19(patientRef, MedicationCovid19.IVERMECTIN, unknownDateTime()))
                }
                if(logicalModel.medication.covid19Therapy.productContainingGanciclovir == YesNoUnknown.YES){
                    add(MediCovid19(patientRef, MedicationCovid19.GANCICLOVIR, unknownDateTime()))
                }
                if(logicalModel.medication.covid19Therapy.productContainingOseltamivir == YesNoUnknown.YES){
                    add(MediCovid19(patientRef, MedicationCovid19.OSELTAMIVIR, unknownDateTime()))
                }
                if(logicalModel.medication.covid19Therapy.productContainingRemdesivir == YesNoUnknown.YES){
                    add(MediCovid19(patientRef, MedicationCovid19.REMDESIVIR, unknownDateTime()))
                }
                if(logicalModel.medication.covid19Therapy.productContainingRibavirin == YesNoUnknown.YES){
                    add(MediCovid19(patientRef, MedicationCovid19.RIBAVIRIN, unknownDateTime()))
                }
                if(logicalModel.medication.covid19Therapy.productContainingTocilizumab == YesNoUnknown.YES){
                    add(MediCovid19(patientRef, MedicationCovid19.TOCILIZUMAB, unknownDateTime()))
                }
                if(logicalModel.medication.covid19Therapy.productContainingSarilumab == YesNoUnknown.YES){
                    add(MediCovid19(patientRef, MedicationCovid19.SARILUMAB, unknownDateTime()))
                }
                if(logicalModel.medication.covid19Therapy.productContainingCalcineurinInhibitor == YesNoUnknown.YES){
                    add(MediCovid19(patientRef, MedicationCovid19.CALCINEURIN_INHIBITOR, unknownDateTime()))
                }
                if(logicalModel.medication.covid19Therapy.productContainingTumorNecrosisFactorAlphaInhibitor == YesNoUnknown.YES){
                    add(MediCovid19(patientRef, MedicationCovid19.TUMOR_NECROSIS_FACTOR_ALPHA_INHIBITOR, unknownDateTime()))
                }
                if(logicalModel.medication.covid19Therapy.productContainingInterleukin1ReceptorAntagonist == YesNoUnknown.YES){
                    add(MediCovid19(patientRef, MedicationCovid19.INTERLEUKIN_1_RECEPTOR_ANTAGONIST, unknownDateTime()))
                }
                if(logicalModel.medication.covid19Therapy.productContainingRuxolitinib == YesNoUnknown.YES){
                    add(MediCovid19(patientRef, MedicationCovid19.RUXOLITINIB, unknownDateTime()))
                }
                if(logicalModel.medication.covid19Therapy.productContainingColchicine == YesNoUnknown.YES){
                    add(MediCovid19(patientRef, MedicationCovid19.COLCHICINE, unknownDateTime()))
                }
                if(logicalModel.medication.covid19Therapy.productContainingInterferon == YesNoUnknown.YES){
                    add(MediCovid19(patientRef, MedicationCovid19.DARUNAVIR, unknownDateTime()))
                }
                if(logicalModel.medication.covid19Therapy.productContainingCalcifediol == YesNoUnknown.YES){
                    add(MediCovid19(patientRef, MedicationCovid19.CALCIFEDIOL, unknownDateTime()))
                }
                if(logicalModel.medication.covid19Therapy.productContainingZinc == YesNoUnknown.YES){
                    add(MediCovid19(patientRef, MedicationCovid19.ZINC, unknownDateTime()))
                }
                if(logicalModel.medication.covid19Therapy.productContainingAntipyretic == YesNoUnknown.YES){
                    add(MediCovid19(patientRef, MedicationCovid19.ANTIPYRETIC, unknownDateTime()))
                }
                if(logicalModel.medication.covid19Therapy.productContainingCamostat == YesNoUnknown.YES){
                    add(MediCovid19(patientRef, MedicationCovid19.CAMOSTAT, unknownDateTime()))
                }
                if(logicalModel.medication.covid19Therapy.productContainingFavipiravir == YesNoUnknown.YES){
                    add(MediCovid19(patientRef, MedicationCovid19.FAVIPIRAVIR, unknownDateTime()))
                }
                if(logicalModel.medication.covid19Therapy.productContainingPlasma == YesNoUnknown.YES){
                    add(MediCovid19(patientRef, MedicationCovid19.CONVALESCENT_PLASMA, unknownDateTime()))
                }
                if(logicalModel.medication.covid19Therapy.steroidsGtHalfMgPerKgPrednisoneEquivalents == YesNoUnknown.YES){
                    add(MediCovid19(patientRef, MedicationCovid19.STEROIDS_GT_0_5_MG_PER_KG, unknownDateTime()))
                }
                if(logicalModel.medication.covid19Therapy.streoidsLtHalfMgPerKgPrednisoneEquivalents == YesNoUnknown.YES){
                    add(MediCovid19(patientRef, MedicationCovid19.STEROIDS_LT_0_5_MG_PER_KG, unknownDateTime()))
                }
            }
        }
        if(logicalModel.medication.aceInhibitors != null){
            add(MediACEInhibitor(patientRef, logicalModel.medication.aceInhibitors!!.status, unknownDateTime()))
        }
        if(logicalModel.medication.immunoglobulins != null){
            add(MediImmunoglobulins(patientRef, logicalModel.medication.immunoglobulins!!, unknownDateTime()))
        }
        if(logicalModel.medication.hadAnticoagulation == YesNoUnknown.YES){
            addMediAntiCoag(patientRef, AntiCoagulant.HEPARINGRUPPE, logicalModel.medication.anticoagulation.heparinGroup)
            addMediAntiCoag(patientRef, AntiCoagulant.HEPARIN, logicalModel.medication.anticoagulation.heparin)
            addMediAntiCoag(patientRef, AntiCoagulant.ANTITHROMBIN_III_ANTITHROMBIN_ALFA, logicalModel.medication.anticoagulation.antiThrombin3OrAntithrombinAlpha)
            addMediAntiCoag(patientRef, AntiCoagulant.DALTEPARIN, logicalModel.medication.anticoagulation.dalteparin)
            addMediAntiCoag(patientRef, AntiCoagulant.ENOXAPARIN, logicalModel.medication.anticoagulation.enoxaparin)
            addMediAntiCoag(patientRef, AntiCoagulant.NADROPARIN, logicalModel.medication.anticoagulation.nadroparin)
            addMediAntiCoag(patientRef, AntiCoagulant.PARNAPARIN, logicalModel.medication.anticoagulation.parnaparin)
            addMediAntiCoag(patientRef, AntiCoagulant.REVIPARIN, logicalModel.medication.anticoagulation.reviparin)
            addMediAntiCoag(patientRef, AntiCoagulant.DANAPAROID, logicalModel.medication.anticoagulation.danaparoid)
            addMediAntiCoag(patientRef, AntiCoagulant.TINZAPARIN, logicalModel.medication.anticoagulation.tinzaparin)
            addMediAntiCoag(patientRef, AntiCoagulant.SULODEXID, logicalModel.medication.anticoagulation.sulodexid)
            addMediAntiCoag(patientRef, AntiCoagulant.BEMIPARIN, logicalModel.medication.anticoagulation.bemiparin)
            addMediAntiCoag(patientRef, AntiCoagulant.CERTOPARIN, logicalModel.medication.anticoagulation.certoparin)
            addMediAntiCoag(patientRef, AntiCoagulant.PARNAPARIN, logicalModel.medication.anticoagulation.parnaparin)
            addMediAntiCoag(patientRef, AntiCoagulant.HEPARIN_KOMBINATIONEN, logicalModel.medication.anticoagulation.heparinCombinations)
            addMediAntiCoag(patientRef, AntiCoagulant.CERTOPARIN_KOMBINATIONEN, logicalModel.medication.anticoagulation.certoparinCombinations)
            addMediAntiCoag(patientRef, AntiCoagulant.ARGATROBAN, logicalModel.medication.anticoagulation.argatroban)
            addMediAntiCoag(patientRef, AntiCoagulant.THROMBOZYTENAGGREGATIONSHEMMER_EXKL_HEPARIN, logicalModel.medication.anticoagulation.thrombocyteAggregationInhibitorExclHeparin)
            addMediAntiCoag(patientRef, AntiCoagulant.DITAZOL, logicalModel.medication.anticoagulation.ditazol)
            addMediAntiCoag(patientRef, AntiCoagulant.CLORICROMEN, logicalModel.medication.anticoagulation.cloricromen)
            addMediAntiCoag(patientRef, AntiCoagulant.PICOTAMID, logicalModel.medication.anticoagulation.picotamid)
            addMediAntiCoag(patientRef, AntiCoagulant.CLOPIDOGREL, logicalModel.medication.anticoagulation.clopidogrel)
            addMediAntiCoag(patientRef, AntiCoagulant.TICLOPIDIN, logicalModel.medication.anticoagulation.ticlopidin)
            addMediAntiCoag(patientRef, AntiCoagulant.ACETYLSALICYLSAEURE, logicalModel.medication.anticoagulation.acetylsalicylicAcid)
            addMediAntiCoag(patientRef, AntiCoagulant.DIPYRIDAMOL, logicalModel.medication.anticoagulation.dipyridamol)
            addMediAntiCoag(patientRef, AntiCoagulant.CARBASALAT_CALCIUM, logicalModel.medication.anticoagulation.carbasalatCalcium)
            addMediAntiCoag(patientRef, AntiCoagulant.EPOPROSTENOL, logicalModel.medication.anticoagulation.epoprostenol)
            addMediAntiCoag(patientRef, AntiCoagulant.INDOBUFEN, logicalModel.medication.anticoagulation.indobufen)
            addMediAntiCoag(patientRef, AntiCoagulant.ILOPROST, logicalModel.medication.anticoagulation.iloprost)
            addMediAntiCoag(patientRef, AntiCoagulant.SULFINPYRAZON, logicalModel.medication.anticoagulation.sulfinpyrazon)
            addMediAntiCoag(patientRef, AntiCoagulant.ABCIXIMAB, logicalModel.medication.anticoagulation.abciximab)
            addMediAntiCoag(patientRef, AntiCoagulant.ALOXIPRIN, logicalModel.medication.anticoagulation.aloxiprin)
            addMediAntiCoag(patientRef, AntiCoagulant.EPTIFIBATID, logicalModel.medication.anticoagulation.eptifibatid)
            addMediAntiCoag(patientRef, AntiCoagulant.TIROFIBAN, logicalModel.medication.anticoagulation.tirofiban)
            addMediAntiCoag(patientRef, AntiCoagulant.TRIFLUSAL, logicalModel.medication.anticoagulation.triflusal)
            addMediAntiCoag(patientRef, AntiCoagulant.BERAPROST, logicalModel.medication.anticoagulation.beraprost)
            addMediAntiCoag(patientRef, AntiCoagulant.TREPROSTINIL, logicalModel.medication.anticoagulation.treprostinil)
            addMediAntiCoag(patientRef, AntiCoagulant.PRASUGREL, logicalModel.medication.anticoagulation.prasugrel)
            addMediAntiCoag(patientRef, AntiCoagulant.CILOSTAZOL, logicalModel.medication.anticoagulation.cilostazol)
            addMediAntiCoag(patientRef, AntiCoagulant.TICAGRELOR, logicalModel.medication.anticoagulation.ticagrelor)
            addMediAntiCoag(patientRef, AntiCoagulant.CANGRELOR, logicalModel.medication.anticoagulation.cangrelor)
            addMediAntiCoag(patientRef, AntiCoagulant.VORAPAXAR, logicalModel.medication.anticoagulation.vorapaxar)
            addMediAntiCoag(patientRef, AntiCoagulant.SELEXIPAG, logicalModel.medication.anticoagulation.selexipag)
            addMediAntiCoag(patientRef, AntiCoagulant.KOMBINATIONEN, logicalModel.medication.anticoagulation.combinations)
            addMediAntiCoag(patientRef, AntiCoagulant.CLOPIDOGREL_UND_ACETYLSALICYLSAEURE, logicalModel.medication.anticoagulation.clopidogrelAndAcetylsalicylicAcid)
            addMediAntiCoag(patientRef, AntiCoagulant.DIPYRIDAMOL_UND_ACETYLSALICYLSAEURE, logicalModel.medication.anticoagulation.dipyridamolAndAcetylsalicylicAcid)
            addMediAntiCoag(patientRef, AntiCoagulant.ACETYLSALICYLSAEURE_KOMBINATIONEN_MIT_PROTONENPUMPENHEMMERN, logicalModel.medication.anticoagulation.acetylsalicylicAcidCombinationsWithProtonpumpInhibitors)
            addMediAntiCoag(patientRef, AntiCoagulant.ACETYLSALICYLSAEURE_UND_ESOMEPRAZOL, logicalModel.medication.anticoagulation.acetylsalicylicAcidCombinationsWithEsomeprazol)
            addMediAntiCoag(patientRef, AntiCoagulant.PHENPROCOUMON, logicalModel.medication.anticoagulation.phenprocoumon)
            addMediAntiCoag(patientRef, AntiCoagulant.DIREKTE_FAKTOR_XA_INHIBITOREN, logicalModel.medication.anticoagulation.directFactorXaInhibitors)
            addMediAntiCoag(patientRef, AntiCoagulant.RIVAROXABAN, logicalModel.medication.anticoagulation.rivaroxaban)
            addMediAntiCoag(patientRef, AntiCoagulant.APIXABAN, logicalModel.medication.anticoagulation.apixaban)
            addMediAntiCoag(patientRef, AntiCoagulant.EDOXABAN, logicalModel.medication.anticoagulation.edoxaban)
            addMediAntiCoag(patientRef, AntiCoagulant.BETRIXABAN, logicalModel.medication.anticoagulation.betrixaban)
            addMediAntiCoag(patientRef, AntiCoagulant.DIREKTE_THROMBININHIBITOREN, logicalModel.medication.anticoagulation.directThrombininIhibitors)
            addMediAntiCoag(patientRef, AntiCoagulant.DESIRUDIN, logicalModel.medication.anticoagulation.desirudin)
            addMediAntiCoag(patientRef, AntiCoagulant.LEPIRUDIN, logicalModel.medication.anticoagulation.lepirudin)
            addMediAntiCoag(patientRef, AntiCoagulant.ARGATROBAN, logicalModel.medication.anticoagulation.argatroban)
            addMediAntiCoag(patientRef, AntiCoagulant.MELAGATRAN, logicalModel.medication.anticoagulation.melagatran)
            addMediAntiCoag(patientRef, AntiCoagulant.XIMELAGATRAN, logicalModel.medication.anticoagulation.ximelagatran)
            addMediAntiCoag(patientRef, AntiCoagulant.BIVALIRUDIN, logicalModel.medication.anticoagulation.bivalirudin)
            addMediAntiCoag(patientRef, AntiCoagulant.DABIGATRANETEXILAT, logicalModel.medication.anticoagulation.dabigatranetexilat)
            //Todo: directOralAnticoagulants = ??
            // thrombosisProphylaxix = ??
            // therapeuticAnticoagulation = ??
            // if(logicalModel.medication.anticoagulation.directOralAnticoagulants == YesNoUnknown.YES){
            //     add(MediAntiCoag(patientRef, AntiCoagulant., unknownDateTime(), null))
            // }

        }
        if (logicalModel.outcomeAtDischarge.respiratoryOutcomeisVentilated == YesNoUnknown.YES) {
            add(RespiratoryOutcome(patientRef, recordedDate, ConditionVerificationStatus.VERIFIED))
        } else if (logicalModel.outcomeAtDischarge.respiratoryOutcomeisVentilated == YesNoUnknown.NO) {
            add(RespiratoryOutcome(patientRef, recordedDate, ConditionVerificationStatus.REFUTED))
        }

        if (logicalModel.outcomeAtDischarge.typeOfDischarge != null) {
            add(TypeOfDischarge(patientRef, logicalModel.outcomeAtDischarge.typeOfDischarge!!))
        }

        if (logicalModel.outcomeAtDischarge.followupSwapResultIsPositive != null) {
            //TODO: Date
            add(FollowUpSwipeResults(patientRef, logicalModel.outcomeAtDischarge.followupSwapResultIsPositive!!, LocalDate.now()))
        }

        if (logicalModel.studyEnrollmentOrInclusionCriteria.enrolledWithCovid19DiagnosisAsMainReason != null) {
            add(
                StudyInclusionDueToCovid19(
                    patientRef,
                    logicalModel.studyEnrollmentOrInclusionCriteria.enrolledWithCovid19DiagnosisAsMainReason!!
                )
            )
        }
        if (logicalModel.studyEnrollmentOrInclusionCriteria.hasPatientParticipatedInOneOrMoreInterventionalClinicalTrials != null) {
            add(
                InterventionalStudiesParticipation(
                    patientRef,
                    logicalModel.studyEnrollmentOrInclusionCriteria.hasPatientParticipatedInOneOrMoreInterventionalClinicalTrials!!
                )
            )
        }

        for (property in Symptoms::class.declaredMemberProperties) {
            val symptom = property.findAnnotation<SymptomEnum>()!!.enum
            val value = property.call(logicalModel.symptoms)
            if (value != null) {
                val (presence, severity) = if(value is YesNoUnknownWithSymptomSeverity) {
                    value.yesNoUnknown to value.severity
                } else {
                    value as YesNoUnknown to null
                }
                if (presence != null) {
                    add(Symptom(patientRef, symptom, presence, recordedDate, severity))
                }
            }


        }

        for (property in LaboratoryValuesLaboratoryValue::class.declaredMemberProperties) {
            val coding = extractCodingFromLabAnnotation(property) ?: continue
            val value = property.call(logicalModel.laboratoryValues.laboratoryValues)
            if (value != null) {
                if (value is Float) {
                    val geccoUnits = extractUnitFromLabAnnotation(property)
                    val quantity = Quantity(value.toDouble())
                    if(geccoUnits != null){
                        quantity.code = coding.code
                        quantity.system = coding.system
                        quantity.unit = geccoUnits.code
                    }
                    add(ObservationLab(patientRef, coding, quantity, recordedDate))
                }
                //TODO: Add YesNoUnknown
            }
        }


    }.bundle


}

private fun GeccoBundleBuilder.addChronicNeurologicalOrMentalDiseases(
    lm: AnamnesisChronicNeurologicalOrMentalDiseases,
    patientRef: Reference,
    recordedDate: DateTimeType
) {
    if (lm.anxietyDisorder != null) {
        add(
            AnaChronicNeurologicalMental(
                patientRef,
                ChronicNeurologicalMentalDisease.ANXIETY_DISORDER,
                lm.anxietyDisorder!!,
                recordedDate
            )
        )
    }
    if (lm.psychoticDisorder != null) {
        add(
            AnaChronicNeurologicalMental(
                patientRef,
                ChronicNeurologicalMentalDisease.PSYCHOTIC_DISORDER,
                lm.psychoticDisorder!!,
                recordedDate
            )
        )
    }
    if (lm.parkinsonDisorder != null) {
        add(
            AnaChronicNeurologicalMental(
                patientRef,
                ChronicNeurologicalMentalDisease.PARKINSONS_DISEASE,
                lm.parkinsonDisorder!!,
                recordedDate
            )
        )
    }
    if (lm.dementia != null) {
        add(
            AnaChronicNeurologicalMental(
                patientRef,
                ChronicNeurologicalMentalDisease.DEMENTIA,
                lm.dementia!!,
                recordedDate
            )
        )
    }
    if (lm.multipleSclerosis != null) {
        add(
            AnaChronicNeurologicalMental(
                patientRef,
                ChronicNeurologicalMentalDisease.MULTIPLE_SCLEROSIS,
                lm.multipleSclerosis!!,
                recordedDate
            )
        )
    }
    if (lm.combinedDisorderOfMuscleAndPeripheralNerve != null) {
        add(
            AnaChronicNeurologicalMental(
                patientRef,
                ChronicNeurologicalMentalDisease.COMBINED_DISORDER_OF_MUSCLE_AND_PERIPHERAL_NERVE,
                lm.combinedDisorderOfMuscleAndPeripheralNerve!!,
                recordedDate
            )
        )
    }
    if (lm.epilepsy != null) {
        add(
            AnaChronicNeurologicalMental(
                patientRef,
                ChronicNeurologicalMentalDisease.EPILEPSY,
                lm.epilepsy!!,
                recordedDate
            )
        )
    }

    if (lm.migraine != null) {
        add(
            AnaChronicNeurologicalMental(
                patientRef,
                ChronicNeurologicalMentalDisease.MIGRAINE,
                lm.migraine!!,
                recordedDate
            )
        )
    }

    if (lm.historyOfCerebrovascularAccidentWithResidualDeficit != null) {
        add(
            AnaChronicNeurologicalMental(
                patientRef,
                ChronicNeurologicalMentalDisease.HISTORY_OF_CEREBROVASCULAR_ACCIDENT_WITH_RESIDUAL_DEFICIT,
                lm.historyOfCerebrovascularAccidentWithResidualDeficit!!,
                recordedDate
            )
        )
    }
    if (lm.historyOfCerebrovascularAccidentWithoutResidualDeficits != null) {
        add(
            AnaChronicNeurologicalMental(
                patientRef,
                ChronicNeurologicalMentalDisease.HISTORY_OF_CEREBROVASCULAR_ACCIDENT_WITHOUT_RESIDUAL_DEFICITS,
                lm.historyOfCerebrovascularAccidentWithoutResidualDeficits!!,
                recordedDate
            )
        )
    }
}

private fun GeccoBundleBuilder.addHistoryOfBeingATissueOrOrganRecipient(
    lm: AnamnesisHistoryOfBeingATissueOrOrganRecipient,
    patientRef: Reference,
    recordedDate: DateTimeType
) {
    if (lm.entireHeart != null) {
        add(AnaTransplant(patientRef, lm.entireHeart!!, recordedDate, ENTIRE_HEART))
    }
    if (lm.entireLung != null) {
        add(AnaTransplant(patientRef, lm.entireLung!!, recordedDate, ENTIRE_LUNG))
    }
    if (lm.entireLiver != null) {
        add(AnaTransplant(patientRef, lm.entireLiver!!, recordedDate, ENTIRE_LIVER))
    }
    if (lm.entireKidney != null) {
        add(AnaTransplant(patientRef, lm.entireKidney!!, recordedDate, ENTIRE_KIDNEY))
    }
    if (lm.entirePancreas != null) {
        add(AnaTransplant(patientRef, lm.entirePancreas!!, recordedDate, ENTIRE_PANCREAS))
    }
    if (lm.intestinalStructure != null) {
        add(AnaTransplant(patientRef, lm.intestinalStructure!!, recordedDate, INTESTINAL_STRUCTURE))
    }
    if (lm.entireSmallIntestine != null) {
        add(AnaTransplant(patientRef, lm.entireSmallIntestine!!, recordedDate, ENTIRE_SMALL_INTESTINE))
    }
    if (lm.entireLargeIntestine != null) {
        add(AnaTransplant(patientRef, lm.entireLargeIntestine!!, recordedDate, ENTIRE_LARGE_INTESTINE))
    }
    if (lm.skinPart != null) {
        add(AnaTransplant(patientRef, lm.skinPart!!, recordedDate, SKIN_PART))
    }
    if (lm.entireCornea != null) {
        add(AnaTransplant(patientRef, lm.entireCornea!!, recordedDate, ENTIRE_CORNEA))
    }
    if (lm.earOssicleStructure != null) {
        add(AnaTransplant(patientRef, lm.earOssicleStructure!!, recordedDate, EAR_OSSICLE_STRUCTURE))
    }
    if (lm.entireHeartValve != null) {
        add(AnaTransplant(patientRef, lm.entireHeartValve!!, recordedDate, ENTIRE_HEART_VALVE))
    }
    if (lm.bloodVesselPart != null) {
        add(AnaTransplant(patientRef, lm.bloodVesselPart!!, recordedDate, BLOOD_VESSEL_PART))
    }
    if (lm.cerebralMeningitisStructure != null) {
        add(AnaTransplant(patientRef, lm.cerebralMeningitisStructure!!, recordedDate, CEREBRAL_MENINGES_STRUCTURE))
    }
    if (lm.boneTissueOrStructure != null) {
        add(AnaTransplant(patientRef, lm.boneTissueOrStructure!!, recordedDate, BONE_TISSUE_STRUCTURE))
    }
    if (lm.cartilageTissue != null) {
        add(AnaTransplant(patientRef, lm.cartilageTissue!!, recordedDate, CARTILAGE_TISSUE))
    }
    if (lm.tendonStructure != null) {
        add(AnaTransplant(patientRef, lm.tendonStructure!!, recordedDate, TENDON_STRUCTURE))
    }
}

private fun GeccoBundleBuilder.addRheumatologicalImmunologicalDiseases(
    lm: AnamnesisRheumatologicalImmunologicalDiseases,
    patient: Reference,
    recordedDate: DateTimeType
) {
    if (lm.vasculitis != null) {
        add(AnaRheumaticImmunological(patient, VASCULITIS, lm.vasculitis!!, recordedDate))
    }
    if (lm.rheumatoidArthritis != null) {
        add(AnaRheumaticImmunological(patient, RHEUMATOID_ARTHRITIS, lm.rheumatoidArthritis!!, recordedDate))
    }
    if (lm.inflammatoryBowelDisease != null) {
        add(AnaRheumaticImmunological(patient, INFLAMMATORY_BOWEL_DISEASE, lm.inflammatoryBowelDisease!!, recordedDate))
    }
    if (lm.collagenosis != null) {
        add(AnaRheumaticImmunological(patient, COLLAGENOSIS, lm.collagenosis!!, recordedDate))
    }
    if (lm.congenitalImmunodeficiencyDisease != null) {
        add(
            AnaRheumaticImmunological(
                patient,
                CONGENITAL_IMMUNODEFICIENCY_DISEASE,
                lm.congenitalImmunodeficiencyDisease!!,
                recordedDate
            )
        )
    }
}

private fun GeccoBundleBuilder.addChronicLiverDiseases(
    lm: AnamnesisChronicLiverDiseases,
    patient: Reference,
    recordedDate: DateTimeType
) {
    if (lm.steatosisOfLiver != null) {
        add(AnaChronicLiver(patient, ChronicLiverDiseases.STEATOSIS_OF_LIVER, lm.steatosisOfLiver!!, recordedDate))
    }
    if (lm.cirrhosisOfLiver != null) {
        add(AnaChronicLiver(patient, ChronicLiverDiseases.CIRRHOSIS_OF_LIVER, lm.cirrhosisOfLiver!!, recordedDate))
    }
    if (lm.chronicViralHepatitis != null) {
        add(
            AnaChronicLiver(
                patient,
                ChronicLiverDiseases.CHRONIC_VIRAL_HEPATITIS,
                lm.chronicViralHepatitis!!,
                recordedDate
            )
        )
    }
    if (lm.autoimmuneLiverDisease != null) {
        add(
            AnaChronicLiver(
                patient,
                ChronicLiverDiseases.AUTOIMMUNE_LIVER_DISEASE,
                lm.autoimmuneLiverDisease!!,
                recordedDate
            )
        )
    }
}


private fun GeccoBundleBuilder.addChronicLungDiseases(
    lm: AnamnesisChronicLungDiseases,
    patientRef: Reference,
    recordedDate: DateTimeType
) {
    if (lm.asthma != null) {
        add(AnaChronicLungDisease(patientRef, ChronicLungDisease.ASTHMA, lm.asthma!!, recordedDate))
    }
    if (lm.copd != null) {
        add(AnaChronicLungDisease(patientRef, ChronicLungDisease.COPD, lm.copd!!, recordedDate))
    }
    if (lm.cysticFibrosis != null) {
        add(AnaChronicLungDisease(patientRef, ChronicLungDisease.CYSTIC_FIBROSIS, lm.cysticFibrosis!!, recordedDate))
    }
    if (lm.fibrosis != null) {
        add(AnaChronicLungDisease(patientRef, ChronicLungDisease.FIBROSIS, lm.fibrosis!!, recordedDate))
    }
    if (lm.ohs != null) {
        add(AnaChronicLungDisease(patientRef, ChronicLungDisease.OHS, lm.ohs!!, recordedDate))
    }
    if (lm.osas != null) {
        add(AnaChronicLungDisease(patientRef, ChronicLungDisease.OSAS, lm.osas!!, recordedDate))
    }
    if (lm.pulmonaryHypertension != null) {
        add(
            AnaChronicLungDisease(
                patientRef,
                ChronicLungDisease.PULMONARY_HYPERTENSION,
                lm.pulmonaryHypertension!!,
                recordedDate
            )
        )
    }
    if (lm.sleepApnea != null) {
        add(AnaChronicLungDisease(patientRef, ChronicLungDisease.SLEEP_APNEA, lm.sleepApnea!!, recordedDate))
    }
}

private fun GeccoBundleBuilder.addCardiovascularDiseases(
    lm: AnamnesisCardiovascularDiseases,
    patient: Reference,
    recordedDate: DateTimeType
) {
    if (lm.arterialHyptertension != null) {
        add(AnaCardiovascular(patient, HYPERTENSIVE_DISORDER_SYSTEMIC_ARTERIAL, lm.cardiacArrhytmia!!, recordedDate))
    }
    if (lm.cardiacArrhytmia != null) {
        add(AnaCardiovascular(patient, CARDIAC_ARRHYTHMIA, lm.cardiacArrhytmia!!, recordedDate))
    }
    if (lm.carotidArteryStenosis != null) {
        add(AnaCardiovascular(patient, CAROTID_ARTERY_STENOSIS, lm.carotidArteryStenosis!!, recordedDate))
    }
    if (lm.coronaryArteriosclerosis != null) {
        add(AnaCardiovascular(patient, CORONARY_ARTERIOSCLEROSIS, lm.coronaryArteriosclerosis!!, recordedDate))
    }
    if (lm.heartFailure != null) {
        add(AnaCardiovascular(patient, HEART_FAILURE, lm.heartFailure!!, recordedDate))
    }
    if (lm.peripherialArterialOcclusiveDisease != null) {
        add(
            AnaCardiovascular(
                patient,
                PERIPHERAL_ARTERIAL_OCCLUSIVE_DISEASE,
                lm.peripherialArterialOcclusiveDisease!!,
                recordedDate
            )
        )
    }
    if (lm.stateAfterHeartAttack != null) {
        add(AnaCardiovascular(patient, ZUSTAND_NACH_HERZINFARKT, lm.stateAfterHeartAttack!!, recordedDate))
    }
    if (lm.stateAfterRevascularization != null) {
        add(AnaCardiovascular(patient, ZUSTAND_NACH_REVASKULARISATION, lm.stateAfterRevascularization!!, recordedDate))
    }
}


private fun GeccoBundleBuilder.addCovid19Vaccine(logicalModel: Covid19Vaccination?, patientRef: Reference) {
    if (logicalModel != null && logicalModel.status == YesNoUnknown.YES) {
        val vaccineDate = logicalModel.date?.let { DateTimeType(it.toUtilDate()) } ?: unknownDateTime()
        if (logicalModel.vaccine != null) {
            add(VaccinationCovid19(patientRef, logicalModel.vaccine as Covid19Vaccine, vaccineDate))
        }
    }
}

private fun GeccoBundleBuilder.addMediAntiCoag(
    patientRef: Reference,
    antiCoagulant: AntiCoagulant,
    yesNoUnknownWithIntent: YesNoUnknownWithIntent?
) {
    if (yesNoUnknownWithIntent != null && yesNoUnknownWithIntent.administration == YesNoUnknown.YES) {
        add(MediAntiCoag(patientRef, antiCoagulant, unknownDateTime(), yesNoUnknownWithIntent.intent))
    }
}


