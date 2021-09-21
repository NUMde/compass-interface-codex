import org.hl7.fhir.r4.model.*

interface GeccoBundleBuilder {
    fun add(resource: Resource)
    val bundle: Bundle
}

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
                fullUrl = (if(resource.hasId()) resource.idElement else IdType.newRandomUuid()).toString()
            }
        })
    }
}


class ValidationServerBundleBuilder(author: Organization, app: Device, questionnaire: Questionnaire): GeccoBundleBuilder {
    override val bundle: Bundle = Bundle()

    init {
        bundle.type = Bundle.BundleType.DOCUMENT
        bundle.timestampElement = InstantType.now()
        bundle.identifier = Identifier().apply {
            system = "https://github.com/NUMde/"
            value = "dummy-identifier"
        }
        val (authorId, appId) = IdType.newRandomUuid() to IdType.newRandomUuid()
        val questionnaireId = IdType.newRandomUuid()
        bundle.addEntry(Bundle.BundleEntryComponent().apply {
            fullUrl = IdType.newRandomUuid().toString()
            resource = SummaryNote(DateTimeType.now(), Reference(authorId), Reference(appId), Reference(questionnaireId))
        })
        bundle.addEntry(Bundle.BundleEntryComponent().apply {
            fullUrl = authorId.toString()
            resource = author.apply { setId(authorId) }
        })
        bundle.addEntry(Bundle.BundleEntryComponent().apply {
            fullUrl = appId.toString()
            resource = app.apply { setId(appId); owner = Reference(authorId) }
        })
        bundle.addEntry(Bundle.BundleEntryComponent().apply {
            fullUrl = questionnaireId.toString()
            resource = questionnaire.apply { setId(questionnaireId) }
        })
    }

    private val listOfGeccoResources: MutableList<Reference> = (bundle.entry.first().resource as Composition).section.last().entry

    override fun add(resource: Resource) {
        val id = if(resource.hasId()) resource.idElement else IdType.newRandomUuid()
        bundle.addEntry(Bundle.BundleEntryComponent().apply {
            this.resource = resource
            fullUrl = id.toString()
        })
        listOfGeccoResources += Reference(id)
    }
}




fun SummaryNote(date: DateTimeType, author: Reference, app: Reference, questionnaire: Reference) = Composition().apply {
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




fun App() = Device().apply {
    deviceName = listOf(Device.DeviceDeviceNameComponent().apply {
         name = "Example Covid-19 App"
         type = Device.DeviceNameType.MANUFACTURERNAME
    })
    type = CodeableConcept(snomed("706689003", "Application program software"))
    version = listOf(Device.DeviceVersionComponent(StringType("1.0.0")))
}

fun Author() = Organization().apply {
    name = "Klinik für Infektiologie - Universitätsklinikum Beispielstadt"
    telecom = listOf(ContactPoint().apply {
        system = ContactPoint.ContactPointSystem.EMAIL
        value = "uk-beispiel@example.com"
    })
    address = listOf(
        Address().apply {
            line = listOf(StringType("Spitalgasse 123"))
            city = "Beispielstadt"
            postalCode = "12345"
            country = "Germany"
        }
    )
}