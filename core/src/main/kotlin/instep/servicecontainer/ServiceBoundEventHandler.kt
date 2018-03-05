package instep.servicecontainer

interface ServiceBoundEventHandler {
    /**
     * Handle service bound event.
     */
    fun <T : Any> handle(binding: ServiceBinding<T>)
}
