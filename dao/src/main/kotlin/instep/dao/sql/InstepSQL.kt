package instep.dao.sql

import instep.Instep
import instep.collection.AssocArray
import instep.dao.Expression
import instep.dao.ExpressionFactory
import instep.dao.sql.impl.*
import instep.servicecontainer.ServiceNotFoundException
import instep.typeconversion.TypeConversion
import java.sql.ResultSet

@Suppress("unused")
object InstepSQL {
    fun plan(txt: String): SQLPlan<*> {
        val factory = Instep.make(SQLPlanFactory::class.java)
        return factory.createInstance(txt)
    }

    @Suppress("UNCHECKED_CAST")
    fun executor(): SQLPlanExecutor<SQLPlan<*>> {
        return Instep.make(SQLPlanExecutor::class.java) as SQLPlanExecutor<SQLPlan<*>>
    }

    fun transaction(): TransactionRunner {
        return Instep.make(TransactionRunner::class.java)
    }

    @Throws(TransactionAbortException::class)
    fun <R> transaction(action: TransactionContext.() -> R): R {
        val runner = Instep.make(TransactionRunner::class.java)
        return runner.run(null, action)
    }

    @Throws(TransactionAbortException::class)
    fun <R> transaction(action: TransactionContext.() -> Unit) {
        val runner = Instep.make(TransactionRunner::class.java)
        return runner.run(null, action)
    }

    fun expression(txt: String): Expression<*> {
        val factory = Instep.make(ExpressionFactory::class.java)
        return factory.createInstance(txt)
    }

    init {
        try {
            Instep.make(ResultSetColumnValueExtractor::class.java)
        } catch (e: ServiceNotFoundException) {
            Instep.bind(ResultSetColumnValueExtractor::class.java, DefaultResultSetColumnValueExtractor())
        }

        try {
            Instep.make(ResultSetDelegate::class.java)
        } catch (e: ServiceNotFoundException) {
            Instep.bind(ResultSetDelegate::class.java, DefaultResultSetDelegate())
        }

        try {
            Instep.make(ColumnInfoSetGenerator::class.java)
        } catch (e: ServiceNotFoundException) {
            Instep.bind(ColumnInfoSetGenerator::class.java, DefaultColumnInfoSetGenerator())
        }

        try {
            Instep.make(PreparedStatementGenerator::class.java)
        } catch (e: ServiceNotFoundException) {
            Instep.bind(PreparedStatementGenerator::class.java, DefaultPreparedStatementGenerator())
        }

        try {
            Instep.make(SQLPlanFactory::class.java)
        } catch (e: ServiceNotFoundException) {
            Instep.bind(SQLPlanFactory::class.java, SQLPlanFactory.Companion)
        }

        try {
            Instep.make(SQLPlanExecutor::class.java)
        } catch (e: ServiceNotFoundException) {
            Instep.bind(SQLPlanExecutor::class.java, DefaultSQLPlanExecutor())
        }

        try {
            Instep.make(ExpressionFactory::class.java)
        } catch (e: ServiceNotFoundException) {
            Instep.bind(ExpressionFactory::class.java, ExpressionFactory.Companion)
        }

        try {
            Instep.make(SQLPlanExecutor::class.java)
        } catch (e: ServiceNotFoundException) {
            Instep.bind(SQLPlanExecutor::class.java, DefaultSQLPlanExecutor())
        }

        try {
            Instep.make(TableSelectPlanFactory::class.java)
        } catch (e: ServiceNotFoundException) {
            Instep.bind(TableSelectPlanFactory::class.java, TableSelectPlanFactory.Companion)
        }

        try {
            Instep.make(TableInsertPlanFactory::class.java)
        } catch (e: ServiceNotFoundException) {
            Instep.bind(TableInsertPlanFactory::class.java, TableInsertPlanFactory.Companion)
        }

        try {
            Instep.make(TableUpdatePlanFactory::class.java)
        } catch (e: ServiceNotFoundException) {
            Instep.bind(TableUpdatePlanFactory::class.java, TableUpdatePlanFactory.Companion)
        }

        try {
            Instep.make(TableDeletePlanFactory::class.java)
        } catch (e: ServiceNotFoundException) {
            Instep.bind(TableDeletePlanFactory::class.java, TableDeletePlanFactory.Companion)
        }

        runCatching {
            Instep.make(ConnectionProvider::class.java)
        }.onSuccess {
            Instep.bind(TransactionRunner::class.java, it.transactionRunner)
        }

        runCatching {
            Instep.make(TypeConversion::class.java)
        }.onSuccess {
            it.getConverter(ResultSet::class.java, AssocArray::class.java) ?: it.register(ResultSetToAssocArrayConverter())
        }
    }
}
