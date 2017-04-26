package instep.servicecontainer.impl

import instep.servicecontainer.*

abstract class AbstractServiceContainer<out T : ServiceContainer> : ServiceContainer {
    override var binding: ServiceBindingEventHandler? = null
    override var bound: ServiceBoundEventHandler? = null
    override var resolving: ServiceResolvingEventHandler? = null
    override var resolved: ServiceResolvedEventHandler? = null

    protected fun <T> getKey(cls: Class<T>, tag: String = ""): String {
        return if (tag.isBlank()) "instep.servicecontainer.${cls.name}" else "instep.servicecontainer.${cls.name}#$tag"
    }
}