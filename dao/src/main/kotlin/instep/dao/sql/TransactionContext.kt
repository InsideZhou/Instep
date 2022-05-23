package instep.dao.sql


interface TransactionContext {
    operator fun get(key: String): Any?
    operator fun set(key: String, obj: Any?)

    fun abort() {
        throw TransactionAbortException(null)
    }

    @Suppress("unused")
    fun abort(cause: Exception) {
        throw TransactionAbortException(cause)
    }
}

class TransactionAbortException(cause: Exception?) : RuntimeException(cause)
