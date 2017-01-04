package instep.typeconvert

open class TypeConvertException(open val from: Class<*>, open val to: Class<*>) : Exception() {
    override val message: String?
        get() = "TypeConvert fail from ${from.name} to ${to.name}"
}
