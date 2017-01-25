package instep.orm.sql.impl

import instep.orm.sql.ConnectionManager
import java.sql.Connection
import java.sql.Savepoint
import javax.sql.DataSource

open class DefaultConnectionManager(val ds: DataSource) : ConnectionManager {
    override fun getConnection(): Connection {
        val connSet = threadLocalConnectionSet.get()
        val connInTransaction = connSet.lastOrNull()
        if (null != connInTransaction) {
            return connInTransaction
        }

        return ConnectionWithTransactionAcrossMethod(ds.connection, connSet)
    }

    companion object {
        private val threadLocalConnectionSet = object : ThreadLocal<MutableSet<ConnectionWithTransactionAcrossMethod>>() {
            override fun initialValue(): MutableSet<ConnectionWithTransactionAcrossMethod> {
                return mutableSetOf()
            }
        }
    }
}

class ConnectionWithTransactionAcrossMethod(
    private val conn: Connection,
    private val pool: MutableSet<ConnectionWithTransactionAcrossMethod>
) : Connection by conn {
    var depth = 0

    override fun setSavepoint(): Savepoint? {
        increaseConnectionTransactionDepth()

        return conn.setSavepoint()
    }

    override fun setSavepoint(name: String?): Savepoint? {
        increaseConnectionTransactionDepth()

        return conn.setSavepoint(name)
    }

    override fun releaseSavepoint(savepoint: Savepoint?) {
        conn.releaseSavepoint(savepoint)

        decreaseConnectionTransactionDepth()
    }

    override fun rollback(savepoint: Savepoint?) {
        conn.rollback(savepoint)

        decreaseConnectionTransactionDepth()
    }

    override fun rollback() {
        if (depth <= 1) {
            conn.rollback()
        }

        decreaseConnectionTransactionDepth()
    }

    override fun commit() {
        if (depth <= 1) {
            conn.commit()
        }

        decreaseConnectionTransactionDepth()
    }

    override fun close() {
        if (depth <= 1) {
            conn.close()
        }
    }

    private fun increaseConnectionTransactionDepth() {
        depth += 1

        if (!pool.contains(this)) {
            pool.add(this)
        }
    }

    private fun decreaseConnectionTransactionDepth() {
        if (!pool.contains(this)) return

        depth -= 1
        if (0 == depth) {
            pool.remove(this)
        }
    }
}
