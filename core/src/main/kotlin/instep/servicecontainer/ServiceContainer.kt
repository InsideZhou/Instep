package instep.servicecontainer

/**
 * Register service and make service instance.
 */
interface ServiceContainer {
    /**
     * Fire on service binding. If event handler return null, service binding would be canceled.
     */
    var binding: ServiceBindingEventHandler?

    /**
     * Fire on service bound.
     */
    var bound: ServiceBoundEventHandler?

    /**
     * Fire on service resolving.
     */
    var resolving: ServiceResolvingEventHandler?

    /**
     * Fire on service resolved.
     */
    var resolved: ServiceResolvedEventHandler?

    /**
     * Get all original bindings of service.
     */
    fun <T : Any> serviceBinds(): List<ServiceBinding<T>>

    /**
     * Make instance by class.
     * @param tag binding tagged by.
     * @throws ServiceNotFoundException
     */
    fun <T : Any> make(cls: Class<T>, tag: String = ""): T

    /**
     * Bind instance to class. Instance which is not serializable will lose in (de)serialization.
     * @param tag binding tagged by.
     */
    fun <T : Any> bind(cls: Class<T>, instance: T, tag: String = "")

    /**
     * Bind instance to class. Instance which is not serializable will lose in (de)serialization.
     */
    fun <T : Any> bind(binding: ServiceBinding<T>)

    /**
     * Remove binding.
     * @param tag binding tagged by.
     */
    fun <T : Any> remove(cls: Class<T>, tag: String = ""): T?

    /**
     * Remove all bindings related to instance.
     */
    fun removeAll(instance: Any)

    /**
     * Clear all services in this container.
     */
    fun clear()

    /**
     * Copy services only from other service container.
     */
    fun copyServices(container: ServiceContainer)

    /**
     * Copy all from other service container.
     */
    fun copy(container: ServiceContainer)
}

data class ServiceBinding<T : Any>(val cls: Class<T>, val instance: T, val tag: String = "")