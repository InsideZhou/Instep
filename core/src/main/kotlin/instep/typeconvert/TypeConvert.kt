package instep.typeconvert

import java.io.Serializable

/**
 * Convert instance to another type。
 */
interface TypeConvert : Serializable {
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
     * Check if TypeConvert can convert FromClass to ToClass.
     */
    fun <From, To> canConvert(from: Class<From>, to: Class<To>): Boolean

    /**
     * Convert instance to ToClass.

     * @throws TypeConvertException If cannot convert to ToClass.
     */
    @Throws(TypeConvertException::class)
    fun <From : Any, To> convert(instance: From, to: Class<To>): To

    /**
     * Convert instance to ToClass.

     * @throws TypeConvertException If cannot convert to ToClass.
     */
    @Throws(TypeConvertException::class)
    fun <From : Any, To, T : From> convert(instance: T, from: Class<From>, to: Class<To>): To
}
