package instep.servicecontainer

interface ServiceResolvedEventHandler {
    /**
     * Handle service resolved event.
     */
    fun <T> handle(cls: Class<out T>, obj: T, tag: String = "") {}
}
