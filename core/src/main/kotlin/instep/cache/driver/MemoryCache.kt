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

    override fun get(key: String): Any {
        val store = map[key]

        return when (store) {
            null -> throw CacheKeyNotExistsException(key)
            else -> store.value
        }
    }

    override fun set(key: String, value: Any) {
        put(key, value, -1)
    }

    override fun getAlive(key: String): Any? {
        val store = map[key]
        if (null == store || !storeAlive(store)) return null

        return store.value
    }

    override fun put(key: String, value: Any, ttl: Int) {
        map.put(key, CacheStore(value, System.currentTimeMillis(), ttl))
    }

    override fun touch(key: String, ttl: Int?) {
        var store = map[key]
        if (null == store) throw CacheKeyNotExistsException(key)

        store = store.copy(timestamp = System.currentTimeMillis(), ttl = ttl ?: store.ttl)
        map.put(key, store)
    }

    override fun cleanExpires(): Map<String, Any> {
        val expired = map.filterValues { store -> !storeAlive(store) }
        expired.forEach { pair -> map.remove(pair.key) }
        return expired.mapValues { store -> store.value.value }
    }

    override fun getAlive(): Map<String, Any> {
        return map.filterValues { store -> storeAlive(store) }
    }

    override fun toMap(): Map<String, Any> {
        return map.mapValues { it.value.value }
    }

    protected fun storeAlive(store: CacheStore): Boolean {
        return store.ttl < 0 || store.timestamp + store.ttl > System.currentTimeMillis()
    }

    companion object {
        private const val serialVersionUID = -3085179225103617058L
    }
}

data class CacheStore(val value: Any, val timestamp: Long, val ttl: Int = -1)
