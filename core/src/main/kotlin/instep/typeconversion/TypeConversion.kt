package instep.typeconversion

/**
 * Convert instance to another type。
 */
interface TypeConversion {
    /**
     * Register type converter.
     */
    fun <From, To> register(converter: Converter<From, To>)

    /**
     * Get converter.
     */
    fun <From, To> getConverter(from: Class<From>, to: Class<To>): Converter<From, To>?

    /**
     * Remove type converter.
     */
    fun <From, To> remove(from: Class<From>, to: Class<To>): Converter<From, To>?

    /**
     * Remove type converter, all possible conversion implement by derived From to derived To would also be removed.
     */
    fun <From, To> removeAll(from: Class<From>, to: Class<To>)

    /**
     * Check if TypeConversion can convert FromClass to ToClass.
     */
    fun <From, To> canConvert(from: Class<From>, to: Class<To>): Boolean

    /**
     * Convert instance to ToClass.

     * @throws TypeConversionException If cannot convert to ToClass.
     */
    @Throws(TypeConversionException::class)
    fun <From : Any, To> convert(instance: From, to: Class<To>): To

    /**
     * Convert instance to ToClass.

     * @throws TypeConversionException If cannot convert to ToClass.
     */
    @Throws(TypeConversionException::class)
    fun <From : Any, To, T : From> convert(instance: T, from: Class<From>, to: Class<To>): To
}
