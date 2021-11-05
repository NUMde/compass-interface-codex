import org.hl7.fhir.r4.model.*


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