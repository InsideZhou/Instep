package instep.dao.sql

import java.sql.Connection


interface TransactionContext {
    operator fun get(key: String): Any?
    operator fun set(key: String, obj: Any?)

    val conn: Connection

    fun abort() {
        throw TransactionAbortException(null)
    }

    @Suppress("unused")
    fun abort(cause: Exception) {
        throw TransactionAbortException(cause)
    }
}

class TransactionAbortException(cause: Exception?) : Exception(cause)
