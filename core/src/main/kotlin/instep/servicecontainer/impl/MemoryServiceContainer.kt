package instep.servicecontainer.impl

import instep.Instep
import instep.servicecontainer.ServiceBinding
import instep.servicecontainer.ServiceContainer
import instep.servicecontainer.ServiceNotFoundException

open class MemoryServiceContainer : AbstractServiceContainer<MemoryServiceContainer>() {
    protected val memory = mutableMapOf<String, Any>()

    protected val serviceBindings = mutableListOf<ServiceBinding<Any>>()

    @Suppress("unchecked_cast")
    override fun <T : Any> bind(cls: Class<T>, instance: T, tag: String) {
        if (!fireOnBinding(cls, instance, tag)) return

        memory[getKey(cls, tag)] = instance
        serviceBindings.add(ServiceBinding(cls, instance, tag) as ServiceBinding<Any>)

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
    override fun <T : Any> serviceBinds(): List<ServiceBinding<T>> {
        return serviceBindings.map { it as ServiceBinding<T> }
    }

    override fun <T : Any> bind(binding: ServiceBinding<T>) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> remove(cls: Class<T>, tag: String): T? {
        return memory.remove(getKey(cls, tag)) as? T
    }

    override fun removeAll(instance: Any) {
        memory.filterValues { it == instance }.forEach { memory.remove(it.key) }
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

    override fun copyServices(container: ServiceContainer) {
        container.serviceBinds<Any>().forEach { bind(it) }
    }

    override fun copy(container: ServiceContainer) {
        copyServices(container)

        binding = container.binding
        bound = container.bound
        resolving = container.resolving
        resolved = container.resolved
    }

    override fun clear() {
        memory.clear()
        serviceBindings.clear()

        binding = null
        bound = null
        resolving = null
        resolved = null
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

    companion object {
        private const val serialVersionUID = 1650446412812766180L
    }
}