package instep.servicecontainer

@Suppress("unused")
interface ServiceBoundEventHandler<T : Any> {
    /**
     * Handle service bound event.
     */
    fun handle(binding: ServiceBinding<out T>) {}
}
