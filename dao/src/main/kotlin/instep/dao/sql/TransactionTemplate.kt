package instep.dao.sql

import instep.Instep
import instep.InstepLogger
import instep.servicecontainer.ServiceNotFoundException
import javax.sql.DataSource
import instep.dao.sql.ConnectionProvider as IConnectionProvider
import java.sql.Connection as JdbcConnection

@Suppress("unused")
object TransactionTemplate {
    fun <R> run(runner: TransactionContext.() -> R): R {
        return template(null, runner)
    }

    fun <R> uncommitted(runner: TransactionContext.() -> R): R {
        return template(JdbcConnection.TRANSACTION_READ_UNCOMMITTED, runner)
    }

    fun <R> committed(runner: TransactionContext.() -> R): R {
        return template(JdbcConnection.TRANSACTION_READ_COMMITTED, runner)
    }

    fun <R> repeatable(runner: TransactionContext.() -> R): R {
        return template(JdbcConnection.TRANSACTION_REPEATABLE_READ, runner)
    }

    fun <R> serializable(runner: TransactionContext.() -> R): R {
        return template(JdbcConnection.TRANSACTION_SERIALIZABLE, runner)
    }

    val threadLocalTransactionContext = object : ThreadLocal<TransactionContext>() {}

    @Suppress("unchecked_cast")
    fun <R> template(level: Int?, runner: TransactionContext.() -> R): R {
        var transactionContext = threadLocalTransactionContext.get()
        if (null == transactionContext) {
            val connProvider = Instep.make(IConnectionProvider::class.java)
            val conn = connProvider.getConnection()

            if (null != level) {
                conn.transactionIsolation = level
            }

            conn.autoCommit = false
            transactionContext = TransactionContext(conn)
        }
        else {
            if (null != level && level < transactionContext.conn.transactionIsolation) {
                InstepLogger.warning({ "nested transaction isolation $level is lesser then outer ${transactionContext.conn.transactionIsolation}" }, this.javaClass)
            }

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
        catch (e: TransactionContext.AbortException) {
            conn.rollback(sp)
            return null as R
        }
        catch (e: Exception) {
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

class TransactionContext(val conn: JdbcConnection) {
    var depth = 0

    fun abort() {
        throw AbortException()
    }

    class AbortException : Exception()

    class ConnectionProvider(val ds: DataSource, override val dialect: Dialect) : IConnectionProvider {
        init {
            try {
                Instep.make(Dialect::class.java)
            }
            catch (e: ServiceNotFoundException) {
                Instep.bind(Dialect::class.java, dialect)
            }
        }

        override fun getConnection(): JdbcConnection {
            TransactionTemplate.threadLocalTransactionContext.get()?.let {
                return@getConnection it.conn
            }

            return Connection(ds.connection)
        }

        class Connection(private val conn: JdbcConnection) : JdbcConnection by conn {
            override fun rollback() {
                TransactionTemplate.threadLocalTransactionContext.get()?.run {
                    abort()
                }

                conn.rollback()
            }

            override fun commit() {
                TransactionTemplate.threadLocalTransactionContext.get()?.let {
                    return@commit
                }

                conn.commit()
            }

            override fun close() {
                TransactionTemplate.threadLocalTransactionContext.get()?.run {
                    return@close
                }

                conn.close()
            }
        }
    }
}
