package instep.servicecontainer

@Suppress("unused")
interface ServiceBindingEventHandler<T : Any> {
    /**
     * Handle service binding event.

     * @return If false, service binding will be canceled.
     */
    fun handle(binding: ServiceBinding<out T>): Boolean {
        return true
    }
}
