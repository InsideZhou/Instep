package instep.servicecontainer

interface ServiceResolvedEventHandler {
    /**
     * Handle service resolved event.
     */
    fun <T : Any> handle(cls: Class<T>, obj: T, tag: String = "") {}
}
