import Medication as Medi
import ca.uhn.fhir.context.FhirContext
import org.hl7.fhir.r4.model.*
import java.time.LocalDate
import java.util.*
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.javaSetter
import kotlin.reflect.jvm.jvmErasure


fun main() {
    val ctx = FhirContext.forR4()
    val parser = ctx.newJsonParser().setPrettyPrint(true)
    val qr = parser.parseResource(
        LogicalModel::class.java.getResource("/generated-response.json").readText()
    ) as QuestionnaireResponse
    val q =
        parser.parseResource(LogicalModel::class.java.getResource("/questionnaire.json").readText()) as Questionnaire
    addExtensions(qr, q)
    parser.encodeResourceToString(qr)
    val logicalModel = toLogicalModel(qr)
    println(logicalModel.toString())
}

fun addExtensions(response: QR, questionnaire: Questionnaire) {
    for (qrItem in response.item) {
        val qItem = questionnaire.item.find { it.linkId == qrItem.linkId }
            ?: throw UnknownLinkIdException(qrItem.linkId)
        addExtensions(qrItem, qItem)
    }
}

fun addExtensions(qrItem: QRItem, qItem: QItem) {
    for (answerComponent in qrItem.answer) {
        for (itemComponent in answerComponent.item) {
            val qItem = qItem.item.find { it.linkId == itemComponent.linkId }
                ?: throw UnknownLinkIdException(itemComponent.linkId)
            addExtensions(itemComponent, qItem)
        }
    }
    for (itemComponent in qrItem.item) {
        val qItem = qItem.item.find { it.linkId == itemComponent.linkId }
            ?: throw UnknownLinkIdException(itemComponent.linkId)
        addExtensions(itemComponent, qItem)
    }

    qrItem.extension = qItem.extension.map { it.copy() }

}

class UnknownLinkIdException(val linkId: String): Exception() {
    override val message: String
        get() = "Encountered linkId '${linkId}' in QuestionnaireResponse, which does not exist in Questionnaire! Please make sure, that Questionnaire and QuestionnaireResponse are corresponding to each other!"
}


fun toYesNoUnknown(value: Type?): YesNoUnknown? {
    if (value == null) {
        return null
    }
    if (value is Coding) {
        val result = getByCoding<YesNoUnknown>(value)
        if (result != null) {
            return result
        }
    }
    if (value is BooleanType) {
        return if(value.booleanValue()) YesNoUnknown.YES else YesNoUnknown.NO
    }
    val answerValue = when (value) {
        is StringType -> value.toString()
        is Coding -> value.display?.toString()
        else -> null
    }
    return when (answerValue?.lowercase()) {
        "ja", "yes" -> YesNoUnknown.YES
        "nein", "no" -> YesNoUnknown.NO
        "unbekannt", "unknown" -> YesNoUnknown.UNKNOWN
        else -> null
    }
}


fun toYesNoUnknownOtherNa(value: Type?): YesNoUnknownOtherNa? {
    if (value == null) {
        return null
    }
    if (value is Coding) {
        val result = getByCoding<YesNoUnknownOtherNa>(value)
        if (result != null) {
            return result
        }
    }

    val answerValue = when (value) {
        is StringType -> value.toString()
        is Coding -> value.display?.toString()
        else -> null
    }
    return when (answerValue?.toLowerCase()) {
        "ja", "yes" -> YesNoUnknownOtherNa.YES
        "nein", "no" -> YesNoUnknownOtherNa.NO
        "andere", "other" -> YesNoUnknownOtherNa.OTHER
        "na", "not applicable", "nicht anwendbar", "unzutreffend" -> YesNoUnknownOtherNa.NA
        "unbekannt", "unknown" -> YesNoUnknownOtherNa.UNKNOWN
        else -> null
    }
}


