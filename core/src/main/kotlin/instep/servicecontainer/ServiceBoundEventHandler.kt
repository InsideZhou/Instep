package instep.servicecontainer

@Suppress("unused")
interface ServiceBoundEventHandler {
    /**
     * Handle service bound event.
     */
    fun <T> handle(binding: ServiceBinding<T>) {}
}
