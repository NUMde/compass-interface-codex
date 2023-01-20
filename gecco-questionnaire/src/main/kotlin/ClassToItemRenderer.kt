import org.hl7.fhir.r4.model.Questionnaire

interface ClassToItemRenderer<T> {
    fun render(compassId: String, item: QItem)

    fun parse(item: QRItem): T

    fun isInstanceOf(item: QRItem): Boolean
}

class YesNoUnknownRenderer : ClassToItemRenderer<YesNoUnknown> {
    override fun render(compassId: String, item: QItem) {
        item.apply {
            type = Questionnaire.QuestionnaireItemType.CHOICE
            extractEnum(YesNoUnknown::class.java)
        }

    }

    override fun isInstanceOf(item: QRItem): Boolean {
        TODO("Not yet implemented")
    }

    override fun parse(item: QRItem): YesNoUnknown {
        return TODO()
    }


}

class YesNoUnknownWithSeverityRenderer : ClassToItemRenderer<YesNoUnknownWithSymptomSeverity> {
    override fun render(compassId: String, item: QItem) {
        item.apply {
            type = Questionnaire.QuestionnaireItemType.GROUP
            val presence = Questionnaire.QuestionnaireItemComponent().apply {
                linkId = item.linkId + ".1"
                text = "Vorhandensein?"
                type = Questionnaire.QuestionnaireItemType.CHOICE
                extractEnum(YesNoUnknown::class.java)
                addExtension(CompassGeccoItemExtension("$compassId.presence"))
            }
            this.addItem(presence)

            val severity = Questionnaire.QuestionnaireItemComponent().apply {
                linkId = item.linkId + ".2"
                text = "Schweregrad?"
                type = Questionnaire.QuestionnaireItemType.CHOICE
                extractEnum(SymptomSeverity::class.java)
                enableWhen = listOf(Questionnaire.QuestionnaireItemEnableWhenComponent().apply {
                    question = "${compassId.removePrefix(".")}.presence"
                    operator = Questionnaire.QuestionnaireItemOperator.EQUAL
                    answer = YesNoUnknown.YES.coding
                })
                addExtension(CompassGeccoItemExtension("$compassId.severity"))
                addExtension(DependentItemExtension())
            }
            item.addItem(severity)
        }

    }

    override fun isInstanceOf(item: QRItem): Boolean {
        TODO("Not yet implemented")
    }

    override fun parse(item: QRItem): YesNoUnknownWithSymptomSeverity {
        return TODO()
    }
}

class YesNoUnknownWithIntentRenderer : ClassToItemRenderer<YesNoUnknownWithIntent> {
    override fun render(compassId: String, item: QItem) {
        item.apply {
            type = Questionnaire.QuestionnaireItemType.GROUP
            this.item = listOf(Questionnaire.QuestionnaireItemComponent().apply {
                linkId = item.linkId + ".1"
                text = "Wurde das Medikament verabreicht?"
                type = Questionnaire.QuestionnaireItemType.CHOICE
                extractEnum(YesNoUnknown::class.java)
                addExtension(CompassGeccoItemExtension("$compassId.given"))
            }, Questionnaire.QuestionnaireItemComponent().apply {
                linkId = item.linkId + ".2"
                text = "Mit welcher therapheutischen Absicht das Medikament verarbreicht?"
                type = Questionnaire.QuestionnaireItemType.CHOICE
                extractEnum(TherapeuticIntent::class.java)
                enableWhen = listOf(Questionnaire.QuestionnaireItemEnableWhenComponent().apply {
                    question = "${compassId.removePrefix(".")}.given"
                    operator = Questionnaire.QuestionnaireItemOperator.EQUAL
                    answer = YesNoUnknown.YES.coding
                })
                addExtension(CompassGeccoItemExtension("$compassId.intent"))
                addExtension(DependentItemExtension())
            }
            )
        }
    }

    override fun isInstanceOf(item: QRItem): Boolean {
        TODO("Not yet implemented")
    }

    override fun parse(item: QRItem): YesNoUnknownWithIntent {
        TODO("Not yet implemented")
    }
}

class YesNoUnknownWithDateRenderer : ClassToItemRenderer<YesNoUnknownWithIntent> {
    override fun render(compassId: String, item: QItem) {
        item.apply {
            type = Questionnaire.QuestionnaireItemType.GROUP
            this.item = listOf(Questionnaire.QuestionnaireItemComponent().apply {
                linkId = item.linkId + ".1"
                extractEnum(YesNoUnknown::class.java)
                addExtension(CompassGeccoItemExtension("$compassId.status"))
            }, Questionnaire.QuestionnaireItemComponent().apply {
                linkId = item.linkId + ".2"
                text = "Datum"
                type = Questionnaire.QuestionnaireItemType.DATE
                enableWhen = listOf(Questionnaire.QuestionnaireItemEnableWhenComponent().apply {
                    question = "${compassId.removePrefix(".")}.status"
                    operator = Questionnaire.QuestionnaireItemOperator.EQUAL
                    answer = YesNoUnknown.YES.coding
                })
                addExtension(CompassGeccoItemExtension("$compassId.date"))
                addExtension(DependentItemExtension())
            })

        }
    }

    override fun isInstanceOf(item: QRItem): Boolean {
        TODO("Not yet implemented")
    }

    override fun parse(item: QRItem): YesNoUnknownWithIntent {
        TODO("Not yet implemented")
    }
}