fun toLogicalModel(response: QuestionnaireResponse): LogicalModel {
    //var logicalModel = LogicalModel()
    val mapByExtension = mapByExtension(response)

    val logicalModel = LogicalModel(
        Anamnesis(
            hasChronicLungDiseases = toYesNoUnknown(mapByExtension["anamnesis.hasChronicLungDiseases"]?.value),
            chronicLungDiseases = AnamnesisChronicLungDiseases(
                asthma = toYesNoUnknown(mapByExtension["anamnesis.chronicLungDiseases.asthma"]?.value),
                copd = toYesNoUnknown(mapByExtension["anamnesis.chronicLungDiseases.copd"]?.value),
                fibrosis = toYesNoUnknown(mapByExtension["anamnesis.chronicLungDiseases.fibrosis"]?.value),
                pulmonaryHypertension = toYesNoUnknown(mapByExtension["anamnesis.chronicLungDiseases.pulmonaryHypertension"]?.value),
                ohs = toYesNoUnknown(mapByExtension["anamnesis.chronicLungDiseases.ohs"]?.value),
                sleepApnea = toYesNoUnknown(mapByExtension["anamnesis.chronicLungDiseases.sleepApnea"]?.value),
                osas = toYesNoUnknown(mapByExtension["anamnesis.chronicLungDiseases.osas"]?.value),
                cysticFibrosis = toYesNoUnknown(mapByExtension["anamnesis.chronicLungDiseases.cysticFibrosis"]?.value),
            ),
            hasCardiovascularDiseases = toYesNoUnknown(mapByExtension["anamnesis.hasCardiavascularDiseases"]?.value),
            cardiovascularDiseases = AnamnesisCardiovascularDiseases(
                stateAfterHeartAttack = toYesNoUnknown(mapByExtension["anamnesis.cardiovascularDiseases.stateAfterHeartAttack"]?.value),
                cardiacArrhytmia = toYesNoUnknown(mapByExtension["anamnesis.cardiovascularDiseases.cardiacArrhytmia"]?.value),
                heartFailure = toYesNoUnknown(mapByExtension["anamnesis.cardiovascularDiseases.heartFailure"]?.value),
                peripherialArterialOcclusiveDisease = toYesNoUnknown(mapByExtension["anamnesis.cardiovascularDiseases.peripherialArterialOcclusiveDisease"]?.value),
                stateAfterRevascularization = toYesNoUnknown(mapByExtension["anamnesis.cardiovascularDiseases.stateAfterRevascularization"]?.value),
                coronaryArteriosclerosis = toYesNoUnknown(mapByExtension["anamnesis.cardiovascularDiseases.coronaryArteriosclerosis"]?.value),
                carotidArteryStenosis = toYesNoUnknown(mapByExtension["anamnesis.cardiovascularDiseases.carotidArteryStenosis"]?.value),
            ),
            hasChronicLiverDiseases = toYesNoUnknown(mapByExtension["anamnesis.hasChronicLiverDiseases"]?.value),
            chronicLiverDiseases = AnamnesisChronicLiverDiseases(
                steatosisOfLiver = toYesNoUnknown(mapByExtension["anamnesis.chronicLiverDiseases.steatosisOfLiver"]?.value),
                cirrhosisOfLiver = toYesNoUnknown(mapByExtension["anamnesis.chronicLiverDiseases.cirrhosisOfLiver"]?.value),
                chronicViralHepatitis = toYesNoUnknown(mapByExtension["anamnesis.chronicLiverDiseases.chronicViralHepatitis"]?.value),
                autoimmuneLiverDisease = toYesNoUnknown(mapByExtension["anamnesis.chronicLiverDiseases.autoimmuneLiverDisease"]?.value),
            ),
            hasRheumatologicalImmunologicalDiseases = toYesNoUnknown(mapByExtension["anamnesis.hasRheumatologicalImmunologicalDiseases"]?.value),
            rheumatologicalImmunologicalDiseases = AnamnesisRheumatologicalImmunologicalDiseases(
                inflammatoryBowelDisease = toYesNoUnknown(mapByExtension["anamnesis.rheumatologicalImmunologicalDiseases.inflammatoryBowelDisease"]?.value),
                rheumatoidArthritis = toYesNoUnknown(mapByExtension["anamnesis.rheumatologicalImmunologicalDiseases.rheumatoidArthritis"]?.value),
                collagenosis = toYesNoUnknown(mapByExtension["anamnesis.rheumatologicalImmunologicalDiseases.collagenosis"]?.value),
                vasculitis = toYesNoUnknown(mapByExtension["anamnesis.rheumatologicalImmunologicalDiseases.vasculitis"]?.value),
                congenitalImmunodeficiencyDisease = toYesNoUnknown(mapByExtension["anamnesis.rheumatologicalImmunologicalDiseases.congenitalImmunodeficiencyDisease"]?.value),
            ),
            hasHivInfection = toYesNoUnknown(mapByExtension["anamnesis.hasHivInfection"]?.value),
            hasHistoryOfBeingATissueOrOrganRecipient = toYesNoUnknown(mapByExtension["anamnesis.hasHistoryOfBeingATissueOrOrganRecipient"]?.value),
            historyOfBeingATissueOrOrganRecipient = AnamnesisHistoryOfBeingATissueOrOrganRecipient(
                entireHeart = toYesNoUnknown(mapByExtension["anamnesis.historyOfBeingATissueOrOrganRecipient.entireHeart"]?.value),
                entireLung = toYesNoUnknown(mapByExtension["anamnesis.historyOfBeingATissueOrOrganRecipient.entireLung"]?.value),
                entireLiver = toYesNoUnknown(mapByExtension["anamnesis.historyOfBeingATissueOrOrganRecipient.entireLiver"]?.value),
                entireKidney = toYesNoUnknown(mapByExtension["anamnesis.historyOfBeingATissueOrOrganRecipient.entireKidney"]?.value),
                entirePancreas = toYesNoUnknown(mapByExtension["anamnesis.chronicLungDiseases.entirePancreas"]?.value),
                intestinalStructure = toYesNoUnknown(mapByExtension["anamnesis.historyOfBeingATissueOrOrganRecipient.intestinalStructure"]?.value),
                entireSmallIntestine = toYesNoUnknown(mapByExtension["anamnesis.historyOfBeingATissueOrOrganRecipient.entireSmallIntestine"]?.value),
                entireLargeIntestine = toYesNoUnknown(mapByExtension["anamnesis.historyOfBeingATissueOrOrganRecipient.entireLargeIntestine"]?.value),
                skinPart = toYesNoUnknown(mapByExtension["anamnesis.historyOfBeingATissueOrOrganRecipient.skinPart"]?.value),
                entireCornea = toYesNoUnknown(mapByExtension["anamnesis.historyOfBeingATissueOrOrganRecipient.entireCornea"]?.value),
                earOssicleStructure = toYesNoUnknown(mapByExtension["anamnesis.historyOfBeingATissueOrOrganRecipient.earOssicleStructure"]?.value),
                entireHeartValve = toYesNoUnknown(mapByExtension["anamnesis.historyOfBeingATissueOrOrganRecipient.entireHeartValve"]?.value),
                bloodVesselPart = toYesNoUnknown(mapByExtension["anamnesis.historyOfBeingATissueOrOrganRecipient.bloodVesselPart"]?.value),
                cerebralMeningitisStructure = toYesNoUnknown(mapByExtension["anamnesis.historyOfBeingATissueOrOrganRecipient.cerebralMeningitisStructure"]?.value),
                boneTissueOrStructure = toYesNoUnknown(mapByExtension["anamnesis.historyOfBeingATissueOrOrganRecipient.boneTissueOrStructure"]?.value),
                cartilageTissue = toYesNoUnknown(mapByExtension["anamnesis.historyOfBeingATissueOrOrganRecipient.cartilageTissue"]?.value),
                tendonStructure = toYesNoUnknown(mapByExtension["anamnesis.historyOfBeingATissueOrOrganRecipient.tendonStructure"]?.value),
            ),
            hasDiabetesMellitus = toYesNoUnknown(mapByExtension["anamnesis.hasDiabetesMellitus"]?.value),
            diabetesMellitus = mapByExtension["anamnesis.diabetesMellitus.diabetesMellitusType1"]?.toEnum2<Diabetes>(),
            malignantNeoplasticDiseases = mapByExtension["anamnesis.malignantNeoplasticDiseases"]?.toEnum1<CancerStatus>(),
            tobaccoSmokingStatus = mapByExtension["anamnesis.tobaccoSmokingStatus"]?.toEnum2<SmokingStatus>(),
            hasChronicNeurologicalOrMentalDiseases = toYesNoUnknown(mapByExtension["anamnesis.hasChronicNeurologicalOrMentalDiseases"]?.value),
            chronicNeurologicalOrMentalDiseases = AnamnesisChronicNeurologicalOrMentalDiseases(
//                chronicNervousSystemDisorder = toYesNoUnknown(mapByExtension["anamnesis.chronicLungDiseases.chronicNervousSystemDisorder"]?.value),
//                mentalDisorder = toYesNoUnknown(mapByExtension["anamnesis.chronicNeurologicalOrMentalDiseases.mentalDisorder"]?.value),
                anxietyDisorder = toYesNoUnknown(mapByExtension["anamnesis.chronicNeurologicalOrMentalDiseases.anxietyDisorder"]?.value),
                depressiveDisorder = toYesNoUnknown(mapByExtension["anamnesis.chronicNeurologicalOrMentalDiseases.depressiveDisorder"]?.value),
                psychoticDisorder = toYesNoUnknown(mapByExtension["anamnesis.chronicNeurologicalOrMentalDiseases.psychoticDisorder"]?.value),
                parkinsonDisorder = toYesNoUnknown(mapByExtension["anamnesis.chronicNeurologicalOrMentalDiseases.parkinsonDisorder"]?.value),
                dementia = toYesNoUnknown(mapByExtension["anamnesis.chronicNeurologicalOrMentalDiseases.dementia"]?.value),
                multipleSclerosis = toYesNoUnknown(mapByExtension["anamnesis.chronicNeurologicalOrMentalDiseases.multipleSclerosis"]?.value),
                combinedDisorderOfMuscleAndPeripheralNerve = toYesNoUnknown(mapByExtension["anamnesis.chronicNeurologicalOrMentalDiseases.combinedDisorderOfMuscleAndPeripheralNerve"]?.value),
                epilepsy = toYesNoUnknown(mapByExtension["anamnesis.chronicNeurologicalOrMentalDiseases.epilepsy"]?.value),
                migraine = toYesNoUnknown(mapByExtension["anamnesis.chronicNeurologicalOrMentalDiseases.migraine"]?.value),
                historyOfCerebrovascularAccidentWithResidualDeficit = toYesNoUnknown(mapByExtension["anamnesis.chronicNeurologicalOrMentalDiseases.historyOfCerebrovascularAccidentWithResidualDeficit"]?.value),
                historyOfCerebrovascularAccidentWithoutResidualDeficits = toYesNoUnknown(mapByExtension["anamnesis.chronicNeurologicalOrMentalDiseases.historyOfCerebrovascularAccidentWithoutResidualDeficits"]?.value),
            ),
            hasHadOxygenOrRespiratoryTherapyBeforeCurrentIllness = toYesNoUnknown(mapByExtension["anamnesis.hasHadOxygenOrRespiratoryTherapyBeforeCurrentIllness"]?.value),
            chronicKidneyDisease = ChronicKidneyDisease.from(CodeableConcept(mapByExtension["anamnesis.chronicKidneyDisease"]?.valueCoding)),
            hasTravelled = toYesNoUnknown(mapByExtension["anamnesis.hasTravelled"]?.value),
            historyOfTravel = AnamnesisHistoryOfTravel(
                country = mapByExtension["anamnesis.historyOfTravel.country"]?.toEnum1<Countries>(),
                federalState = mapByExtension["anamnesis.historyOfTravel.federalState"]?.toEnum1<FederalStates>()
            ),
            hasGastrointestinalUclers = toYesNoUnknown(mapByExtension["anamnesis.hasGastrointestinalUclers"]?.value),
//            hasImmunization = toYesNoUnknown(mapByExtension["anamnesis.hasImmunization"]?.value),
            immunizationStatus = AnamnesisImmunizationStatus(
                influenza = YesNoUnknownWithDate(
                    toYesNoUnknown(mapByExtension["anamnesis.immunizationStatus.influenza"]?.value),
                    (mapByExtension["anamnesis.immunizationStatus.influenza.Datum"]?.value as? BaseDateTimeType)?.toCalendar()?.time
                ),
                pneumococcal = YesNoUnknownWithDate(
                    toYesNoUnknown(mapByExtension["anamnesis.immunizationStatus.pneumococcal"]?.value),
                    (mapByExtension["anamnesis.immunizationStatus.pneumococcal.Datum"]?.value as? BaseDateTimeType)?.toCalendar()?.time
                ),
                bcg = YesNoUnknownWithDate(
                    toYesNoUnknown(mapByExtension["anamnesis.immunizationStatus.bcg"]?.value),
                    (mapByExtension["anamnesis.immunizationStatus.bcg.Datum"]?.value as? BaseDateTimeType)?.toCalendar()?.time
                ),

                covid19_first = extractCovid19Vaccination("anamnesis.immunizationStatus.covid19_first", mapByExtension),
                covid19_second = extractCovid19Vaccination("anamnesis.immunizationStatus.covid19_second", mapByExtension),
                covid19_third = extractCovid19Vaccination("anamnesis.immunizationStatus.covid19_third", mapByExtension),
            ),
            resuscitateOrder = mapByExtension["anamnesis.resuscitateOrder"]?.toEnum1<Resuscitation>(),
        ),
        Imaging(
            hasHadImagingProcedures = toYesNoUnknown(mapByExtension["imaging.hasHadImagingImagingProcedures"]?.value),
            imagingProcedures = ImagingProcedures(
                computedTomography = toYesNoUnknown(mapByExtension["imaging.imagingProcedures.computedTomography"]?.value),
                radiographicImaging = toYesNoUnknown(mapByExtension["imaging.imagingProcedures.radiographicImaging"]?.value),
                ultrasound = toYesNoUnknown(mapByExtension["imaging.imagingProcedures.ultrasound"]?.value),
            ),
            hasRadiologicalFindings = toYesNoUnknown(mapByExtension["imaging.hasRadiologicalFindings"]?.value),
            radiologicalFindings = RadiologicFindings.from(CodeableConcept(mapByExtension["imaging.radiologicalFindings"]?.valueCoding))
        ),
        //Todo: Demographics
        Demographics(
            ageInYears = mapByExtension["demographics.ageInYears"]?.toInt(),
            ageInMonth = mapByExtension["demographics.ageInMonth"]?.toInt(),
            biologicalSex = mapByExtension["demographics.biologicalSex"]?.toEnum1<BirthSex>(),
            ethnicGroup = mapByExtension["demographics.bodyHeight"]?.toEnum1<EthnicGroup>(),
            pregnancyStatus = mapByExtension["demographics.pregnancyStatus"]?.toEnum2<PregnancyStatus>(),
            frailityScore = mapByExtension["demographics.frailityScore"]?.toEnum2<FrailityScore>(),
            bodyHeight = mapByExtension["demographics.bodyHeight"]?.toDecimal(),
            bodyWeight = mapByExtension["demographics.bodyWeight"]?.toDecimal(),
            dateOfBirth = mapByExtension["demographics.dateOfBirth"]?.toLocalDate()
        ),
        EpidemiologicalFactors(
            knownCovid19Exposure = toYesNoUnknown(mapByExtension["epidemiologicalFactors.knownCovid19Exposure"]?.value),
        ),
        Complications(
            hasHadThromboembolicComplications = toYesNoUnknown(mapByExtension["complications.thrombosis"]?.value),
            thromboembolicComplications = ThromboembolicComplications(
                venousThrombosis = toYesNoUnknown(mapByExtension["complications.thromboembolicComplications.venomousThrombosis"]?.value),
                pulmonaryEmbolism = toYesNoUnknown(mapByExtension["complications.thromboembolicComplications.pulmonaryEmbolism"]?.value),
                cerebrovascularAccident = toYesNoUnknown(mapByExtension["complications.thromboembolicComplications.cerebrovascularAccident"]?.value),
                myocardialInfarction = toYesNoUnknown(mapByExtension["complications.thromboembolicComplications.myocardialInfarction"]?.value),
                embolism = toYesNoUnknown(mapByExtension["complications.thromboembolicComplications.embolism"]?.value),
                thrombosis = toYesNoUnknown(mapByExtension["complications.thromboembolicComplications.thrombosis"]?.value),
            ),
            infectiousDiseaseOfLung = toYesNoUnknown(mapByExtension["complications.complication.infectiousDiseaseOfLung"]?.value),
            infectiousAgentInBloodstream = toYesNoUnknown(mapByExtension["complications.complication.infectiousAgentInBloodstream"]?.value),
            acuteRenalFailureSyndrome = toYesNoUnknown(mapByExtension["complications.complication.acuteRenalFailureSyndrome"]?.value),
        ),
        OnsetOfIllnessOrAdmission(
            stageAtDiagnosis = mapByExtension["onsetOfIllnessOrAdmission.stageAtDiagnosis"]?.toEnum2<StageAtDiagnosis>()
        ),
        LaboratoryValues(
            extractLab(mapByExtension),
            sarsCov2RtPcrResult = toYesNoUnknown(mapByExtension["laboratoryValues.laboratoryValue.sarsCov2RtPcrResult"]?.value),
            sarsCov2AntibodiesResult = toYesNoUnknown(mapByExtension["laboratoryValues.laboratoryValue.sarsCov2AntibodiesResult"]?.value),
            ),
        Medi(
            hadCovid19Therapy = toYesNoUnknown(mapByExtension["medication.hadCovid19Therapy"]?.value),
            covid19Therapy = MedicationCovid19Therapy(
                productContainingSteroid = toYesNoUnknown(mapByExtension["medication.covid19Therapy.productContainingSteroid"]?.value),
                productContainingAtazanavir = toYesNoUnknown(mapByExtension["medication.covid19Therapy.productContainingAtazanavir"]?.value),
                productContainingDarunavir = toYesNoUnknown(mapByExtension["medication.covid19Therapy.productContainingDarunavir"]?.value),
                productContainingChloroquine = toYesNoUnknown(mapByExtension["medication.covid19Therapy.productContainingChloroquine"]?.value),
                productContainingHydroxychloroquine = toYesNoUnknown(mapByExtension["medication.covid19Therapy.productContainingHydroxychloroquine"]?.value),
                productContainingIvermectin = toYesNoUnknown(mapByExtension["medication.covid19Therapy.productContainingIvermectin"]?.value),
                productContainingLopinavirAndRitonavir = toYesNoUnknown(mapByExtension["medication.covid19Therapy.productContainingLopinavirAndRitonavir"]?.value),
                productContainingGanciclovir = toYesNoUnknown(mapByExtension["medication.covid19Therapy.productContainingGanciclovir"]?.value),
                productContainingOseltamivir = toYesNoUnknown(mapByExtension["medication.covid19Therapy.productContainingOseltamivir"]?.value),
                productContainingRemdesivir = toYesNoUnknown(mapByExtension["medication.covid19Therapy.productContainingRemdesivir"]?.value),
                productContainingRibavirin = toYesNoUnknown(mapByExtension["medication.covid19Therapy.productContainingRibavirin"]?.value),
                productContainingTocilizumab = toYesNoUnknown(mapByExtension["medication.covid19Therapy.productContainingTocilizumab"]?.value),
                productContainingSarilumab = toYesNoUnknown(mapByExtension["medication.covid19Therapy.productContainingSarilumab"]?.value),
                productContainingCalcineurinInhibitor = toYesNoUnknown(mapByExtension["medication.covid19Therapy.productContainingCalcineurinInhibitor"]?.value),
                productContainingTumorNecrosisFactorAlphaInhibitor = toYesNoUnknown(mapByExtension["medication.covid19Therapy.productContainingTumorNecrosisFactorAlphaInhibitor"]?.value),
                productContainingInterleukin1ReceptorAntagonist = toYesNoUnknown(mapByExtension["medication.covid19Therapy.productContainingInterleukin1ReceptorAntagonist"]?.value),
                productContainingRuxolitinib = toYesNoUnknown(mapByExtension["medication.covid19Therapy.productContainingRuxolitinib"]?.value),
                productContainingColchicine = toYesNoUnknown(mapByExtension["medication.covid19Therapy.productContainingColchicine"]?.value),
                productContainingInterferon = toYesNoUnknown(mapByExtension["medication.covid19Therapy.productContainingInterferon"]?.value),
                productContainingCalcifediol = toYesNoUnknown(mapByExtension["medication.covid19Therapy.productContainingCalcifediol"]?.value),
                productContainingAntipyretic = toYesNoUnknown(mapByExtension["medication.covid19Therapy.productContainingAntipyretic"]?.value),
                productContainingCamostat = toYesNoUnknown(mapByExtension["medication.covid19Therapy.productContainingCamostat"]?.value),
                productContainingFavipiravir = toYesNoUnknown(mapByExtension["medication.covid19Therapy.productContainingFavipiravir"]?.value),
                productContainingPlasma = toYesNoUnknown(mapByExtension["medication.covid19Therapy.productContainingPlasma"]?.value),
                productContainingZinc = toYesNoUnknown(mapByExtension["medication.covid19Therapy.productContainingZinc"]?.value),
                steroidsGtHalfMgPerKgPrednisoneEquivalents = toYesNoUnknown(mapByExtension["medication.covid19Therapy.steroidsGtHalfMgPerKgPrednisoneEquivalents"]?.value),
                streoidsLtHalfMgPerKgPrednisoneEquivalents = toYesNoUnknown(mapByExtension["medication.covid19Therapy.streoidsLtHalfMgPerKgPrednisoneEquivalents"]?.value),
            ),
            aceInhibitors = mapByExtension["medication.aceInhibitors"]?.toEnum1<ACEInhibitorAdministration>(),
            immunoglobulins = toYesNoUnknown(mapByExtension["medication.immunoglobulins"]?.value),
            hadAnticoagulation = toYesNoUnknown(mapByExtension["medication.hadAnticoagulation"]?.value),
            anticoagulation = MedicationAnticoagulation(
                //TODO
//                prophylacticalIntent = toYesNoUnknown(mapByExtension["medication.anticoagulation.prophylacticalIntent"]?.value),
//                therapeuticalIntent = toYesNoUnknown(mapByExtension["medication.anticoagulation.therapeuticalIntent"]?.value),
//                unfractionatedHeparin = toYesNoUnknown(mapByExtension["medication.anticoagulation.unfractionatedHeparin"]?.value),
//                lowMolecularWeightHeparin = toYesNoUnknown(mapByExtension["medication.anticoagulation.lowMolecularWeightHeparin"]?.value),
//                argatroban = toYesNoUnknown(mapByExtension["medication.anticoagulation.argatroban"]?.value),
//                plateletAggregationInhibitor = toYesNoUnknown(mapByExtension["medication.anticoagulation.plateletAggregationInhibitor"]?.value),
//                danaparpoid = toYesNoUnknown(mapByExtension["medication.anticoagulation.danaparpoid"]?.value),
//                phenprocoumon = toYesNoUnknown(mapByExtension["medication.anticoagulation.phenprocoumon"]?.value),
//                directOralAnticoagulants = toYesNoUnknown(mapByExtension["medication.anticoagulation.directOralAnticoagulants"]?.value),
//                thrombosisProphylaxix = toYesNoUnknown(mapByExtension["medication.anticoagulation.thrombosisProphylaxix"]?.value),
//                therapeuticAnticoagulation = toYesNoUnknown(mapByExtension["medication.anticoagulation.therapeuticAnticoagulation"]?.value),
            )
        ),
        OutcomeAtDischarge(
            respiratoryOutcomeisVentilated = toYesNoUnknown(mapByExtension["outcomeAtDischarge.respiratoryOutcomeisVentilated"]?.value),
            typeOfDischarge = TypeOfDischarge.ALIVE,
//            followupSwapResultIsPositive = toYesNoUnknown(mapByExtension["outcomeAtDischarge.followupSwapResultIsPositive"]?.value),
        ),
        StudyEnrollmentOrInclusionCriteria(
            enrolledWithCovid19DiagnosisAsMainReason = toYesNoUnknownOtherNa(mapByExtension["studyEnrollmentOrInclusionCriteria.enrolledWithCovid19DiagnosisAsMainReason"]?.value),
            hasPatientParticipatedInOneOrMoreInterventionalClinicalTrials = toYesNoUnknownOtherNa(mapByExtension["studyEnrollmentOrInclusionCriteria.hasPatientParticipatedInOneOrMoreInterventionalClinicalTrials"]?.value),
        ),
        extractSymptoms(mapByExtension),
        Therapy(
            dialysisOrHemofiltration = toYesNoUnknown(mapByExtension["therapy.dialysisOrHemofiltration"]?.value),
            apheresis = toYesNoUnknown(mapByExtension["therapy.apheresis"]?.value),
            pronePosition = toYesNoUnknown(mapByExtension["therapy.pronePosition"]?.value),
            ecmoTherapy = toYesNoUnknown(mapByExtension["therapy.ecmoTherapy"]?.value),
            isPatientInTheIntensiveCareUnit = toYesNoUnknown(mapByExtension["therapy.isPatientInTheIntensiveCareUnit"]?.value),
            ventilationType = VentilationTypes.UNKNOWN
        ),
        VitalSigns(
            pacCO2 = (mapByExtension["vitalSigns.pacCO2"]?.value as? DecimalType)?.value?.toFloat(),
            paO2 = (mapByExtension["vitalSigns.paO2"]?.value as? DecimalType)?.value?.toFloat(),
            FiO2 = (mapByExtension["vitalSigns.FiO2"]?.value as? DecimalType)?.value?.toFloat(),
            pH = (mapByExtension["vitalSigns.pH"]?.value as? DecimalType)?.value?.toFloat(),
            sofaScore = (mapByExtension["vitalSigns.SOFAScore"]?.value as? IntegerType)?.value?.toInt(),
            respiratoryRate = (mapByExtension["vitalSigns.respiratoryRate"]?.value as? IntegerType)?.value?.toInt(),
            diastolicBloodPressure = (mapByExtension["vitalSigns.diastolicBloodPressure"]?.value as? IntegerType)?.value?.toInt(),
            systolicBloodPressure = (mapByExtension["vitalSigns.systolicBloodPressure"]?.value as? IntegerType)?.value?.toInt(),
            heartRate = (mapByExtension["vitalSigns.heartRate"]?.value as? IntegerType)?.value?.toInt(),
            bodyTemperature = (mapByExtension["vitalSigns.bodyTemperature"]?.value as? DecimalType)?.value?.toFloat(),
            peripheralOxygenSaturation = (mapByExtension["vitalSigns.peripheralOxygenSaturation"]?.value as? DecimalType)?.value?.toFloat()
        ),
    )
    return logicalModel
}

