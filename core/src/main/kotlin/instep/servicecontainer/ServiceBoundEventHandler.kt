package instep.servicecontainer

interface ServiceBoundEventHandler {
    /**
     * Handle service bound event.
     */
    fun <T> handle(cls: Class<T>, obj: T, tag: String = "")
}
