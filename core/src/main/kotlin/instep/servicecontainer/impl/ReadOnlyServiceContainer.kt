package instep.servicecontainer.impl

import instep.servicecontainer.ServiceBinding
import instep.servicecontainer.ServiceContainer

/**
 * Read-only service container.
 */
@Suppress("unused")
abstract class ReadOnlyServiceContainer<T : Any> : AbstractServiceContainer<T>() {
    override fun <E : T> bind(cls: Class<E>, instance: E, tag: String) {
        throw UnsupportedOperationException("Service container is read-only.")
    }

    override fun <E : T> bind(binding: ServiceBinding<E>) {
        throw UnsupportedOperationException("Service container is read-only.")
    }

    override fun <E : T> remove(cls: Class<E>, tag: String): E? {
        throw UnsupportedOperationException("Service container is read-only.")
    }

    override fun removeAll(instance: T) {
        throw UnsupportedOperationException("Service container is read-only.")
    }

    override fun clear() {
        throw UnsupportedOperationException("Service container is read-only.")
    }

    override fun <E : T> copyServices(container: ServiceContainer<E>) {
        throw UnsupportedOperationException("Service container is read-only.")
    }

    override fun <E : T> copy(container: ServiceContainer<E>) {
        throw UnsupportedOperationException("Service container is read-only.")
    }
}