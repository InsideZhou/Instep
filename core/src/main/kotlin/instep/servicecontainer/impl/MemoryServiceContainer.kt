package instep.servicecontainer.impl

import instep.cache.driver.MemoryCache
import instep.servicecontainer.ServiceNotFoundException

open class MemoryServiceContainer<T>(memoryCache: MemoryCache<T>) : AbstractServiceContainer<T>() {
    private val memory = memoryCache

    override fun bindInstance(key: String, instance: T) {
        memory[key] = instance
    }

    override fun hasKey(key: String): Boolean = memory.containsKey(key)

    @Suppress("UNCHECKED_CAST")
    override fun <E : T> remove(cls: Class<E>, tag: String): E? {
        return memory.remove(getKey(cls, tag)) as? E
    }

    override fun removeAll(instance: T) {
        memory.filterValues { it == instance }.forEach { memory.remove(it.key) }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <E : T> make(cls: Class<E>, tag: String): E {
        val key = getKey(cls, tag)

        val obj = fireOnResolving(cls) ?: memory[key]

        return obj as? E ?: throw ServiceNotFoundException(key)
    }

    override fun clear() {
        super.clear()
        memory.keys.forEach { memory.remove(it) }
    }
}