package instep

import instep.cache.Cache
import instep.cache.driver.MemoryCache
import instep.reflection.Mirror
import instep.reflection.MirrorImpl
import instep.servicecontainer.ServiceContainer
import instep.servicecontainer.impl.MemoryServiceContainer
import instep.typeconvert.TypeConvert
import instep.typeconvert.TypeConvertException
import instep.typeconvert.DefaultTypeConvert

object Instep {
    var serviceContainer: ServiceContainer

    init {
        val container = MemoryServiceContainer(MemoryCache())
        serviceContainer = container

        container.bind(Cache::class.java, MemoryCache())
        container.bind(TypeConvert::class.java, DefaultTypeConvert())
    }

    /**
     * @see ServiceContainer.bind
     */
    fun <T : Any> bind(cls: Class<T>, obj: T, tag: String = "") {
        return serviceContainer.bind(cls, obj, tag)
    }

    /**
     * @see ServiceContainer.make
     */
    fun <T : Any> make(cls: Class<T>, tag: String = ""): T {
        return serviceContainer.make(cls, tag)
    }

    /**
     * @see TypeConvert.convert
     */
    @Throws(TypeConvertException::class)
    fun <From : Any, To, T : From> convert(instance: T, from: Class<From>, to: Class<To>): To {
        val typeConvert = Instep.make(TypeConvert::class.java)
        return typeConvert.convert(instance, from, to)
    }

    /**
     * Reflect class.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> reflect(cls: Class<T>): Mirror<T> {
        val cache = Instep.make(Cache::class.java)
        val mirror = if (cache.containsKey(cls.name)) {
            cache[cls.name] as Mirror<T>
        }
        else {
            val m = MirrorImpl(cls)
            cache[cls.name] = m
            m
        }

        return mirror
    }

    /**
     * Reflect instance.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> reflect(instance: T): Mirror<T> {
        val cls = instance.javaClass
        val cache = Instep.make(Cache::class.java)
        val mirror = if (cache.containsKey(cls.name)) {
            cache[cls.name] as Mirror<T>
        }
        else {
            val m = MirrorImpl(cls)
            cache[cls.name] = m
            m
        }

        return mirror
    }
}
