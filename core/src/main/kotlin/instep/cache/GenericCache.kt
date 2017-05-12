package instep.cache


/**
 * Cache(key-value pair) with ttl(time to live, in millisecond).
 * Subclass should assert cache key is valid before write it to underlying layer.
 */
interface GenericCache<T> : Map<String, T> {
    /**
     * Put cache with ttl, replace existing one.
     *
     * @param ttl if < 0, cache live permanently.
     */
    fun put(key: String, value: T, ttl: Int)

    /**
     * Set permanent cache. if there is existing one, value will only be set.
     */
    operator fun set(key: String, value: T)

    /**
     * Get a alive cache.
     */
    override fun get(key: String): T?

    /**
     * Get all alive caches.
     */
    fun getAlive(): Map<String, T>

    /**
     * Get all expired caches.
     */
    fun getExpired(): Map<String, T>

    /**
     * Touch a cache, refresh its createdTime and/or set new ttl time.
     *
     * @param ttl if null, won't touch ttl of cache.
     * @throws CacheKeyNotExistsException
     */
    @Throws(CacheKeyNotExistsException::class)
    fun touch(key: String, ttl: Int? = null)

    /**
     * Remove cache.
     *
     * @return null if key does not exist.
     */
    fun remove(key: String): T?

    /**
     * Clear all expired caches.
     *
     * @return Expired caches.
     */
    fun clearExpired(): Map<String, T>

    companion object {
        val invalidPatterns = listOf(Regex("""\W+"""))

        fun assertKeyIsValid(key: String) {
            if (invalidPatterns.any { it.matches(key) }) throw InvalidCacheKeyException(key)
        }

        fun isKeyValid(key: String) {
            invalidPatterns.all { !it.matches(key) }
        }

        /**
         * Replace all invalid strings in key with '_'.
         */
        fun normalizeKey(key: String): String {
            var result = key

            invalidPatterns.forEach { result = result.replace(it, "_") }

            return result
        }
    }
}