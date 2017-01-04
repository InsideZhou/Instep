package instep.servicecontainer

interface ServiceResolvedEventHandler {
    /**
     * Handle service resolved event.
     */
    fun <T> handle(cls: Class<T>, obj: T, tag: String = "")
}
