import YesNoUnknown.*
import org.hl7.fhir.r4.model.*
import org.hl7.fhir.r4.model.codesystems.DataAbsentReason
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*


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
		extension.add(Extension("dateTimeOfDocumentation", dateTimeOfDocumentation))
		extension.add(Extension("age", AgeInYears(ageInYears)))
	}

	return Patient().apply {
		meta.addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/Patient")
		setId(patientId)
		identifier = listOf()
		if (ageInYears != null) {
			addExtension(AgeExtension(ageInYears, dateTimeOfDocumentation))
		}
		if (ethnicGroup != null) {
			addExtension(EthnicGroupExtension(ethnicGroup))
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
			modifierExtension = listOf(uncertaintyOfPresenceExt())
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
		meta.addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/chronic-lung-diseases") //TODO: FIXME
		category = listOf(CodeableConcept(snomed("418112009", "Pulmonary medicine")))
	}

fun AnaCardiovascular(
	patientRef: Reference,
	cardiovascularDisease: CardiovascularDiseases,
	yesNoUnknown: YesNoUnknown,
	recordedDate: DateTimeType,
) =
	Anamnese(patientRef, cardiovascularDisease.codeableConcept, yesNoUnknown, recordedDate).apply {
		meta.addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/cardiovascular-diseases") //TODO: FIXME
		category = listOf(CodeableConcept(snomed("722414000", "Vascular medicine")))
	}

fun AnaChronicLiver(
	patientRef: Reference,
	chronicLiverDisease: ChronicLiverDiseases,
	yesNoUnknown: YesNoUnknown,
	recordedDate: DateTimeType
) =
	Anamnese(patientRef, chronicLiverDisease.codeableConcept, yesNoUnknown, recordedDate).apply {
		meta.addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/chronic-liver-diseases")
		category = listOf(CodeableConcept(snomed("408472002", "Hepatology")))
	}

fun AnaRheumaticImmunological(
	patientRef: Reference,
	rheumatologicalImmunologicalDisease: RheumatologicalImmunologicalDiseases,
	yesNoUnknown: YesNoUnknown,
	recordedDate: DateTimeType
) = Anamnese(patientRef, rheumatologicalImmunologicalDisease.codeableConcept, yesNoUnknown, recordedDate).apply {
	meta.addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/rheumatological-immunological-diseases")
	category = listOf(
		CodeableConcept(
			snomed("394810000", "Rheumatology"),
			snomed("408480009", "Clinical immunology")
		)
	)
}

fun AnaChronicNeurologicalMental(
	patientRef: Reference,
	disease: ChronicNeurologicalMentalDisease,
	yesNoUnknown: YesNoUnknown,
	recordedDate: DateTimeType
) = Anamnese(patientRef, disease.codeableConcept, yesNoUnknown, recordedDate).apply {
	meta.addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/chronic-neurological-mental-diseases")
	category = listOf(
		CodeableConcept(
			snomed("394591006", "Neurology"),
			snomed("394587001", "Psychiatry")
		)
	)
}

fun AnaHIV(patientRef: Reference, yesNoUnknown: YesNoUnknown, recordedDate: DateTimeType) =
	//TODO: Theoretisch sind auch andere Codes zugelassen
	Anamnese(
		patientRef,
		CodeableConcept(snomed("86406008", "Human immunodeficiency virus infection")),
		yesNoUnknown,
		recordedDate,
	).apply {
		meta.addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/human-immunodeficiency-virus-infection")
		category = listOf(CodeableConcept(snomed("394807007", "Infectious diseases (specialty)")))
	}

fun AnaSmoking(patientRef: Reference, smokingStatus: SmokingStatus, recordedDate: DateTimeType) = Observation().apply {
	meta.addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/smoking-status")
	status = Observation.ObservationStatus.FINAL
	category = listOf(ObservationCategory.SOCIAL_HISTORY.codeableConcept)
	code = CodeableConcept(loinc("72166-2", "Tobacco smoking status"))
	subject = patientRef
	value = smokingStatus.codeableConcept
	effective = recordedDate
}

fun ObservationLab(patientRef: Reference, coding: Coding, quantity: Quantity, recordedDate: DateTimeType) = Observation().apply {
	meta.addProfile("https://www.medizininformatik-initiative.de/fhir/core/modul-labor/StructureDefinition/ObservationLab")
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
		meta.addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/gastrointestinal-ulcers")
		category = listOf(CodeableConcept(snomed("394584008", "Gastroenterology")))
	}


fun AnaRespiratoryTherapy(patientRef: Reference, yesNoUnknown: YesNoUnknown, performedDt: DateTimeType) =
	Procedure().apply {
		meta.addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/respiratory-therapies")
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
	meta.addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/chronic-kidney-diseases")
	category = listOf(CodeableConcept(snomed("394589003", "Nephrology (qualifier value)")))
	subject = patientRef

	code = if (chronicKidneyDisease !in listOf(ChronicKidneyDisease.ABSENT, ChronicKidneyDisease.UNKNOWN)) {
		chronicKidneyDisease.codeableConcept
	} else {
		ChronicKidneyDisease.CHRONIC_KIDNEY_DISEASE.codeableConcept
	}
	when (chronicKidneyDisease) {
		ChronicKidneyDisease.UNKNOWN -> extension = listOf(uncertaintyOfPresenceExt())
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
		meta.addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/organ-recipient")
		category = listOf(CodeableConcept(snomed("788415003", "Transplant medicine")))
		//TODO: Wie ist das gemeint? Alle transplantierten Organe in eine Ressource oder für jedes Organ eine eigene?
		if (organ != null) {
			bodySite = listOf(CodeableConcept(organ.snomed))
		}
	}

fun AnaDiabetes(patientRef: Reference, diabetes: Diabetes, yesNoUnknown: YesNoUnknown, recordedDate: DateTimeType) =
	Anamnese(patientRef, diabetes.codeableConcept, yesNoUnknown, recordedDate).apply {
		meta.addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/diabetes-mellitus")
		category = listOf(CodeableConcept(snomed("408475000", "Diabetic medicine")))
	}

fun AnaCancer(patientRef: Reference, cancerStatus: CancerStatus, recordedDate: DateTimeType) = Condition().apply {
	meta.addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/malignant-neoplastic-disease")
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
			extension.add(uncertaintyOfPresenceExt())
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
	meta.addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/history-of-travel")
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
	dateTimeElement = unknownDateTime()
	category = listOf(
		CodeableConcept(
			Coding("http://terminology.hl7.org/CodeSystem/consentcategorycodes", "dnr", "Do Not Resuscitate")
		)
	)
	policy = listOf(Consent.ConsentPolicyComponent().apply {
		uri = "https://www.aerzteblatt.de/archiv/65440/DNR-Anordnungen-Das-fehlende-Bindeglied"
	})
	provision = Consent.provisionComponent().apply {
		code = listOf(CodeableConcept(resuscitation.coding))
	}

}

fun Vaccination(patientRef: Reference, disease: ImmunizationDisease, occurenceDt: DateTimeType) = Immunization().apply {
	meta.addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/immunization")
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
	meta.addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/immunization")
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
	meta.addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/immunization")
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
		meta.addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/sars-cov-2-rt-pcr")
		identifier = listOf(
			Identifier().apply {
				type = CodeableConcept(
					Coding("http://terminology.hl7.org/CodeSystem/v2-0203", "OBI", "Observation Instance Identifier")
				)
				system = "https://compass.science/fhir/NamingSystem/RT_PCR"
				value = "TODO" //TODO
				assigner = Reference().apply {
					identifier = Identifier().apply {
						system = "https://compass.science/fhir/NamingSystem/Assigner"
						value = "unknown-assigner"
					}
				}
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
		effective = unknownDateTime() //TODO
		value = detectedInconclusive.codeableConcept
	}


fun FrailtyScore(patientRef: Reference, frailityScore: FrailityScore) = Observation().apply {
	meta.addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/frailty-score")
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
	meta.addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/body-weight")
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
	meta.addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/body-height")
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
		meta.addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/pregnancy-status")
		status = Observation.ObservationStatus.FINAL
		code = CodeableConcept(loinc("82810-3", "Pregnancy status"))
		subject = patientRef
		effective = effectiveDt
		value = pregnancyStatus.codeableConcept

	}

fun KnownExposure(patientRef: Reference, yesNoUnknown: YesNoUnknown) = Observation().apply {
	meta.addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/known-exposure")
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
	meta.addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/complications-covid-19")
	code = complicationsCovid19.codeableConcept
	recordedDateElement = recordedDate
	category = listOf(CodeableConcept(snomed("116223007", "Complication (disorder)")))
	subject = patientRef
	when (yesNoUnknown) {
		NO -> verificationStatus = ConditionVerificationStatus.REFUTED.codeableConcept
		YES -> verificationStatus = ConditionVerificationStatus.VERIFIED.codeableConcept
		UNKNOWN -> modifierExtension = listOf(uncertaintyOfPresenceExt())
	}
}

fun DiagnosisCovid19(patientRef: Reference, stageAtDiagnosis: StageAtDiagnosis, recordedDate: DateTimeType) =
	Condition().apply {
		meta.addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/diagnosis-covid-19")
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
	meta.addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/symptoms-covid-19")
	category = listOf(CodeableConcept(loinc("75325-1", "Symptom")))
	code = symptom.codeableConcept
	recordedDateElement = recordedDate
	//onset =
	subject = patientRef
	this.severity = severity?.let { CodeableConcept(it.coding) }
	when (response) {
		NO -> verificationStatus = ConditionVerificationStatus.REFUTED.codeableConcept
		YES -> verificationStatus = ConditionVerificationStatus.VERIFIED.codeableConcept
		UNKNOWN -> modifierExtension = listOf(uncertaintyOfPresenceExt())
	}
}



fun RespiratoryOutcome(patientRef: Reference, recordedDate: DateTimeType, response: ConditionVerificationStatus) =
	Condition().apply {
		meta.addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/dependence-on-ventilator")
		category = listOf(CodeableConcept(snomed("404989005", "Ventilation status (observable entity)")))
		code = CodeableConcept(snomed("444932008", "Dependence on ventilator (finding)"))
		recordedDateElement = recordedDate
		subject = patientRef
		verificationStatus = response.codeableConcept
	}

fun TypeOfDischarge(patientRef: Reference, typeOfDischarge: TypeOfDischarge) = Observation().apply {
	meta.addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/discharge-disposition")
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
	meta.addProfile(therapy.profile)
	category = therapy.category
	code = therapy.codeableConcept
	subject = patientRef

	performedDateTimeType.addExtension("http://hl7.org/fhir/StructureDefinition/data-absent-reason",
		when (yesNoUnknown) {
			YES, UNKNOWN -> DataAbsentReason.UNKNOWN
			NO -> DataAbsentReason.NOTPERFORMED
		}.let { CodeType(it.toCode()) }
	)
	status = when (yesNoUnknown) {
		YES -> Procedure.ProcedureStatus.INPROGRESS
		NO -> Procedure.ProcedureStatus.NOTDONE
		UNKNOWN -> Procedure.ProcedureStatus.UNKNOWN
	}

}

fun isPatientInIntensiveCareUnit(patientRef: Reference, yesNoUnknown: YesNoUnknown, start: Date, end: Date) =
	Observation().apply {
		meta.addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/patient-in-icu")
		category = listOf(ObservationCategory.SOCIAL_HISTORY.codeableConcept)
		status = Observation.ObservationStatus.FINAL
		subject = patientRef
		code = CodeableConcept(
			Coding(
				"https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/ecrf-parameter-codes",
				"01",
				"Is the patient in the intensive care unit?"
			)
		)
		//TODO: Nachfragen, ob es nicht mehr Sinn macht, diese Angabe nur bei ICU-Aufenthalt zu setzen?
		this.effective = Period().apply {
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
		meta.addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/respiratory-therapies")
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
		meta.addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/pharmacological-therapy")
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
	meta.addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/pharmacological-therapy")
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
		meta.addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/pharmacological-therapy")
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
		meta.addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/pharmacological-therapy")
		status = MedicationStatement.MedicationStatementStatus.ACTIVE
		medication = CodeableConcept(
			snomed("81839001", "Medicinal product acting as anticoagulant agent (product)"),
			medi.code
		)
		subject = patientRef
		effective = effectiveDt
		if (intent != null) {
			reasonCode = listOf(intent.codeableConcept)
		}
	}

fun StudyInclusionDueToCovid19(patientRef: Reference, yesNoUnknown: YesNoUnknownOtherNa) = Observation().apply {
	meta.addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/study-inclusion-covid-19")
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
	meta.addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/radiology-procedures")
	status = Procedure.ProcedureStatus.COMPLETED
	category = CodeableConcept(snomed("103693007", "Diagnostic procedure (procedure)"))
	code = imaging.codeableConcept
	subject = patientRef
	setPerformed(performed)
	bodySite = listOf(CodeableConcept(snomed("39607008", "Lung structure (body structure)")))
}

fun ImagingFinding(patientRef: Reference, finding: RadiologicFindings) = DiagnosticReport().apply {
	meta.addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/diagnostic-report-radiology")
	status = DiagnosticReport.DiagnosticReportStatus.FINAL
	category = listOf(
		CodeableConcept(
			loinc("18726-0", "Radiology studies (set)"),
			Coding("http://terminology.hl7.org/CodeSystem/v2-0074", "RAD", "Radiology")
		)
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
	meta.addProfile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/interventional-clinical-trial-participation")
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