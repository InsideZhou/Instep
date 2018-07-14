package instep.typeconversion

import instep.Instep
import instep.cache.driver.MemoryCache

open class DefaultTypeConversion : TypeConversion {
    private val cache = MemoryCache<Converter<*, *>>()

    @Suppress("UNCHECKED_CAST")
    override fun <From, To> canConvert(from: Class<From>, to: Class<To>): Boolean {
        var result = cache.containsKey(getKey(from, to))
        if (result) return true

        result = cache.values.any { to.isAssignableFrom(it.to) && from.isAssignableFrom(it.from) }
        if (result) return true

        val mirror = Instep.reflect(to)

        mirror.findFactoryMethodBy(from)?.run {
            register(object : Converter<From, To> {
                override val from: Class<From> = from
                override val to: Class<To> = to

                override fun <T : From> convert(instance: T): To {
                    return invoke(instance) as To
                }
            })

            return true
        }

        mirror.findFactoryConstructorBy(from)?.run {
            register(object : Converter<From, To> {
                override val from: Class<From> = from
                override val to: Class<To> = to

                override fun <T : From> convert(instance: T): To {
                    return newInstance(instance) as To
                }
            })

            return true
        }

        return false
    }

    override fun <From : Any, To> convert(instance: From, to: Class<To>): To {
        return convert(instance, instance.javaClass, to)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <From : Any, To, T : From> convert(instance: T, from: Class<From>, to: Class<To>): To {
        val converter = getConverter(from, to)
        if (null != converter) return converter.convert(instance)

        val mirror = Instep.reflect(to)

        mirror.findFactoryMethodBy(from)?.run {
            register(object : Converter<From, To> {
                override val from: Class<From> = from
                override val to: Class<To> = to

                override fun <T : From> convert(instance: T): To {
                    return invoke(instance) as To
                }
            })

            return invoke(instance) as To
        }

        mirror.findFactoryConstructorBy(from)?.run {
            register(object : Converter<From, To> {
                override val from: Class<From> = from
                override val to: Class<To> = to

                override fun <T : From> convert(instance: T): To {
                    return newInstance(instance) as To
                }
            })

            return newInstance(instance) as To
        }

        throw ConverterNotExistsException(from, to)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <From, To> getConverter(from: Class<From>, to: Class<To>): Converter<From, To>? {
        val key = getKey(from, to)

        if (cache.containsKey(key)) return cache[key] as Converter<From, To>

        cache.values.find { to.isAssignableFrom(it.to) && from.isAssignableFrom(it.from) }?.let {
            return it as Converter<From, To>
        }

        val mirror = Instep.reflect(to)

        mirror.findFactoryMethodBy(from)?.run {
            val converter = object : Converter<From, To> {
                override val from: Class<From> = from
                override val to: Class<To> = to

                override fun <T : From> convert(instance: T): To {
                    return invoke(instance) as To
                }
            }

            register(converter)
        }

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

    override fun <From, To> register(converter: Converter<From, To>) {
        cache[getKey(converter.from, converter.to)] = converter
    }

    @Suppress("UNCHECKED_CAST")
    override fun <From, To> remove(from: Class<From>, to: Class<To>): Converter<From, To>? {
        val key = getKey(from, to)

        return if (cache.containsKey(key)) cache.remove(key) as Converter<From, To> else null
    }

    override fun <From, To> removeAll(from: Class<From>, to: Class<To>) {
        val keys = cache.filter { to.isAssignableFrom(it.value.to) && from.isAssignableFrom(it.value.from) }.map { it.key }
        keys.forEach { cache.remove(it) }
    }

    protected fun <From, To> getKey(from: Class<From>, to: Class<To>): String {
        return "instep.typeconversion.${from.name}_${to.name}"
    }
}