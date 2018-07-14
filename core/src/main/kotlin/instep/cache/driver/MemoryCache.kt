package instep.cache.driver

import instep.cache.Cache
import instep.cache.Cache.Companion.assertKeyIsValid
import instep.cache.CacheKeyNotExistsException
import java.util.concurrent.ConcurrentHashMap

open class MemoryCache<T> : Cache<T> {
    private val map = ConcurrentHashMap<String, CacheStore<T>>()

    override val entries: Set<Map.Entry<String, T>>
        get() = map.entries.map {
            object : Map.Entry<String, T> {
                override val key: String = it.key
                override val value: T = it.value.value
            }
        }.toSet()

    override val keys: Set<String>
        get() = map.keys

    override val size: Int
        get() = map.size

    override val values: Collection<T>
        get() = map.values.map { it.value }

    override fun containsKey(key: String): Boolean {
        return map.containsKey(key)
    }

    override fun containsValue(value: T): Boolean {
        return values.contains(value)
    }

    override fun isEmpty(): Boolean {
        return map.isEmpty()
    }

    @Suppress("UNCHECKED_CAST")
    override fun remove(key: String): T? {
        return map.remove(key)?.value
    }

    override fun put(key: String, value: T, ttl: Int) {
        assertKeyIsValid(key)

        map[key] = CacheStore(value, System.currentTimeMillis(), ttl)
    }

    override fun set(key: String, value: T) {
        assertKeyIsValid(key)

        var store = map[key]
        if (null == store) {
            map[key] = CacheStore(value, System.currentTimeMillis())
        }
        else {
            store = store.copy(value)
            map[key] = store
        }
    }

    override fun get(key: String): T? {
        val store = map[key]
        if (null == store || !isAlive(store)) return null

        return store.value
    }

    override fun touch(key: String, ttl: Int?) {
        var store = map[key]
        if (null == store) throw CacheKeyNotExistsException(key)

        store = store.copy(createdTime = System.currentTimeMillis(), ttl = ttl ?: store.ttl)
        map[key] = store
    }

    override fun getAlive(): Map<String, T> {
        return map.filterValues { store -> isAlive(store) }.mapValues { store -> store.value.value }
    }

    override fun getExpired(): Map<String, T> {
        return map.filterValues { store -> !isAlive(store) }.mapValues { store -> store.value.value }
    }

    override fun clearExpired(): Map<String, T> {
        val expired = map.filterValues { store -> !isAlive(store) }
        expired.forEach { pair -> map.remove(pair.key) }
        return expired.mapValues { store -> store.value.value }
    }

    open protected fun isAlive(store: CacheStore<T>): Boolean {
        return store.ttl < 0 || store.createdTime + store.ttl > System.currentTimeMillis()
    }
}

data class CacheStore<T>(val value: T, val createdTime: Long, val ttl: Int = -1)
