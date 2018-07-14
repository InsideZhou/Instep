package instep.typeconversion

/**
 * Convert instance to another type。
 */
interface Converter<From, To> {
    /**
     * Get type that convert from.
     */
    val from: Class<From>

    /**
     * Get type that convert to.
     */
    val to: Class<To>

    /**
     * Convert instance from FromClass to ToClass.
     */
    fun <T : From> convert(instance: T): To
}
