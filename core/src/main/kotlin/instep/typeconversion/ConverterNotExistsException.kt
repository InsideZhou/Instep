package instep.typeconversion

class ConverterNotExistsException(override val from: Class<*>, override val to: Class<*>) : TypeConversionException(from, to) {
}