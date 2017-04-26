package instep.servicecontainer.impl

import instep.Instep
import instep.servicecontainer.ServiceBinding
import instep.servicecontainer.ServiceNotFoundException

open class MemoryServiceContainer : AbstractServiceContainer<MemoryServiceContainer>() {
    protected val memory = mutableMapOf<String, Any>()

    @Suppress("unchecked_cast")
    override fun <T : Any> bind(cls: Class<T>, instance: T, tag: String) {
        if (!fireOnBinding(cls, instance, tag)) return

        memory[getKey(cls, tag)] = instance
        serviceBindings.add(ServiceBinding(cls, instance, tag) as ServiceBinding<Any>)

        if (!cls.isArray && !Collection::class.java.isAssignableFrom(cls)) {
            val parents = Instep.reflect(cls).parents.filter { !it.isArray && !Collection::class.java.isAssignableFrom(it) }

            parents.forEach {
                val key = getKey(it, tag)
                if (!memory.containsKey(key)) {
                    memory[key] = instance
                }
            }
        }

        fireOnBound(cls, instance)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> remove(cls: Class<T>, tag: String): T? {
        return memory.remove(getKey(cls, tag)) as? T
    }

    override fun removeAll(instance: Any) {
        memory.filterValues { it == instance }.forEach { memory.remove(it.key) }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> make(cls: Class<T>, tag: String): T {
        val key = getKey(cls, tag)
        var obj = fireOnResolving(cls)
        if (null == obj) {
            obj = memory[key] as? T
        }

        if (null == obj) throw ServiceNotFoundException(key)

        fireOnResolved(cls, obj)
        return obj
    }

    override fun clear() {
        super.clear()
        memory.clear()
    }

    companion object {
        private const val serialVersionUID = 1650446412812766180L
    }
}