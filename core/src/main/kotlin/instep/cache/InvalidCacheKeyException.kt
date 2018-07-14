package instep.cache

/**
 * @see Cache.normalizeKey
 */
class InvalidCacheKeyException(val key: String) : RuntimeException("Cache $key is invalid.") {
}
