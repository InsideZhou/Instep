package instep.servicecontainer.impl

import instep.servicecontainer.ServiceBinding
import instep.servicecontainer.ServiceContainer

/**
 * Read-only service container.
 */
@Suppress("unused")
abstract class ReadOnlyServiceContainer : AbstractServiceContainer() {
    override fun <T : Any> bind(cls: Class<T>, instance: T, tag: String) {
        throw UnsupportedOperationException("Service container is read-only.")
    }

    override fun <T : Any> bind(binding: ServiceBinding<T>) {
        throw UnsupportedOperationException("Service container is read-only.")
    }

    override fun <T : Any> remove(cls: Class<T>, tag: String): T? {
        throw UnsupportedOperationException("Service container is read-only.")
    }

    override fun removeAll(instance: Any) {
        throw UnsupportedOperationException("Service container is read-only.")
    }

    override fun clear() {
        throw UnsupportedOperationException("Service container is read-only.")
    }

    override fun copyServices(container: ServiceContainer) {
        throw UnsupportedOperationException("Service container is read-only.")
    }

    override fun copy(container: ServiceContainer) {
        throw UnsupportedOperationException("Service container is read-only.")
    }
}