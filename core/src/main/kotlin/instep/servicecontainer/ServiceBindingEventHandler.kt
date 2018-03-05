package instep.servicecontainer

interface ServiceBindingEventHandler {
    /**
     * Handle service binding event.

     * @return If false, service binding will be canceled.
     */
    fun <T : Any> handle(binding: ServiceBinding<T>): Boolean
}
