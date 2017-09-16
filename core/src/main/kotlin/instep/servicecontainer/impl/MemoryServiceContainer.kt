package instep.servicecontainer.impl

import instep.cache.driver.MemoryCache
import instep.servicecontainer.ServiceNotFoundException

open class MemoryServiceContainer(memoryCache: MemoryCache) : AbstractServiceContainer() {
    private val memory = memoryCache

    override fun <T : Any> bindInstance(key: String, instance: T) {
        memory[key] = instance
    }

    override fun hasKey(key: String): Boolean = memory.containsKey(key)

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
        memory.keys.forEach { memory.remove(it) }
    }
}