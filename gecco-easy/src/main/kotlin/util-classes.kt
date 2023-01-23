import java.util.*

class YesNoUnknownWithSymptomSeverity(
    var yesNoUnknown: YesNoUnknown? = null,
    var severity: SymptomSeverity? = null
) {
    override fun toString(): String {
        return "YesNoUnknownWithSymptomSeverity(yesNoUnknown=$yesNoUnknown, severity=$severity)"
    }
}

class YesNoUnknownWithDate(
    var yesNoUnknown: YesNoUnknown? = null,
    var date: Date? = null
) {
    override fun toString(): String {
        return "YesNoUnknownWithDate(yesNoUnknown=$yesNoUnknown, date=$date)"
    }
}