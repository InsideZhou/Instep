package instep.servicecontainer

/**
 * Register service and make service instance.
 */
interface ServiceContainer : Cloneable {
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
     * Remove binding.
     * @param tag binding tagged by.
     */
    fun <T : Any> remove(cls: Class<T>, tag: String = ""): T?

    /**
     * Remove all bindings related to instance.
     */
    fun removeAll(instance: Any)

    /**
     * Fire on service binding. If event handler return null, service binding would be canceled.
     */
    fun onBinding(eventHandler: ServiceBindingEventHandler)

    /**
     * Fire on service bound.
     */
    fun onBound(eventHandler: ServiceBoundEventHandler)

    /**
     * Fire on service resolving.
     */
    fun onResolving(eventHandler: ServiceResolvingEventHandler)

    /**
     * Fire on service resolved.
     */
    fun onResolved(eventHandler: ServiceResolvedEventHandler)
}
