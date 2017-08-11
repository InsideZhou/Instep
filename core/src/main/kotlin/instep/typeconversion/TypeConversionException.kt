package instep.typeconversion

open class TypeConversionException(open val from: Class<*>, open val to: Class<*>) : Exception() {
    override val message: String?
        get() = "TypeConversion fail from ${from.name} to ${to.name}"
}
