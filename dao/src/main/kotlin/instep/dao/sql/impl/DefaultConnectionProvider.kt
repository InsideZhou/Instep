package instep.dao.sql.impl

import instep.Instep
import instep.dao.sql.ConnectionProvider
import instep.dao.sql.Dialect
import instep.dao.sql.TransactionContext
import java.sql.Connection
import javax.sql.DataSource

open class DefaultConnectionProvider(val ds: DataSource, override val dialect: Dialect) : ConnectionProvider {
    constructor(ds: DataSource) : this(ds, Instep.make(Dialect::class.java))

    open class ConnectionWithNestedTransaction(private val conn: Connection) : Connection by conn {
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

    override fun getConnection(): Connection {
        TransactionContext.threadLocalTransactionContext.get()?.let {
            return@getConnection it.conn
        }

        return ConnectionWithNestedTransaction(ds.connection)
    }
}