fun extractCovid19Vaccination(prefix: String, mapByExtension: HashMap<String, QRAnswer>) =
    Covid19Vaccination(
        toYesNoUnknown(mapByExtension["$prefix.status"]?.value),
        mapByExtension["$prefix.date"]?.toLocalDate(),
        mapByExtension["$prefix.vaccine"]?.toEnum1<Covid19Vaccine>()
    )

fun extractSymptoms(mapByExtension: HashMap<String, QRAnswer>): Symptoms {
    val result = Symptoms()
    for (property in result::class.declaredMemberProperties) {
        if (property is KMutableProperty<*>) {
            val presence = toYesNoUnknown(mapByExtension["symptoms.${property.name}.presence"]?.value)
            val severity =
                if (presence == YesNoUnknown.YES) mapByExtension["symptoms.${property.name}.severity"]?.toEnum1<SymptomSeverity>() else null

            val value = if (property.returnType.jvmErasure == YesNoUnknownWithSymptomSeverity::class) {
                YesNoUnknownWithSymptomSeverity(presence, severity)
            } else {
                presence
            }

            try {
                property.javaSetter!!.invoke(result, value)
            } catch (e: Exception) {
                println("${property.name}    presence = ${presence} severity = ${severity} value = ${value}")
                println(e)
            }
        }
    }
    return result
}

