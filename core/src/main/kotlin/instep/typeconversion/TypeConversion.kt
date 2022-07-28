package instep.typeconversion

/**
 * Convert instance to another type。
 */
interface TypeConversion {
    /**
     * Register type converter.
     */
    fun <From, To> register(converter: Converter<From, To>, tag: String = "")

    /**
     * Unregister type converter.
     */
    fun <From, To> unregister(converter: Converter<From, To>, tag: String = "")

    /**
     * Get converter.
     */
    fun <From, To> getConverter(from: Class<From>, to: Class<To>, tag: String = ""): Converter<From, To>?

    /**
     * Remove type converter, all possible conversion implement by derived From to derived To would also be removed.
     */
    fun <From, To> removeAll(from: Class<From>, to: Class<To>)
}
