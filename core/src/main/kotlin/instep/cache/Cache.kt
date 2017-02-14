package instep.cache

/**
 * Cache with ttl(time to live, in millisecond) to every key-value pair.
 */
interface Cache : Map<String, Any> {
    fun put(key: String, value: Any, ttl: Int)

    /**
     * Set permanent key-value.
     */
    operator fun set(key: String, value: Any)

    fun touch(key: String, ttl: Int? = null)
    override fun get(key: String): Any
    fun getAlive(key: String): Any?

    /**
     * Remove key-value.
     * @return null if key does not exist.
     */
    fun remove(key: String): Any?

    /**
     * Clean all pairs expired.
     *
     * @return Expired pairs.
     */
    fun cleanExpires(): Map<String, Any>

    fun getAlive(): Map<String, Any>

    fun toMap(): Map<String, Any>
}
