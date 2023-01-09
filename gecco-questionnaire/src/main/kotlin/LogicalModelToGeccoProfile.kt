import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.util.BundleBuilder
import org.hl7.fhir.instance.model.api.IBaseResource
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

fun BundleBuilder.add(resource: IBaseResource) {
//    this.addCreateEntry(resource)
    this.addCollectionEntry(resource)
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
        val ageInYears = logicalModel.demographics.ageInYears ?: logicalModel.demographics.ageInMonth?.floorDiv(12) ?:
            logicalModel.demographics.dateOfBirth?.let{ JodaPeriod.between(it, now).years }

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
                if (logicalModel.anamnesis.chronicLungDiseases.asthma != null) {
                    add(
                        AnaChronicLungDisease(
                            patientRef, ChronicLungDisease.ASTHMA,
                            logicalModel.anamnesis.chronicLungDiseases.asthma!!,
                            recordedDate
                        )
                    )
                }
                if (logicalModel.anamnesis.chronicLungDiseases.copd != null) {
                    add(
                        AnaChronicLungDisease(
                            patientRef, ChronicLungDisease.COPD,
                            logicalModel.anamnesis.chronicLungDiseases.copd!!,
                            recordedDate
                        )
                    )
                }
                if (logicalModel.anamnesis.chronicLungDiseases.cysticFibrosis != null) {
                    add(
                        AnaChronicLungDisease(
                            patientRef, ChronicLungDisease.CYSTIC_FIBROSIS,
                            logicalModel.anamnesis.chronicLungDiseases.cysticFibrosis!!,
                            recordedDate
                        )
                    )
                }
                if (logicalModel.anamnesis.chronicLungDiseases.fibrosis != null) {
                    add(
                        AnaChronicLungDisease(
                            patientRef, ChronicLungDisease.FIBROSIS,
                            logicalModel.anamnesis.chronicLungDiseases.fibrosis!!,
                            recordedDate
                        )
                    )
                }
                if (logicalModel.anamnesis.chronicLungDiseases.ohs != null) {
                    add(
                        AnaChronicLungDisease(
                            patientRef, ChronicLungDisease.OHS,
                            logicalModel.anamnesis.chronicLungDiseases.ohs!!,
                            recordedDate
                        )
                    )
                }
                if (logicalModel.anamnesis.chronicLungDiseases.osas != null) {
                    add(
                        AnaChronicLungDisease(
                            patientRef, ChronicLungDisease.OSAS,
                            logicalModel.anamnesis.chronicLungDiseases.osas!!,
                            recordedDate
                        )
                    )
                }
                if (logicalModel.anamnesis.chronicLungDiseases.pulmonaryHypertension != null) {
                    add(
                        AnaChronicLungDisease(
                            patientRef, ChronicLungDisease.PULMONARY_HYPERTENSION,
                            logicalModel.anamnesis.chronicLungDiseases.pulmonaryHypertension!!,
                            recordedDate
                        )
                    )
                }
                if (logicalModel.anamnesis.chronicLungDiseases.sleepApnea != null) {
                    add(
                        AnaChronicLungDisease(
                            patientRef, ChronicLungDisease.SLEEP_APNEA,
                            logicalModel.anamnesis.chronicLungDiseases.sleepApnea!!,
                            recordedDate
                        )
                    )
                }
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
                if (logicalModel.anamnesis.cardiovascularDiseases.arterialHyptertension != null) {
                    add(
                        AnaCardiovascular(
                            patientRef, CardiovascularDiseases.HYPERTENSIVE_DISORDER_SYSTEMIC_ARTERIAL,
                            logicalModel.anamnesis.cardiovascularDiseases.cardiacArrhytmia!!,
                            recordedDate
                        )
                    )
                }

                if (logicalModel.anamnesis.cardiovascularDiseases.cardiacArrhytmia != null) {
                    add(
                        AnaCardiovascular(
                            patientRef, CardiovascularDiseases.CARDIAC_ARRHYTHMIA,
                            logicalModel.anamnesis.cardiovascularDiseases.cardiacArrhytmia!!,
                            recordedDate
                        )
                    )
                }
                if (logicalModel.anamnesis.cardiovascularDiseases.carotidArteryStenosis != null) {
                    add(
                        AnaCardiovascular(
                            patientRef, CardiovascularDiseases.CAROTID_ARTERY_STENOSIS,
                            logicalModel.anamnesis.cardiovascularDiseases.carotidArteryStenosis!!,
                            recordedDate
                        )
                    )
                }
                if (logicalModel.anamnesis.cardiovascularDiseases.coronaryArteriosclerosis != null) {
                    add(
                        AnaCardiovascular(
                            patientRef, CardiovascularDiseases.CORONARY_ARTERIOSCLEROSIS,
                            logicalModel.anamnesis.cardiovascularDiseases.coronaryArteriosclerosis!!,
                            recordedDate
                        )
                    )
                }
                if (logicalModel.anamnesis.cardiovascularDiseases.heartFailure != null) {
                    add(
                        AnaCardiovascular(
                            patientRef, CardiovascularDiseases.HEART_FAILURE,
                            logicalModel.anamnesis.cardiovascularDiseases.heartFailure!!,
                            recordedDate
                        )
                    )
                }
                if (logicalModel.anamnesis.cardiovascularDiseases.peripherialArterialOcclusiveDisease != null) {
                    add(
                        AnaCardiovascular(
                            patientRef, CardiovascularDiseases.PERIPHERAL_ARTERIAL_OCCLUSIVE_DISEASE,
                            logicalModel.anamnesis.cardiovascularDiseases.peripherialArterialOcclusiveDisease!!,
                            recordedDate
                        )
                    )
                }
                if (logicalModel.anamnesis.cardiovascularDiseases.stateAfterHeartAttack != null) {
                    add(
                        AnaCardiovascular(
                            patientRef, CardiovascularDiseases.ZUSTAND_NACH_HERZINFARKT,
                            logicalModel.anamnesis.cardiovascularDiseases.stateAfterHeartAttack!!,
                            recordedDate
                        )
                    )
                }
                if (logicalModel.anamnesis.cardiovascularDiseases.stateAfterRevascularization != null) {
                    add(
                        AnaCardiovascular(
                            patientRef, CardiovascularDiseases.ZUSTAND_NACH_REVASKULARISATION,
                            logicalModel.anamnesis.cardiovascularDiseases.stateAfterRevascularization!!,
                            recordedDate
                        )
                    )
                }
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
                if (logicalModel.anamnesis.chronicLiverDiseases.steatosisOfLiver != null) {
                    add(
                        AnaChronicLiver(
                            patientRef, ChronicLiverDiseases.STEATOSIS_OF_LIVER,
                            logicalModel.anamnesis.chronicLiverDiseases.steatosisOfLiver!!,
                            recordedDate
                        )
                    )
                }
                if (logicalModel.anamnesis.chronicLiverDiseases.cirrhosisOfLiver != null) {
                    add(
                        AnaChronicLiver(
                            patientRef, ChronicLiverDiseases.CIRRHOSIS_OF_LIVER,
                            logicalModel.anamnesis.chronicLiverDiseases.cirrhosisOfLiver!!,
                            recordedDate
                        )
                    )
                }
                if (logicalModel.anamnesis.chronicLiverDiseases.chronicViralHepatitis != null) {
                    add(
                        AnaChronicLiver(
                            patientRef, ChronicLiverDiseases.CHRONIC_VIRAL_HEPATITIS,
                            logicalModel.anamnesis.chronicLiverDiseases.chronicViralHepatitis!!,
                            recordedDate
                        )
                    )
                }
                if (logicalModel.anamnesis.chronicLiverDiseases.autoimmuneLiverDisease != null) {
                    add(
                        AnaChronicLiver(
                            patientRef, ChronicLiverDiseases.AUTOIMMUNE_LIVER_DISEASE,
                            logicalModel.anamnesis.chronicLiverDiseases.autoimmuneLiverDisease!!,
                            recordedDate
                        )
                    )
                }
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
                if (logicalModel.anamnesis.rheumatologicalImmunologicalDiseases.vasculitis != null) {
                    add(
                        AnaRheumaticImmunological(
                            patientRef, RheumatologicalImmunologicalDiseases.VASCULITIS,
                            logicalModel.anamnesis.rheumatologicalImmunologicalDiseases.vasculitis!!,
                            recordedDate
                        )
                    )
                }
                if (logicalModel.anamnesis.rheumatologicalImmunologicalDiseases.rheumatoidArthritis != null) {
                    add(
                        AnaRheumaticImmunological(
                            patientRef, RheumatologicalImmunologicalDiseases.RHEUMATOID_ARTHRITIS,
                            logicalModel.anamnesis.rheumatologicalImmunologicalDiseases.rheumatoidArthritis!!,
                            recordedDate
                        )
                    )
                }
                if (logicalModel.anamnesis.rheumatologicalImmunologicalDiseases.inflammatoryBowelDisease != null) {
                    add(
                        AnaRheumaticImmunological(
                            patientRef, RheumatologicalImmunologicalDiseases.INFLAMMATORY_BOWEL_DISEASE,
                            logicalModel.anamnesis.rheumatologicalImmunologicalDiseases.inflammatoryBowelDisease!!,
                            recordedDate
                        )
                    )
                }
                if (logicalModel.anamnesis.rheumatologicalImmunologicalDiseases.collagenosis != null) {
                    add(
                        AnaRheumaticImmunological(
                            patientRef, RheumatologicalImmunologicalDiseases.COLLAGENOSIS,
                            logicalModel.anamnesis.rheumatologicalImmunologicalDiseases.collagenosis!!,
                            recordedDate
                        )
                    )
                }
                if (logicalModel.anamnesis.rheumatologicalImmunologicalDiseases.congenitalImmunodeficiencyDisease != null) {
                    add(
                        AnaRheumaticImmunological(
                            patientRef, RheumatologicalImmunologicalDiseases.CONGENITAL_IMMUNODEFICIENCY_DISEASE,
                            logicalModel.anamnesis.rheumatologicalImmunologicalDiseases.congenitalImmunodeficiencyDisease!!,
                            recordedDate
                        )
                    )
                }
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
                if (logicalModel.anamnesis.historyOfBeingATissueOrOrganRecipient.entireHeart != null) {
                    add(
                        AnaTransplant(
                            patientRef,
                            logicalModel.anamnesis.historyOfBeingATissueOrOrganRecipient.entireHeart!!,
                            recordedDate,
                            OrgansForTransplant.ENTIRE_HEART,
                        )
                    )
                }
                if (logicalModel.anamnesis.historyOfBeingATissueOrOrganRecipient.entireLung != null) {
                    add(
                        AnaTransplant(
                            patientRef,
                            logicalModel.anamnesis.historyOfBeingATissueOrOrganRecipient.entireLung!!,
                            recordedDate,
                            OrgansForTransplant.ENTIRE_LUNG
                        )
                    )
                }
                if (logicalModel.anamnesis.historyOfBeingATissueOrOrganRecipient.entireLiver != null) {
                    add(
                        AnaTransplant(
                            patientRef,
                            logicalModel.anamnesis.historyOfBeingATissueOrOrganRecipient.entireLiver!!,
                            recordedDate,
                            OrgansForTransplant.ENTIRE_LIVER
                        )
                    )
                }
                if (logicalModel.anamnesis.historyOfBeingATissueOrOrganRecipient.entireKidney != null) {
                    add(
                        AnaTransplant(
                            patientRef,
                            logicalModel.anamnesis.historyOfBeingATissueOrOrganRecipient.entireKidney!!,
                            recordedDate,
                            OrgansForTransplant.ENTIRE_KIDNEY
                        )
                    )
                }
                if (logicalModel.anamnesis.historyOfBeingATissueOrOrganRecipient.entirePancreas != null) {
                    add(
                        AnaTransplant(
                            patientRef,
                            logicalModel.anamnesis.historyOfBeingATissueOrOrganRecipient.entirePancreas!!,
                            recordedDate,
                            OrgansForTransplant.ENTIRE_PANCREAS
                        )
                    )
                }
              if(logicalModel.anamnesis.historyOfBeingATissueOrOrganRecipient.intestinalStructure != null){
                        add(AnaTransplant(patientRef,
                         logicalModel.anamnesis.historyOfBeingATissueOrOrganRecipient.intestinalStructure!!,
                         recordedDate,
                         OrgansForTransplant.INTESTINAL_STRUCTURE
                     ))
                 }
                if (logicalModel.anamnesis.historyOfBeingATissueOrOrganRecipient.entireSmallIntestine != null) {
                    add(
                        AnaTransplant(
                            patientRef,
                            logicalModel.anamnesis.historyOfBeingATissueOrOrganRecipient.entireSmallIntestine!!,
                            recordedDate,
                            OrgansForTransplant.ENTIRE_SMALL_INTESTINE
                        )
                    )
                }
                if (logicalModel.anamnesis.historyOfBeingATissueOrOrganRecipient.entireLargeIntestine != null) {
                    add(
                        AnaTransplant(
                            patientRef,
                            logicalModel.anamnesis.historyOfBeingATissueOrOrganRecipient.entireLargeIntestine!!,
                            recordedDate,
                            OrgansForTransplant.ENTIRE_LARGE_INTESTINE
                        )
                    )
                }
                if (logicalModel.anamnesis.historyOfBeingATissueOrOrganRecipient.skinPart != null) {
                    add(
                        AnaTransplant(
                            patientRef,
                            logicalModel.anamnesis.historyOfBeingATissueOrOrganRecipient.skinPart!!,
                            recordedDate,
                            OrgansForTransplant.SKIN_PART
                        )
                    )
                }
                if (logicalModel.anamnesis.historyOfBeingATissueOrOrganRecipient.entireCornea != null) {
                    add(
                        AnaTransplant(
                            patientRef,
                            logicalModel.anamnesis.historyOfBeingATissueOrOrganRecipient.entireCornea!!,
                            recordedDate,
                            OrgansForTransplant.ENTIRE_CORNEA
                        )
                    )
                }
                if (logicalModel.anamnesis.historyOfBeingATissueOrOrganRecipient.earOssicleStructure != null) {
                    add(
                        AnaTransplant(
                            patientRef,
                            logicalModel.anamnesis.historyOfBeingATissueOrOrganRecipient.earOssicleStructure!!,
                            recordedDate,
                            OrgansForTransplant.EAR_OSSICLE_STRUCTURE
                        )
                    )
                }
                if (logicalModel.anamnesis.historyOfBeingATissueOrOrganRecipient.entireHeartValve != null) {
                    add(
                        AnaTransplant(
                            patientRef,
                            logicalModel.anamnesis.historyOfBeingATissueOrOrganRecipient.entireHeartValve!!,
                            recordedDate,
                            OrgansForTransplant.ENTIRE_HEART_VALVE
                        )
                    )
                }
                if (logicalModel.anamnesis.historyOfBeingATissueOrOrganRecipient.bloodVesselPart != null) {
                    add(
                        AnaTransplant(
                            patientRef,
                            logicalModel.anamnesis.historyOfBeingATissueOrOrganRecipient.bloodVesselPart!!,
                            recordedDate,
                            OrgansForTransplant.BLOOD_VESSEL_PART
                        )
                    )
                }
                if (logicalModel.anamnesis.historyOfBeingATissueOrOrganRecipient.cerebralMeningitisStructure != null) {
                    add(
                        AnaTransplant(
                            patientRef,
                            logicalModel.anamnesis.historyOfBeingATissueOrOrganRecipient.cerebralMeningitisStructure!!,
                            recordedDate,
                            OrgansForTransplant.CEREBRAL_MENINGES_STRUCTURE
                        )
                    )
                }
                if (logicalModel.anamnesis.historyOfBeingATissueOrOrganRecipient.boneTissueOrStructure != null) {
                    add(
                        AnaTransplant(
                            patientRef,
                            logicalModel.anamnesis.historyOfBeingATissueOrOrganRecipient.boneTissueOrStructure!!,
                            recordedDate,
                            OrgansForTransplant.BONE_TISSUE_STRUCTURE
                        )
                    )
                }
                if (logicalModel.anamnesis.historyOfBeingATissueOrOrganRecipient.cartilageTissue != null) {
                    add(
                        AnaTransplant(
                            patientRef,
                            logicalModel.anamnesis.historyOfBeingATissueOrOrganRecipient.cartilageTissue!!,
                            recordedDate,
                            OrgansForTransplant.CARTILAGE_TISSUE
                        )
                    )
                }
                if (logicalModel.anamnesis.historyOfBeingATissueOrOrganRecipient.tendonStructure != null) {
                    add(
                        AnaTransplant(
                            patientRef,
                            logicalModel.anamnesis.historyOfBeingATissueOrOrganRecipient.tendonStructure!!,
                            recordedDate,
                            OrgansForTransplant.TENDON_STRUCTURE
                        )
                    )
                }
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
                if (logicalModel.anamnesis.chronicNeurologicalOrMentalDiseases.anxietyDisorder != null) {
                    add(
                        AnaChronicNeurologicalMental(
                            patientRef, ChronicNeurologicalMentalDisease.ANXIETY_DISORDER,
                            logicalModel.anamnesis.chronicNeurologicalOrMentalDiseases.anxietyDisorder!!,
                            recordedDate
                        )
                    )
                }
                if (logicalModel.anamnesis.chronicNeurologicalOrMentalDiseases.psychoticDisorder != null) {
                    add(
                        AnaChronicNeurologicalMental(
                            patientRef, ChronicNeurologicalMentalDisease.PSYCHOTIC_DISORDER,
                            logicalModel.anamnesis.chronicNeurologicalOrMentalDiseases.psychoticDisorder!!,
                            recordedDate
                        )
                    )
                }
                if (logicalModel.anamnesis.chronicNeurologicalOrMentalDiseases.parkinsonDisorder != null) {
                    add(
                        AnaChronicNeurologicalMental(
                            patientRef, ChronicNeurologicalMentalDisease.PARKINSONS_DISEASE,
                            logicalModel.anamnesis.chronicNeurologicalOrMentalDiseases.parkinsonDisorder!!,
                            recordedDate
                        )
                    )
                }
                if (logicalModel.anamnesis.chronicNeurologicalOrMentalDiseases.dementia != null) {
                    add(
                        AnaChronicNeurologicalMental(
                            patientRef, ChronicNeurologicalMentalDisease.DEMENTIA,
                            logicalModel.anamnesis.chronicNeurologicalOrMentalDiseases.dementia!!,
                            recordedDate
                        )
                    )
                }
                if (logicalModel.anamnesis.chronicNeurologicalOrMentalDiseases.multipleSclerosis != null) {
                    add(
                        AnaChronicNeurologicalMental(
                            patientRef, ChronicNeurologicalMentalDisease.MULTIPLE_SCLEROSIS,
                            logicalModel.anamnesis.chronicNeurologicalOrMentalDiseases.multipleSclerosis!!,
                            recordedDate
                        )
                    )
                }
                if (logicalModel.anamnesis.chronicNeurologicalOrMentalDiseases.combinedDisorderOfMuscleAndPeripheralNerve != null) {
                    add(
                        AnaChronicNeurologicalMental(
                            patientRef,
                            ChronicNeurologicalMentalDisease.COMBINED_DISORDER_OF_MUSCLE_AND_PERIPHERAL_NERVE,
                            logicalModel.anamnesis.chronicNeurologicalOrMentalDiseases.combinedDisorderOfMuscleAndPeripheralNerve!!,
                            recordedDate
                        )
                    )
                }
                if (logicalModel.anamnesis.chronicNeurologicalOrMentalDiseases.epilepsy != null) {
                    add(
                        AnaChronicNeurologicalMental(
                            patientRef, ChronicNeurologicalMentalDisease.EPILEPSY,
                            logicalModel.anamnesis.chronicNeurologicalOrMentalDiseases.epilepsy!!,
                            recordedDate
                        )
                    )
                }

                if (logicalModel.anamnesis.chronicNeurologicalOrMentalDiseases.migraine != null) {
                    add(
                        AnaChronicNeurologicalMental(
                            patientRef, ChronicNeurologicalMentalDisease.MIGRAINE,
                            logicalModel.anamnesis.chronicNeurologicalOrMentalDiseases.migraine!!,
                            recordedDate
                        )
                    )
                }

                if (logicalModel.anamnesis.chronicNeurologicalOrMentalDiseases.historyOfCerebrovascularAccidentWithResidualDeficit != null) {
                    add(
                        AnaChronicNeurologicalMental(
                            patientRef,
                            ChronicNeurologicalMentalDisease.HISTORY_OF_CEREBROVASCULAR_ACCIDENT_WITH_RESIDUAL_DEFICIT,
                            logicalModel.anamnesis.chronicNeurologicalOrMentalDiseases.historyOfCerebrovascularAccidentWithResidualDeficit!!,
                            recordedDate
                        )
                    )
                }
                if (logicalModel.anamnesis.chronicNeurologicalOrMentalDiseases.historyOfCerebrovascularAccidentWithoutResidualDeficits != null) {
                    add(
                        AnaChronicNeurologicalMental(
                            patientRef,
                            ChronicNeurologicalMentalDisease.HISTORY_OF_CEREBROVASCULAR_ACCIDENT_WITHOUT_RESIDUAL_DEFICITS,
                            logicalModel.anamnesis.chronicNeurologicalOrMentalDiseases.historyOfCerebrovascularAccidentWithoutResidualDeficits!!,
                            recordedDate
                        )
                    )
                }
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

private fun GeccoBundleBuilder.addCovid19Vaccine(logicalModel: Covid19Vaccination?, patientRef: Reference) {
    if (logicalModel != null && logicalModel.status == YesNoUnknown.YES) {
        val vaccineDate = logicalModel.date?.let { DateTimeType(it.toUtilDate()) } ?: unknownDateTime()
        if (logicalModel.vaccine != null) {
            add(VaccinationCovid19(patientRef,  logicalModel.vaccine as Covid19Vaccine, vaccineDate))
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


