package instep.dao.sql

import java.sql.Connection

interface TransactionRunner {
    fun <R> with(level: Int?, action: TransactionContext.() -> R): R

    fun run(level: Int?, action: TransactionContext.() -> Unit) {
        val function: TransactionContext.() -> Any = {
            action(this)
        }

        with(level, function)
    }

    fun <R> with(action: TransactionContext.() -> R): R {
        return with(null, action)
    }

    fun run(action: TransactionContext.() -> Unit) {
        return run(null, action)
    }

    fun <R> withUncommitted(action: TransactionContext.() -> R): R {
        return with(Connection.TRANSACTION_READ_UNCOMMITTED, action)
    }

    fun uncommitted(action: TransactionContext.() -> Unit) {
        return run(Connection.TRANSACTION_READ_UNCOMMITTED, action)
    }

    fun <R> withCommitted(action: TransactionContext.() -> R): R {
        return with(Connection.TRANSACTION_READ_COMMITTED, action)
    }

    fun committed(action: TransactionContext.() -> Unit) {
        return run(Connection.TRANSACTION_READ_COMMITTED, action)
    }

    fun <R> withRepeatable(action: TransactionContext.() -> R): R {
        return with(Connection.TRANSACTION_REPEATABLE_READ, action)
    }

    fun repeatable(action: TransactionContext.() -> Unit) {
        return run(Connection.TRANSACTION_REPEATABLE_READ, action)
    }

    fun <R> withSerializable(action: TransactionContext.() -> R): R {
        return with(Connection.TRANSACTION_SERIALIZABLE, action)
    }

    fun serializable(action: TransactionContext.() -> Unit) {
        return run(Connection.TRANSACTION_SERIALIZABLE, action)
    }
}