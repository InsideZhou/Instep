package instep.servicecontainer

class ServiceNotFoundException(val key: String) : RuntimeException(key) {
}