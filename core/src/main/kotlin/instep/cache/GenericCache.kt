package instep.cache

/**
 * Cache(key-value pair) with ttl(time to live, in millisecond).
 */
interface GenericCache<T> : Map<String, T> {
    fun put(key: String, value: T, ttl: Int)

    /**
     * Set permanent cache.
     */
    operator fun set(key: String, value: T)

    /**
     * Touch a cache, refresh its createdTime and/or set new ttl time.
     */
    fun touch(key: String, ttl: Int? = null)

    override fun get(key: String): T

    /**
     * Get a alive cache.
     */
    fun getAlive(key: String): T?

    /**
     * Remove cache.
     * @return null if key does not exist.
     */
    fun remove(key: String): T?

    /**
     * Clear all expired caches.
     *
     * @return Expired caches.
     */
    fun clearExpired(): Map<String, T>

    /**
     * Get all alive caches.
     */
    fun getAllAlive(): Map<String, T>
}