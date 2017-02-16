package instep.dao.sql

import instep.Instep
import instep.servicecontainer.ServiceNotFoundException
import java.sql.Connection

class TransactionContext(val conn: Connection) {
    class AbortException : Exception()

    var depth = 0

    fun abort() {
        throw AbortException()
    }

    companion object : TransactionScope {
        init {
            try {
                Instep.make(TransactionScope::class.java)
            }
            catch(e: ServiceNotFoundException) {
                Instep.bind(TransactionScope::class.java, TransactionContext.Companion)
            }
        }

        val threadLocalTransactionContext = object : ThreadLocal<TransactionContext>() {}

        override fun <R : Any?> template(level: Int, runner: TransactionContext.() -> R): R {
            var transactionContext = threadLocalTransactionContext.get()
            if (null == transactionContext) {
                val connMan = Instep.make(ConnectionProvider::class.java)
                val conn = connMan.getConnection()
                conn.transactionIsolation = level
                conn.autoCommit = false
                transactionContext = TransactionContext(conn)
            }
            else {
                transactionContext.depth += 1
            }

            threadLocalTransactionContext.set(transactionContext)
            val conn = transactionContext.conn
            val sp = conn.setSavepoint()

            try {
                val result = runner(transactionContext)

                if (transactionContext.depth > 0) {
                    conn.releaseSavepoint(sp)
                }
                else {
                    threadLocalTransactionContext.set(null)
                    conn.commit()
                }

                return result
            }
            catch(e: AbortException) {
                conn.rollback(sp)
                return null as R
            }
            catch(e: Exception) {
                conn.rollback(sp)
                throw e
            }
            finally {
                if (transactionContext.depth > 0) {
                    transactionContext.depth -= 1
                }
                else {
                    threadLocalTransactionContext.set(null)
                    conn.close()
                }
            }
        }
    }
}

interface TransactionScope {
    fun <R : Any?> template(level: Int, runner: TransactionContext.() -> R): R
}
