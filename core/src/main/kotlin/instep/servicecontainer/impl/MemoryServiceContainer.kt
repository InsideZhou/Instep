package instep.servicecontainer.impl

import instep.cache.driver.MemoryCache
import instep.servicecontainer.ServiceNotFoundException

open class MemoryServiceContainer<T : Any>(memoryCache: MemoryCache) : AbstractServiceContainer<T>() {
    private val memory = memoryCache

    override fun bindInstance(key: String, instance: T) {
        memory[key] = instance
    }

    override fun hasKey(key: String): Boolean = memory.containsKey(key)

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> remove(cls: Class<T>, tag: String): T? {
        return memory.remove(getKey(cls, tag)) as? T
    }

    override fun removeAll(instance: T) {
        memory.filterValues { it == instance }.forEach { memory.remove(it.key) }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> make(cls: Class<T>, tag: String): T {
        val key = getKey(cls, tag)

        val obj = fireOnResolving(cls) ?: memory[key] as? T

        return obj ?: throw ServiceNotFoundException(key)
    }

    override fun clear() {
        super.clear()
        memory.keys.forEach { memory.remove(it) }
    }
}