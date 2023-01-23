interface ClassToItemRenderer<T> {
    fun render(compassId: String, item: QItem)
    fun parse(geccoId: String, item: Map<String, QRAnswer>): T
}

fun ClassToItemRenderer<*>.getType() =
    this::class.supertypes.find { it.classifier == ClassToItemRenderer::class }!!.arguments.first().type

