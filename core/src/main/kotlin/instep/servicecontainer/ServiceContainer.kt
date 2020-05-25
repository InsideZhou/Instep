package instep.servicecontainer

@Suppress("unused")
/**
 * Register service and make service instance.
 */
interface ServiceContainer<T : Any> {
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
    fun serviceBinds(): List<ServiceBinding<out T>>

    /**
     * Make instance by class.
     * @param tag binding tagged by.
     */
    fun <E : T> make(cls: Class<E>, tag: String = ""): E

    /**
     * Bind instance to class. Instance which is not serializable will lose in (de)serialization.
     * @param tag binding tagged by.
     */
    @Suppress("UNCHECKED_CAST")
    fun <E : T> bind(cls: Class<E>, instance: E, tag: String = "") {
        bind(ServiceBinding(cls, instance, tag))
    }

    /**
     * Bind instance to class. Instance which is not serializable will lose in (de)serialization.
     */
    fun <E : T> bind(binding: ServiceBinding<E>)

    /**
     * Remove binding.
     * @param tag binding tagged by.
     */
    fun <E : T> remove(cls: Class<E>, tag: String = ""): E?

    /**
     * Remove all bindings related to instance.
     */
    fun removeAll(instance: T)

    /**
     * Clear all services in this container.
     */
    fun clear()

    /**
     * Copy services only from other service container.
     */
    fun <E : T> copyServices(container: ServiceContainer<E>)

    /**
     * Copy all from other service container.
     */
    fun <E : T> copy(container: ServiceContainer<E>)
}

data class ServiceBinding<T>(val cls: Class<T>, val instance: T, val tag: String = "")