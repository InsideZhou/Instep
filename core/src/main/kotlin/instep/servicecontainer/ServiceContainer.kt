package instep.servicecontainer

@Suppress("unused")
/**
 * Register service and make service instance.
 */
interface ServiceContainer<T> {
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
    fun bind(cls: Class<out T>, instance: T, tag: String = "") {
        bind(ServiceBinding(cls, instance, tag))
    }

    /**
     * Bind instance to class. Instance which is not serializable will lose in (de)serialization.
     */
    fun bind(binding: ServiceBinding<out T>)

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
    fun copyServices(container: ServiceContainer<out T>)

    /**
     * Copy all from other service container.
     */
    fun copy(container: ServiceContainer<out T>)
}

data class ServiceBinding<T>(val cls: Class<out T>, val instance: T, val tag: String = "")