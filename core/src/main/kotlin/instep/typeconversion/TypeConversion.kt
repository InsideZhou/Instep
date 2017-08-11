package instep.typeconversion

import java.io.Serializable

/**
 * Convert instance to another type。
 */
interface TypeConversion : Serializable {
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
    @Throws(ConverterNotExistsException::class)
    fun <From, To> remove(from: Class<From>, to: Class<To>): Converter<From, To>?

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
