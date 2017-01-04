package instep.typeconvert

class ConverterNotExistsException(override val from: Class<*>, override val to: Class<*>) : TypeConvertException(from, to) {
}