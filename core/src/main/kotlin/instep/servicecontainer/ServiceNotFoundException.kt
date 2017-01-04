package instep.servicecontainer

class ServiceNotFoundException(val key: String) : RuntimeException("Service $key not found.") {
}