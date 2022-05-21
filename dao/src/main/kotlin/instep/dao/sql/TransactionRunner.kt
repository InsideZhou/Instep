package instep.dao.sql

import java.sql.Connection

interface TransactionRunner {
    @Throws(TransactionAbortException::class)
    fun <R> run(level: Int?, action: TransactionContext.() -> R): R

    @Throws(TransactionAbortException::class)
    fun <R> run(action: TransactionContext.() -> R): R {
        return run(null, action)
    }

    @Throws(TransactionAbortException::class)
    fun <R> uncommitted(action: TransactionContext.() -> R): R {
        return run(Connection.TRANSACTION_READ_UNCOMMITTED, action)
    }

    @Throws(TransactionAbortException::class)
    fun <R> committed(action: TransactionContext.() -> R): R {
        return run(Connection.TRANSACTION_READ_COMMITTED, action)
    }

    @Throws(TransactionAbortException::class)
    fun <R> repeatable(action: TransactionContext.() -> R): R {
        return run(Connection.TRANSACTION_REPEATABLE_READ, action)
    }

    @Throws(TransactionAbortException::class)
    fun <R> serializable(action: TransactionContext.() -> R): R {
        return run(Connection.TRANSACTION_SERIALIZABLE, action)
    }
}