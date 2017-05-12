package instep.cache.driver

import instep.cache.Cache
import instep.cache.CacheKeyNotExistsException
import java.util.concurrent.ConcurrentHashMap

open class MemoryCache : Cache {
    private val map = ConcurrentHashMap<String, CacheStore>()

    override val entries: Set<Map.Entry<String, Any>>
        get() = map.entries.map {
            object : Map.Entry<String, Any> {
                override val key: String = it.key
                override val value: Any = it.value.value
            }
        }.toSet()

    override val keys: Set<String>
        get() = map.keys

    override val size: Int
        get() = map.size

    override val values: Collection<Any>
        get() = map.values.map { it.value }

    override fun containsKey(key: String): Boolean {
        return map.containsKey(key)
    }

    override fun containsValue(value: Any): Boolean {
        return values.contains(value)
    }

    override fun isEmpty(): Boolean {
        return map.isEmpty()
    }

    override fun remove(key: String): Any? {
        return map.remove(key)
    }

    override fun put(key: String, value: Any, ttl: Int) {
        assertKeyIsValid(key)

        map.put(key, CacheStore(value, System.currentTimeMillis(), ttl))
    }

    override fun set(key: String, value: Any) {
        assertKeyIsValid(key)

        var store = map[key]
        if (null == store) {
            map.put(key, CacheStore(value, System.currentTimeMillis()))
        }
        else {
            store = store.copy(value)
            map.put(key, store)
        }
    }

    override fun get(key: String): Any? {
        val store = map[key]
        if (null == store || !isAlive(store)) return null

        return store.value
    }

    override fun touch(key: String, ttl: Int?) {
        var store = map[key]
        if (null == store) throw CacheKeyNotExistsException(key)

        store = store.copy(createdTime = System.currentTimeMillis(), ttl = ttl ?: store.ttl)
        map.put(key, store)
    }

    override fun getAlive(): Map<String, Any> {
        return map.filterValues { store -> isAlive(store) }
    }

    override fun getExpired(): Map<String, Any> {
        return map.filterValues { store -> !isAlive(store) }
    }

    override fun clearExpired(): Map<String, Any> {
        val expired = map.filterValues { store -> !isAlive(store) }
        expired.forEach { pair -> map.remove(pair.key) }
        return expired.mapValues { store -> store.value.value }
    }

    protected fun isAlive(store: CacheStore): Boolean {
        return store.ttl < 0 || store.createdTime + store.ttl > System.currentTimeMillis()
    }
}

data class CacheStore(val value: Any, val createdTime: Long, val ttl: Int = -1)
