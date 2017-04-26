package instep.servicecontainer.impl

import instep.servicecontainer.ServiceResolvingEventHandler

open class CompositeServiceResolvingEventHandler(vararg val handlers: ServiceResolvingEventHandler) : ServiceResolvingEventHandler {
    override fun <T> handle(cls: Class<T>, tag: String): T? {
        handlers.forEach {
            val service = it.handle(cls, tag)
            if (null != service) return service
        }

        return null
    }
}