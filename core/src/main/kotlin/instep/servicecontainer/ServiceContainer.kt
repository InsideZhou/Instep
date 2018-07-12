package instep.servicecontainer

@Suppress("unused")
/**
 * Register service and make service instance.
 */
interface ServiceContainer<T : Any> {
    /**
     * Fire on service binding. If event handler return null, service binding would be canceled.
     */
    var binding: ServiceBindingEventHandler<T>?

    /**
     * Fire on service bound.
     */
    var bound: ServiceBoundEventHandler<T>?

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
    fun <T : Any> make(cls: Class<T>, tag: String = ""): T

    /**
     * Bind instance to class. Instance which is not serializable will lose in (de)serialization.
     * @param tag binding tagged by.
     */
    @Suppress("UNCHECKED_CAST")
    fun bind(cls: Class<out T>, instance: T, tag: String = "") {
        val binding = ServiceBinding(cls, instance, tag)
        bind(binding)
    }

    /**
     * Bind instance to class. Instance which is not serializable will lose in (de)serialization.
     */
    fun bind(binding: ServiceBinding<out T>)

    /**
     * Remove binding.
     * @param tag binding tagged by.
     */
    fun <T : Any> remove(cls: Class<T>, tag: String = ""): T?

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
    fun copyServices(container: ServiceContainer<T>)

    /**
     * Copy all from other service container.
     */
    fun copy(container: ServiceContainer<T>)
}

data class ServiceBinding<T : Any>(val cls: Class<out T>, val instance: T, val tag: String = "")