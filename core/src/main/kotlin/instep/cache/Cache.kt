package instep.cache

import instep.cache.driver.CacheStore
import java.io.Serializable

/**
 * Cache with ttl(time to live, in millisecond) to every key-value pair.
 */
interface Cache : Serializable, Map<String, Any> {
    fun <T : Serializable> put(key: String, value: T, ttl: Int)

    /**
     * Set permanent key-value.
     */
    operator fun <T : Serializable> set(key: String, value: T)

    fun touch(key: String, ttl: Int? = null)
    override fun get(key: String): Any
    fun getAlive(key: String): Any?
    fun remove(key: String): Any?

    /**
     * Clean all pairs expired.
     *
     * @return Expired pairs.
     */
    fun cleanExpires(): Map<String, Any>

    fun getAlive(): Map<String, Any>

    fun toMap(): Map<String, CacheStore>
}
