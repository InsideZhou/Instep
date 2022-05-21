package instep.dao.sql.impl

import instep.Instep
import instep.dao.sql.ConnectionProvider
import instep.dao.sql.Dialect
import javax.sql.DataSource
import java.sql.Connection as JdbcConnection

class DefaultConnectionProvider(val ds: DataSource, override val dialect: Dialect) : ConnectionProvider {
    init {
        Instep.bind(Dialect::class.java, dialect)
    }

    override fun getConnection(): JdbcConnection {
        Package.threadLocalTransactionContext.get()?.let {
            return@getConnection it.conn
        }

        return Connection(ds.connection)
    }

    class Connection(private val conn: JdbcConnection) : JdbcConnection by conn {
        override fun rollback() {
            Package.threadLocalTransactionContext.get()?.run {
                abort()
            }

            conn.rollback()
        }

        override fun commit() {
            Package.threadLocalTransactionContext.get()?.let {
                return@commit
            }

            conn.commit()
        }

        override fun close() {
            Package.threadLocalTransactionContext.get()?.run {
                return@close
            }

            conn.close()
        }
    }
}