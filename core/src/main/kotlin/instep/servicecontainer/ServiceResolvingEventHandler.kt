package instep.servicecontainer

interface ServiceResolvingEventHandler {
    /**
     * Handle service resolving event.
     *
     * @return If not null, stop processing and return as service.
     */
    fun <T : Any> handle(cls: Class<T>, tag: String = ""): T? {
        return null
    }
}
