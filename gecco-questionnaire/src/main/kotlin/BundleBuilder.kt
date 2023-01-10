import org.hl7.fhir.r4.model.*

/**
 * Helper class to create the different kind of result Bundles (Transaction and Document)
 */
sealed interface GeccoBundleBuilder {
    /**
     * Add resource to Bundle
     */
    fun add(resource: Resource)

    /**
     * Retrieve the Bundle created by the Builder
     */
    val bundle: Bundle
}

/**
 * Creates plain Bundle with Bundle.type = 'transaction' that simply POSTs all entries to the server.
 * To use with any FHIR server.
 */
class TransactionBundleBuilder : GeccoBundleBuilder {
    override val bundle: Bundle = Bundle().apply {
        type = Bundle.BundleType.TRANSACTION
    }

    override fun add(resource: Resource) {
        bundle.addEntry(Bundle.BundleEntryComponent().apply {
            this.resource = resource
            this.request = Bundle.BundleEntryRequestComponent().apply {
                method = Bundle.HTTPVerb.POST
                url = resource.resourceType.name
                fullUrl = (if (resource.hasId()) resource.idElement else IdType.newRandomUuid()).toString()
            }
        })
    }
}

/**
 * Creates a Bundle with Bundle.type = "document" and the Organization, App and Composition resources as required
 * by the validation server.
 * see https://github.com/NUMde/compass-num-conformance-checker/blob/main/docs/README.md#input-format-2 for more info
 */
class ValidationServerBundleBuilder(author: Organization, app: Device, originalQuestionnaire: Questionnaire) :
    GeccoBundleBuilder {
    override val bundle: Bundle = Bundle()
    private val composition: Composition

    init {
        bundle.type = Bundle.BundleType.DOCUMENT
        bundle.timestampElement = InstantType.now()
        bundle.identifier.apply {
            system = "https://github.com/NUMde/"
            value = "dummy-identifier"
        }
        val (authorId, appId, questionnaireId) = (1..3).map { IdType.newRandomUuid() }
        composition = SummaryNote(DateTimeType.now(), Reference(authorId), Reference(appId), Reference(questionnaireId))
        bundle.addEntry(Bundle.BundleEntryComponent().apply {
            fullUrl = IdType.newRandomUuid().toString()
            resource = composition
        })
        bundle.addEntry(Bundle.BundleEntryComponent().apply {
            fullUrl = authorId.toString()
            resource = author.apply { setId(authorId) }
        })
        bundle.addEntry(Bundle.BundleEntryComponent().apply {
            fullUrl = appId.toString()
            resource = app.apply {
                setId(appId)
                owner = Reference(authorId)
            }
        })
        bundle.addEntry(Bundle.BundleEntryComponent().apply {
            fullUrl = questionnaireId.toString()
            resource = originalQuestionnaire.apply { setId(questionnaireId) }
        })
    }

    /**
     * A reference of GECCO items must be added to the Composition resource for the validation server
     */
    private val listOfGeccoResources: MutableList<Reference> = composition.section.last().entry

    override fun add(resource: Resource) {
        val id = if (resource.hasId()) resource.idElement else IdType.newRandomUuid()
        bundle.addEntry(Bundle.BundleEntryComponent().apply {
            this.resource = resource
            fullUrl = id.toString()
        })
        listOfGeccoResources += Reference(id)
    }
}

private fun SummaryNote(date: DateTimeType, author: Reference, app: Reference, questionnaire: Reference) =
    Composition().apply {
        status = Composition.CompositionStatus.FINAL
        type = CodeableConcept(loinc("68608-9", "Summary Note"))
        dateElement = date
        this.author = listOf(author)
        title = "GECCO conformance test document"
        section = listOf(
            Composition.SectionComponent().apply {
                title = "Software that generated the test data"
                code = CodeableConcept(loinc("92040-5", "Information communication technology description"))
                entry = listOf(app)
            },
            Composition.SectionComponent().apply {
                title = "Organization publishing the software"
                code = CodeableConcept(loinc("91025-7", "Lead department or agency name Organization"))
                entry = listOf(author)
            },
            Composition.SectionComponent().apply {
                title = "Questionnaires used in the software"
                code = CodeableConcept(loinc("74468-0", "Questionnaire form definition Document"))
                entry = listOf(questionnaire)
            },
            Composition.SectionComponent().apply {
                title = "Test data resources"
                code = CodeableConcept(loinc("68839-0", "Research note"))
            }
        )

    }
