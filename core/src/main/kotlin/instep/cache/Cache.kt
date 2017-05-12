package instep.cache

/**
 * Common Cache.
 */
interface Cache : GenericCache<Any> {
    override fun put(key: String, value: Any, ttl: Int)
    override operator fun set(key: String, value: Any)
    override fun touch(key: String, ttl: Int?)
    override fun get(key: String): Any
    override fun getAlive(key: String): Any?
    override fun remove(key: String): Any?
    override fun clearExpired(): Map<String, Any>
    override fun getAllAlive(): Map<String, Any>
}
