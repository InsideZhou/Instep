package instep.typeconversion

import instep.Instep
import instep.cache.driver.MemoryCache

@Suppress("DuplicatedCode")
open class DefaultTypeConversion : TypeConversion {
    private val cache = MemoryCache<Converter<*, *>>()

    @Suppress("UNCHECKED_CAST")
    override fun <From, To> getConverter(from: Class<From>, to: Class<To>, tag: String): Converter<From, To>? {
        val key = getKey(from, to, tag)

        if (cache.containsKey(key)) return cache[key] as Converter<From, To>
        if (tag.isNotBlank()) return null

        cache.values.find { to.isAssignableFrom(it.to) && from.isAssignableFrom(it.from) }?.let {
            return it as Converter<From, To>
        }

        val mirror = Instep.reflect(to)

        mirror.findFactoryConstructorBy(from)?.run {
            val converter = object : Converter<From, To> {
                override val from: Class<From> = from
                override val to: Class<To> = to

                override fun <T : From> convert(instance: T): To {
                    return newInstance(instance) as To
                }
            }

            register(converter)
        }

        return null
    }

    override fun <From, To> register(converter: Converter<From, To>, tag: String) {
        cache[getKey(converter, tag)] = converter
    }

    override fun <From, To> unregister(converter: Converter<From, To>, tag: String) {
        getKey(converter, tag).let { key ->
            if (cache.containsKey(key)) cache.remove(key)
        }
    }

    override fun <From, To> removeAll(from: Class<From>, to: Class<To>) {
        val keys = cache.filter { to.isAssignableFrom(it.value.to) && from.isAssignableFrom(it.value.from) }.map { it.key }
        keys.forEach { cache.remove(it) }
    }

    protected open fun <From, To> getKey(from: Class<From>, to: Class<To>, tag: String): String {
        return "instep.typeconversion.${from.toGenericString()}_${to.toGenericString()}#${tag}"
    }

    protected open fun getKey(converter: Converter<*, *>, tag: String): String {
        return getKey(converter.from, converter.to, tag)
    }
}