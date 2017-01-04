package instep.cache

class CacheKeyNotExistsException(val key: String) : RuntimeException("Cache $key not exists.") {
}