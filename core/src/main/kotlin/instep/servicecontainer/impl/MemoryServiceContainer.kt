package instep.servicecontainer.impl

import instep.Instep
import instep.servicecontainer.*

open class MemoryServiceContainer : ServiceContainer {
    protected val memory: MutableMap<String, Any> = mutableMapOf()
    protected var binding: ServiceBindingEventHandler? = null
    protected var bound: ServiceBoundEventHandler? = null
    protected var resolving: ServiceResolvingEventHandler? = null
    protected var resolved: ServiceResolvedEventHandler? = null

    override fun <T : Any> bind(cls: Class<T>, instance: T, tag: String) {
        if (!fireOnBinding(cls, instance, tag)) return

        memory[getKey(cls, tag)] = instance

        if (!cls.isArray && !Collection::class.java.isAssignableFrom(cls)) {
            val parents = Instep.reflect(cls).parents.filter { !it.isArray && !Collection::class.java.isAssignableFrom(it) }

            parents.forEach {
                val key = getKey(it, tag)
                if (!memory.containsKey(key)) {
                    memory[key] = instance
                }
            }
        }

        fireOnBound(cls, instance)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> remove(cls: Class<T>, tag: String): T? {
        return memory.remove(getKey(cls, tag)) as? T
    }

    override fun removeAll(instance: Any) {
        memory.filterValues { it == instance }.forEach { memory.remove(it.key) }
    }

    override fun clone(): Any {
        return super.clone()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> make(cls: Class<T>, tag: String): T {
        val key = getKey(cls, tag)
        var obj = fireOnResolving(cls)
        if (null == obj) {
            obj = memory[key] as? T
        }

        if (null == obj) throw ServiceNotFoundException(key)

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