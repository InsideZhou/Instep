package instep.cache

/**
 * @see GenericCache.normalizeKey
 */
class InvalidCacheKeyException(val key: String) : RuntimeException("Cache $key is invalid.") {
}
