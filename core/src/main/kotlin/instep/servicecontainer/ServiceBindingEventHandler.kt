package instep.servicecontainer

@Suppress("unused")
interface ServiceBindingEventHandler {
    /**
     * Handle service binding event.

     * @return If false, service binding will be canceled.
     */
    fun <T> handle(binding: ServiceBinding<T>): Boolean {
        return true
    }
}
