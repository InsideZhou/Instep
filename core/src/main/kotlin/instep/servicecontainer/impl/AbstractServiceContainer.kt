@file:Suppress("FoldInitializerAndIfToElvis")

package instep.servicecontainer.impl

import instep.Instep
import instep.servicecontainer.*

@Suppress("MemberVisibilityCanPrivate", "unused")
abstract class AbstractServiceContainer<T : Any> : ServiceContainer<T> {
    override var binding: ServiceBindingEventHandler<T>? = null
    override var bound: ServiceBoundEventHandler<T>? = null
    override var resolving: ServiceResolvingEventHandler? = null
    override var resolved: ServiceResolvedEventHandler? = null

    protected val serviceBindings = mutableSetOf<ServiceBinding<out T>>()

    override fun serviceBinds(): List<ServiceBinding<out T>> {
        return serviceBindings.toList()
    }

    override fun copyServices(container: ServiceContainer<T>) {
        container.serviceBinds().forEach { bind(it) }
    }

    override fun copy(container: ServiceContainer<T>) {
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
    override fun bind(binding: ServiceBinding<out T>) {
        if (!fireOnBinding(binding)) return
        if (serviceBindings.contains(binding)) return

        val cls = binding.cls
        val instance = binding.instance
        val tag = binding.tag

        bindInstance(getKey(cls, tag), instance)
        serviceBindings.add(binding)

        if (!cls.isArray && !Collection::class.java.isAssignableFrom(cls)) {
            val parents = Instep.reflect(cls).parents.filter { !it.isArray && !Collection::class.java.isAssignableFrom(it) }

            parents.forEach {
                val key = getKey(it as Class<out T>, tag)
                if (!hasKey(key)) {
                    bindInstance(key, instance)
                }
            }
        }

        fireOnBound(binding)
    }

    protected abstract fun bindInstance(key: String, instance: T)

    protected abstract fun hasKey(key: String): Boolean

    protected fun <T : Any> getKey(cls: Class<T>, tag: String = ""): String {
        return if (tag.isBlank()) "instep.servicecontainer.${cls.name}" else "instep.servicecontainer.${cls.name}#$tag"
    }

    protected fun fireOnBinding(binding: ServiceBinding<out T>): Boolean {
        return this.binding?.handle(binding) ?: true
    }

    protected fun fireOnBound(binding: ServiceBinding<out T>) {
        bound?.handle(binding)
    }

    protected fun <T : Any> fireOnResolving(cls: Class<T>, tag: String = ""): T? {
        return resolving?.handle(cls, tag)
    }

    protected fun <T : Any> fireOnResolved(cls: Class<T>, obj: T, tag: String = "") {
        resolved?.handle(cls, obj, tag)
    }
}