package instep.servicecontainer.impl

import instep.Instep
import instep.cache.Cache
import instep.cache.CacheKeyNotExistsException
import instep.servicecontainer.*
import java.io.Serializable

open class MemoryServiceContainer(protected val cache: Cache) : ServiceContainer {
    protected var binding: ServiceBindingEventHandler? = null
    protected var bound: ServiceBoundEventHandler? = null
    protected var resolving: ServiceResolvingEventHandler? = null
    protected var resolved: ServiceResolvedEventHandler? = null

    @Transient
    protected val localMap = mutableMapOf<String, Any>()

    override fun <T : Any> bind(cls: Class<T>, obj: T, tag: String) {
        if (!fireOnBinding(cls, obj, tag)) return

        if (obj is Serializable) {
            cache[getKey(cls, tag)] = obj
        }
        else {
            localMap[getKey(cls, tag)] = obj
        }

        if (!cls.isArray && !Collection::class.java.isAssignableFrom(cls)) {
            val parents = Instep.reflect(cls).parents.filter { !it.isArray && !Collection::class.java.isAssignableFrom(it) }

            if (obj is Serializable) {
                parents.forEach {
                    val key = getKey(it, tag)
                    if (!cache.containsKey(key)) {
                        cache[key] = obj
                    }
                }
            }
            else {
                parents.forEach {
                    val key = getKey(it, tag)
                    if (!localMap.containsKey(key)) {
                        localMap[key] = obj
                    }
                }
            }
        }

        fireOnBound(cls, obj)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> make(cls: Class<T>, tag: String): T {
        val key = getKey(cls, tag)
        val obj: T = try {
            fireOnResolving(cls) ?: (localMap[key] as? T ?: cache[key] as T)
        }
        catch(e: CacheKeyNotExistsException) {
            throw ServiceNotFoundException(key)
        }

        fireOnResolved(cls, obj)
        return obj
    }

    override fun onBinding(eventHandler: ServiceBindingEventHandler) {
        binding = eventHandler
    }

    override fun onBound(eventHandler: ServiceBoundEventHandler) {
        bound = eventHandler
    }

    override fun onResolving(eventHandler: ServiceResolvingEventHandler) {
        resolving = eventHandler
    }

    override fun onResolved(eventHandler: ServiceResolvedEventHandler) {
        resolved = eventHandler
    }

    protected fun <T> fireOnBinding(cls: Class<T>, obj: T, tag: String = ""): Boolean {
        val handler = binding
        if (null == handler) return true

        return handler.handle(cls, obj, tag)
    }

    protected fun <T> fireOnBound(cls: Class<T>, obj: T, tag: String = "") {
        val handler = bound
        if (null == handler) return

        return handler.handle(cls, obj, tag)
    }

    protected fun <T> fireOnResolving(cls: Class<T>, tag: String = ""): T? {
        val handler = resolving
        if (null == handler) return null

        return handler.handle(cls, tag)
    }

    protected fun <T> fireOnResolved(cls: Class<T>, obj: T, tag: String = "") {
        val handler = resolved
        if (null == handler) return

        return handler.handle(cls, obj, tag)
    }

    protected fun <T> getKey(cls: Class<T>, tag: String = ""): String {
        return if (tag.isBlank()) "instep.servicecontainer.${cls.name}" else "instep.servicecontainer.${cls.name}#$tag"
    }

    companion object {
        private const val serialVersionUID = 1650446412812766180L
    }
}