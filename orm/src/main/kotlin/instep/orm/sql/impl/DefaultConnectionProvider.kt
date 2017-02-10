package instep.orm.sql.impl

import instep.orm.sql.ConnectionProvider
import instep.orm.sql.TransactionContext
import java.sql.Connection
import javax.sql.DataSource

open class DefaultConnectionProvider(val ds: DataSource) : ConnectionProvider {
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
