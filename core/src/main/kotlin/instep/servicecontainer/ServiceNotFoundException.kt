package instep.servicecontainer

class ServiceNotFoundException @JvmOverloads constructor(val key: String, override val cause: Exception? = null) : RuntimeException(key, cause) {
}