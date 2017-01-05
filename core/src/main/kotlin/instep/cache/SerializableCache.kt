package instep.cache

import java.io.Serializable

/**
 * Cache with ttl(time to live, in millisecond) to every key-value pair.
 */
interface SerializableCache : Cache, Serializable {
    fun <T : Serializable> put(key: String, value: T, ttl: Int)

    /**
     * Set permanent key-value.
     */
    operator fun <T : Serializable> set(key: String, value: T)

    override fun get(key: String): Serializable
    override fun getAlive(key: String): Serializable?
    override fun remove(key: String): Serializable?

    /**
     * Clean all pairs expired.
     *
     * @return Expired pairs.
     */
    override fun cleanExpires(): Map<String, Serializable>

    override fun getAlive(): Map<String, Serializable>
}