fun extractLab(mapByExtension: HashMap<String, QRAnswer>): LaboratoryValuesLaboratoryValue {
    val result = LaboratoryValuesLaboratoryValue()
    for (property in result::class.declaredMemberProperties) {
        if (property is KMutableProperty<*>) {
            val valueRaw = mapByExtension["laboratoryValues.laboratoryValues.${property.name}"]?.value
            try {
                property.javaSetter!!.invoke(result, when(valueRaw) {
                    is DecimalType -> valueRaw.value.toFloat()
                    is StringType -> valueRaw.value
                    is Coding -> toYesNoUnknown(valueRaw)
                    else -> null
                })
                val extension = mapByExtension["laboratoryValues.laboratoryValues.${property.name}"]?.getExtensionByUrl(
                    "http://hl7.org/fhir/StructureDefinition/questionnaire-unit")?.value
            } catch (e: Exception) {
                println(property.name + "   " + valueRaw)
                println(e)
            }
        }
    }
    //TODO: Test this function
    return result
}


inline fun <reified T> QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent.toEnum1(): T? where T : CodeableEnum<T>, T : Enum<T> {
    return (this.value as? Coding)?.let { getByCoding<T>(it) }
}

inline fun <reified T> QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent.toEnum2(): T? where T : ConceptEnum<T>, T : Enum<T> {
    return (this.value as? Coding)?.let { getByCoding2<T>(it) }
}

