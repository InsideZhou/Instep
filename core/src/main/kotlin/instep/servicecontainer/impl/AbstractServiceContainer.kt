@file:Suppress("FoldInitializerAndIfToElvis")

package instep.servicecontainer.impl

import instep.Instep
import instep.servicecontainer.*

@Suppress("MemberVisibilityCanPrivate")
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

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> bind(binding: ServiceBinding<T>) {
        if (!fireOnBinding(binding)) return
        if (serviceBindings.contains(binding as ServiceBinding<Any>)) return

        val cls = binding.cls
        val instance = binding.instance
        val tag = binding.tag

        bindInstance(getKey(cls, tag), instance)
        serviceBindings.add(binding)

        if (!cls.isArray && !Collection::class.java.isAssignableFrom(cls)) {
            val parents = Instep.reflect(cls).parents.filter { !it.isArray && !Collection::class.java.isAssignableFrom(it) }

            parents.forEach {
                val key = getKey(it, tag)
                if (!hasKey(key)) {
                    bindInstance(key, instance)
                }
            }
        }

        fireOnBound(binding)
    }

    abstract protected fun <T : Any> bindInstance(key: String, instance: T)

    abstract protected fun hasKey(key: String): Boolean

    protected fun <T> getKey(cls: Class<T>, tag: String = ""): String {
        return if (tag.isBlank()) "instep.servicecontainer.${cls.name}" else "instep.servicecontainer.${cls.name}#$tag"
    }

    protected fun <T : Any> fireOnBinding(binding: ServiceBinding<T>): Boolean {
        return this.binding?.handle(binding) ?: true
    }

    protected fun <T : Any> fireOnBound(binding: ServiceBinding<T>) {
        bound?.handle(binding)
    }

    protected fun <T> fireOnResolving(cls: Class<T>, tag: String = ""): T? {
        return resolving?.handle(cls, tag)
    }

    protected fun <T> fireOnResolved(cls: Class<T>, obj: T, tag: String = "") {
        resolved?.handle(cls, obj, tag)
    }
}