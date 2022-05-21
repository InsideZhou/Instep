package instep.dao.sql.impl

import instep.Instep
import instep.InstepLogger
import instep.cache.driver.MemoryCache
import instep.dao.sql.ConnectionProvider
import instep.dao.sql.TransactionAbortException
import instep.dao.sql.TransactionContext
import instep.dao.sql.TransactionRunner
import java.sql.Connection

open class DefaultTransactionRunner : TransactionRunner {
    private val logger = InstepLogger.getLogger(DefaultTransactionRunner::class.java)

    override fun <R> run(level: Int?, action: TransactionContext.() -> R): R {
        var transactionContext = Package.threadLocalTransactionContext.get()
        if (null == transactionContext) {
            val connProvider = Instep.make(ConnectionProvider::class.java)
            val conn = connProvider.getConnection()

            if (null != level) {
                conn.transactionIsolation = level
            }

            conn.autoCommit = false
            transactionContext = DefaultTransactionContext(conn)
        }
        else {
            if (null != level && level < transactionContext.conn.transactionIsolation) {
                logger.message("nested transaction isolation level is lesser than outer.")
                    .context("nested", level)
                    .context("outer", transactionContext.conn.transactionIsolation)
                    .warn()
            }

            transactionContext.depth += 1
        }

        Package.threadLocalTransactionContext.set(transactionContext)
        val conn = transactionContext.conn
        val sp = conn.setSavepoint()

        try {
            val result = action(transactionContext)

            if (transactionContext.depth > 0) {
                conn.releaseSavepoint(sp)
            }
            else {
                Package.threadLocalTransactionContext.set(null)
                conn.commit()
            }

            return result
        } catch (e: Exception) {
            conn.rollback(sp)

            if (e is TransactionAbortException) {
                if (null == e.cause) {
                    @Suppress("UNCHECKED_CAST")
                    return null as R
                }
                else {
                    throw e
                }
            }
            else {
                throw TransactionAbortException(e)
            }
        }
        finally {
            if (transactionContext.depth > 0) {
                transactionContext.depth -= 1
            }
            else {
                Package.threadLocalTransactionContext.set(null)
                conn.close()
            }
        }
    }
}

class DefaultTransactionContext(override val conn: Connection) : TransactionContext {
    var depth = 0
    private val kvStore: MemoryCache<Any?> = MemoryCache()

    override fun get(key: String): Any? {
        return kvStore[key]
    }

    override fun set(key: String, obj: Any?) {
        kvStore[key] = obj
    }
}