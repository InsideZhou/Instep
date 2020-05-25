package instep.servicecontainer.impl

import instep.servicecontainer.ServiceBinding
import instep.servicecontainer.ServiceContainer
import instep.servicecontainer.ServiceNotFoundException

/**
 * Combine services in multi containers.
 */
@Suppress("CanBeParameter")
open class CompositeServiceContainer<T : Any>(val primary: ServiceContainer<T>, vararg val secondary: ServiceContainer<T>) : AbstractServiceContainer<T>() {
    private val containers = listOf(primary) + secondary

    override fun serviceBinds(): List<ServiceBinding<out T>> {
        return containers.map { it.serviceBinds() }.flatten()
    }

    override fun <E : T> make(cls: Class<E>, tag: String): E {
        containers.forEach {
            it.make(cls, tag).let { return it }
        }

        throw ServiceNotFoundException(getKey(cls, tag))
    }

    override fun <E : T> bind(cls: Class<E>, instance: E, tag: String) {
        primary.bind(cls, instance, tag)
    }

    override fun <E : T> bind(binding: ServiceBinding<E>) {
        primary.bind(binding)
    }

    override fun bindInstance(key: String, instance: T) {
        throw UnsupportedOperationException("composite service container don't actually need this.")
    }

    override fun hasKey(key: String): Boolean {
        throw UnsupportedOperationException("composite service container don't actually need this.")
    }

    override fun <E : T> remove(cls: Class<E>, tag: String): E? {
        return primary.remove(cls, tag)
    }

    override fun removeAll(instance: T) {
        primary.removeAll(instance)
    }

    override fun clear() {
        primary.clear()
    }

    override fun <E : T> copyServices(container: ServiceContainer<E>) {
        primary.copyServices(container)
    }

    override fun <E : T> copy(container: ServiceContainer<E>) {
        primary.copy(container)
    }
}