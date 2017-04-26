package instep.servicecontainer.impl

import instep.servicecontainer.ServiceBinding
import instep.servicecontainer.ServiceContainer
import instep.servicecontainer.ServiceNotFoundException

/**
 * Combine services in multi containers.
 */
open class CompositeServiceContainer(val primary: ServiceContainer, vararg val secondary: ServiceContainer) : AbstractServiceContainer<CompositeServiceContainer>() {
    private val containers = listOf(primary) + secondary

    override fun <T : Any> serviceBinds(): List<ServiceBinding<T>> {
        return containers.map { it.serviceBinds<T>() }.flatten()
    }

    override fun <T : Any> make(cls: Class<T>, tag: String): T {
        containers.forEach {
            try {
                return it.make(cls, tag)
            }
            catch(e: ServiceNotFoundException) {
                //pass
            }
        }

        throw ServiceNotFoundException(getKey(cls, tag))
    }

    override fun <T : Any> bind(cls: Class<T>, instance: T, tag: String) {
        primary.bind(cls, instance, tag)
    }

    override fun <T : Any> bind(binding: ServiceBinding<T>) {
        primary.bind(binding)
    }

    override fun <T : Any> remove(cls: Class<T>, tag: String): T? {
        return primary.remove(cls, tag)
    }

    override fun removeAll(instance: Any) {
        primary.removeAll(instance)
    }

    override fun clear() {
        primary.clear()
    }

    override fun copyServices(container: ServiceContainer) {
        primary.copyServices(container)
    }

    override fun copy(container: ServiceContainer) {
        primary.copy(container)
    }
}