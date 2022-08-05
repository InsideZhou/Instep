package instep.dao.sql.impl

import instep.InstepLogger
import instep.dao.sql.*
import javax.sql.DataSource
import java.sql.Connection as JdbcConnection

class DefaultConnectionProvider(private val ds: DataSource, override val dialect: Dialect) : ConnectionProvider {
    companion object {
        private val transactionContextThreadLocal = ThreadLocal<DefaultTransactionContext>()
    }

    override val transactionRunner: TransactionRunner

    init {
        transactionRunner = DefaultTransactionRunner(this)
    }

    override fun getConnection(): JdbcConnection {
        transactionContextThreadLocal.get()?.let {
            return@getConnection it.connection
        }

        return Connection(ds.connection)
    }

    private class Connection(private val conn: JdbcConnection) : JdbcConnection by conn {
        override fun rollback() {
            transactionContextThreadLocal.get()?.run {
                abort()
            }

            conn.rollback()
        }

        override fun commit() {
            transactionContextThreadLocal.get()?.run {
                return@commit
            }

            conn.commit()
        }

        override fun close() {
            transactionContextThreadLocal.get()?.run {
                return@close
            }

            conn.close()
        }
    }

    private class DefaultTransactionRunner(private val connectionProvider: ConnectionProvider) : TransactionRunner {
        private val logger = InstepLogger.getLogger(DefaultTransactionRunner::class.java)

        override fun <R> with(level: Int?, action: TransactionContext.() -> R): R {
            var transactionContext = transactionContextThreadLocal.get()
            if (null == transactionContext) {
                val conn = connectionProvider.getConnection()

                if (null != level) {
                    conn.transactionIsolation = level
                }

                conn.autoCommit = false
                transactionContext = DefaultTransactionContext(conn)
            }
            else {
                if (null != level && level < transactionContext.connection.transactionIsolation) {
                    logger.message("nested transaction isolation level is lesser than outer.")
                        .context("nested", level)
                        .context("outer", transactionContext.connection.transactionIsolation)
                        .warn()
                }

                transactionContext.depth += 1
            }

            transactionContextThreadLocal.set(transactionContext)
            val conn = transactionContext.connection
            val sp = conn.setSavepoint()
            var rolledback = true

            try {
                val result = action(transactionContext)
                conn.releaseSavepoint(sp)
                rolledback = false
                return result
            } catch (e: TransactionAbortException) {
                conn.rollback(sp)

                if (null == e.cause) {
                    @Suppress("UNCHECKED_CAST")
                    return null as R
                }
                else {
                    throw e
                }
            } catch (e: Exception) {
                conn.rollback(sp)
                throw TransactionAbortException(e)
            }
            finally {
                if (transactionContext.depth > 0) {
                    transactionContext.depth -= 1
                }
                else {
                    transactionContextThreadLocal.set(null)

                    if (!rolledback) {
                        conn.commit()
                    }

                    conn.close()
                }
            }
        }
    }
}