package instep.servicecontainer

import java.io.Serializable

/**
 * Register service and make service instance.
 */
interface ServiceContainer : Serializable {
    /**
     * Make instance by class.
     * @param tag binding tagged by.
     * @throws ServiceNotFoundException
     */
    fun <T : Any> make(cls: Class<T>, tag: String = ""): T

    /**
     * Bind instance to class. Instance which is not serializable will lose in (de)serializable.
     * @param tag binding tagged by.
     */
    fun <T : Any> bind(cls: Class<T>, obj: T, tag: String = "")

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
