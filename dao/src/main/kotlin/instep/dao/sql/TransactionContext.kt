package instep.dao.sql

import instep.Instep
import instep.servicecontainer.ServiceNotFoundException
import javax.sql.DataSource
import instep.dao.sql.ConnectionProvider as IConnectionProvider
import java.sql.Connection as JdbcConnection

class TransactionContext(val conn: JdbcConnection) {
    class AbortException : Exception()

    class ConnectionProvider(val ds: DataSource, override val dialect: Dialect) : IConnectionProvider {
        class Connection(private val conn: JdbcConnection) : JdbcConnection by conn {
            override fun rollback() {
                TransactionContext.threadLocalTransactionContext.get()?.run {
                    throw TransactionContext.AbortException()
                }

                conn.rollback()
            }

            override fun commit() {
                TransactionContext.threadLocalTransactionContext.get()?.let {
                    return@commit
                }

                conn.commit()
            }

            override fun close() {
                TransactionContext.threadLocalTransactionContext.get()?.run {
                    return@close
                }

                conn.close()
            }
        }

        constructor(ds: DataSource) : this(ds, Instep.make(Dialect::class.java))

        override fun getConnection(): JdbcConnection {
            TransactionContext.threadLocalTransactionContext.get()?.let {
                return@getConnection it.conn
            }

            return Connection(ds.connection)
        }
    }

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
        var transactionLevel = JdbcConnection.TRANSACTION_READ_COMMITTED

        fun <R : Any?> scope(runner: TransactionContext.() -> R): R {
            val transactionScope = Instep.make(TransactionScope::class.java)
            return transactionScope.template(transactionLevel, runner)
        }

        fun <R : Any?> scope(level: Int, runner: TransactionContext.() -> R): R {
            val transactionScope = Instep.make(TransactionScope::class.java)
            return transactionScope.template(level, runner)
        }

        override fun <R : Any?> template(level: Int, runner: TransactionContext.() -> R): R {
            var transactionContext = threadLocalTransactionContext.get()
            if (null == transactionContext) {
                val connMan = Instep.make(IConnectionProvider::class.java)
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