fun QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent.toDecimal(): Float? {
    return (this.value as? DecimalType)?.value?.toFloat()
}

fun QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent.toInt(): Int? {
    return (this.value as? IntegerType)?.value
}

fun QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent.toLocalDate(): LocalDate? {
    val dateType = this.value as? DateType
    return dateType?.let { LocalDate.of(it.year, it.month, it.day) }
}


fun mapByExtension(response: QR, hashMap: HashMap<String, QRAnswer> = HashMap()): HashMap<String, QRAnswer> {
    response.item.forEach { itemComponent -> itemComponent.answer?.forEach { mapByExtension(it, hashMap) } }
    response.item.forEach { mapByExtension(it, hashMap) }
//    response.extension?.forEach { extension -> hashMap.put((extension.value as CodeType).code, responseAnswer)}
    return hashMap;
}


fun mapByExtension(responseItem: QRItem, hashMap: HashMap<String, QRAnswer> = HashMap()): HashMap<String, QRAnswer> {
    responseItem.item.filterNotNull().forEach { itemComponent -> itemComponent.answer?.forEach { mapByExtension(it, hashMap) } }
    responseItem.item.forEach { mapByExtension(it, hashMap) }

    val extension = responseItem.getExtensionByUrl("https://num-compass.science/fhir/StructureDefinition/CompassGeccoItem")
    if (extension != null) {
        hashMap[(extension.value as Coding).code] = responseItem.answerFirstRep
    }

    return hashMap;
}

fun mapByExtension(
    responseAnswer: QRAnswer,
    hashMap: HashMap<String, QRAnswer> = HashMap()
): HashMap<String, QRAnswer> {
    responseAnswer.item.forEach { itemComponent -> itemComponent?.answer?.forEach { mapByExtension(it, hashMap) } }
//    responseAnswer.extension?.forEach { extension -> hashMap[(extension.value as Coding).code] = responseAnswer }
    val extension = responseAnswer.getExtensionByUrl("https://num-compass.science/fhir/StructureDefinition/CompassGeccoItem")
    if (extension != null) {
        hashMap[(extension.value as Coding).code] = responseAnswer
    }
    return hashMap;
}