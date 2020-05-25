@file:Suppress("FoldInitializerAndIfToElvis")

package instep.servicecontainer.impl

import instep.Instep
import instep.servicecontainer.*

@Suppress("MemberVisibilityCanPrivate", "unused", "MemberVisibilityCanBePrivate")
abstract class AbstractServiceContainer<T : Any> : ServiceContainer<T> {
    override var binding: ServiceBindingEventHandler? = null
    override var bound: ServiceBoundEventHandler? = null
    override var resolving: ServiceResolvingEventHandler? = null
    override var resolved: ServiceResolvedEventHandler? = null

    protected val serviceBindings = mutableSetOf<ServiceBinding<out T>>()

    @Suppress("UNCHECKED_CAST")
    override fun serviceBinds(): List<ServiceBinding<out T>> {
        return serviceBindings.toList()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <E : T> copyServices(container: ServiceContainer<E>) {
        container.serviceBinds().forEach { bind(it) }
    }

    override fun <E : T> copy(container: ServiceContainer<E>) {
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
    override fun <E : T> bind(binding: ServiceBinding<E>) {
        if (!fireOnBinding(binding)) return
        if (serviceBindings.contains(binding)) return

        val cls = binding.cls
        val instance = binding.instance
        val tag = binding.tag

        bindInstance(getKey(cls, tag), instance)
        serviceBindings.add(binding)

        if (!cls.isArray && !Collection::class.java.isAssignableFrom(cls)) {
            val parents = Instep.reflectFromClass(cls as Class<*>).parents.filter { !it.isArray && !Collection::class.java.isAssignableFrom(it) }

            parents.forEach {
                val key = getKey(it as Class<T>, tag)
                if (!hasKey(key)) {
                    bindInstance(key, instance)
                }
            }
        }

        fireOnBound(binding)
    }

    protected abstract fun bindInstance(key: String, instance: T)

    protected abstract fun hasKey(key: String): Boolean

    protected fun <E : T> getKey(cls: Class<E>, tag: String = ""): String {
        return if (tag.isBlank()) "instep.servicecontainer.${cls.name}" else "instep.servicecontainer.${cls.name}#$tag"
    }

    protected fun fireOnBinding(binding: ServiceBinding<out T>): Boolean {
        return this.binding?.handle(binding) ?: true
    }

    protected fun fireOnBound(binding: ServiceBinding<out T>) {
        bound?.handle(binding)
    }

    protected fun fireOnResolving(cls: Class<out T>, tag: String = ""): T? {
        return resolving?.handle(cls, tag)
    }

    protected fun fireOnResolved(cls: Class<out T>, obj: T, tag: String = "") {
        resolved?.handle(cls, obj, tag)
    }
}