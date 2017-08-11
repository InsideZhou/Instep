package instep.servicecontainer.impl

import instep.servicecontainer.*

abstract class AbstractServiceContainer : ServiceContainer {
    override var binding: ServiceBindingEventHandler? = null
    override var bound: ServiceBoundEventHandler? = null
    override var resolving: ServiceResolvingEventHandler? = null
    override var resolved: ServiceResolvedEventHandler? = null

    protected val serviceBindings = mutableSetOf<ServiceBinding<Any>>()

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> serviceBinds(): List<ServiceBinding<T>> {
        return serviceBindings.map { it as ServiceBinding<T> }
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
        serviceBindings.clear()

        binding = null
        bound = null
        resolving = null
        resolved = null
    }

    override fun <T : Any> bind(binding: ServiceBinding<T>) {
        bind(binding.cls, binding.instance, binding.tag)
    }

    protected fun <T> getKey(cls: Class<T>, tag: String = ""): String {
        return if (tag.isBlank()) "instep.servicecontainer.${cls.name}" else "instep.servicecontainer.${cls.name}#$tag"
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
}