package instep.servicecontainer

interface ServiceBindingEventHandler {
    /**
     * Handle service binding event.

     * @return If null, service binding will be canceled.
     */
    fun <T> handle(cls: Class<T>, obj: T, tag: String = ""): Boolean
}
