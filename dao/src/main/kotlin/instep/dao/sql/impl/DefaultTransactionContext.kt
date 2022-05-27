package instep.dao.sql.impl

import instep.cache.driver.MemoryCache
import instep.dao.sql.TransactionContext
import java.sql.Connection


class DefaultTransactionContext(val connection: Connection) : TransactionContext {
    var depth = 0
    private val kvStore: MemoryCache<Any?> = MemoryCache()

    override fun get(key: String): Any? {
        return kvStore[key]
    }

    override fun set(key: String, obj: Any?) {
        kvStore[key] = obj
    }
}