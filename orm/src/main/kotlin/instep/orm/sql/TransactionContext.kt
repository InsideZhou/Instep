package instep.orm.sql

import java.sql.Connection

class TransactionContext(val conn: Connection) {
    class AbortException : Exception()

    var depth = 0

    fun abort() {
        throw AbortException()
    }

    companion object {
        val threadLocalTransactionContext = object : ThreadLocal<TransactionContext>() {}
    }
}
