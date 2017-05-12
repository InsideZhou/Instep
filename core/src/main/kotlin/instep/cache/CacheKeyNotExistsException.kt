package instep.cache

class CacheKeyNotExistsException(val key: String) : Exception("Cache $key not exists.") {
}