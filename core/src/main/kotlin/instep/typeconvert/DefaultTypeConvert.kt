package instep.typeconvert

import instep.Instep
import instep.cache.Cache

open class DefaultTypeConvert : TypeConvert {
    protected val cache = Instep.make(Cache::class.java)

    override fun <From, To> canConvert(from: Class<From>, to: Class<To>): Boolean {
        val result = cache.containsKey(getKey(from, to))

        if (!result) {
            val superOfFrom = from.superclass
            if (null != superOfFrom) {
                return canConvert(superOfFrom, to)
            }
        }

        return result
    }

    override fun <From : Any, To> convert(instance: From, to: Class<To>): To {
        return convert(instance, instance.javaClass, to)
    }

    override fun <From : Any, To, T : From> convert(instance: T, from: Class<From>, to: Class<To>): To {
        val converter = getConverter(from, to)
        return converter.convert(instance)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <From, To> getConverter(from: Class<From>, to: Class<To>): Converter<From, To> {
        val key = getKey(from, to)
        if (!cache.containsKey(key)) {
            val superOfFrom = from.superclass
            if (null != superOfFrom) {
                return getConverter(superOfFrom, to) as Converter<From, To>
            }

            throw ConverterNotExistsException(from, to)
        }

        return cache.get(key) as Converter<From, To>
    }

    override fun <From, To> register(converter: Converter<From, To>) {
        cache[getKey(converter.from, converter.to)] = converter
    }

    @Suppress("UNCHECKED_CAST")
    override fun <From, To> remove(from: Class<From>, to: Class<To>): Converter<From, To>? {
        val key = getKey(from, to)
        if (!cache.containsKey(key)) {
            val superOfFrom = from.superclass
            if (null != superOfFrom) {
                return remove(superOfFrom, to) as Converter<From, To>
            }

            throw ConverterNotExistsException(from, to)
        }

        return cache.remove(key) as Converter<From, To>?
    }

    protected fun <From, To> getKey(from: Class<From>, to: Class<To>): String {
        return "instep.typeconvert.${from.name}_${to.name}"
    }

    companion object {
        private const val serialVersionUID = -5626355667117652076L
    }
}