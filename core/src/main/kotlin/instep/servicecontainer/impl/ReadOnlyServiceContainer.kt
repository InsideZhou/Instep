package instep.servicecontainer.impl

import instep.servicecontainer.ServiceBinding
import instep.servicecontainer.ServiceContainer

/**
 * Read-only service container.
 */
@Suppress("unused")
abstract class ReadOnlyServiceContainer<T : Any> : AbstractServiceContainer<T>() {
    override fun bind(cls: Class<out T>, instance: T, tag: String) {
        throw UnsupportedOperationException("Service container is read-only.")
    }

    override fun bind(binding: ServiceBinding<out T>) {
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

    override fun copyServices(container: ServiceContainer<out T>) {
        throw UnsupportedOperationException("Service container is read-only.")
    }

    override fun copy(container: ServiceContainer<out T>) {
        throw UnsupportedOperationException("Service container is read-only.")
    }
}