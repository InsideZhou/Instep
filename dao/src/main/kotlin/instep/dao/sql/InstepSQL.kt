package instep.dao.sql

import instep.Instep
import instep.collection.AssocArray
import instep.dao.sql.impl.DefaultColumnInfoSetGenerator
import instep.dao.sql.impl.DefaultPreparedStatementGenerator
import instep.dao.sql.impl.DefaultResultSetDelegate
import instep.dao.sql.impl.DefaultSQLPlanExecutor
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
        return Instep.make(ConnectionProvider::class.java).transactionRunner
    }

    fun <R> withTransaction(action: TransactionContext.() -> R): R {
        val runner = Instep.make(ConnectionProvider::class.java).transactionRunner
        return runner.with(null, action)
    }

    fun transaction(action: TransactionContext.() -> Unit) {
        val runner = Instep.make(ConnectionProvider::class.java).transactionRunner
        return runner.run(null, action)
    }

    init {
        try {
            Instep.make(ResultSetDelegate::class.java)
        }
        catch (e: ServiceNotFoundException) {
            Instep.bind(ResultSetDelegate::class.java, DefaultResultSetDelegate())
        }

        try {
            Instep.make(ColumnInfoSetGenerator::class.java)
        }
        catch (e: ServiceNotFoundException) {
            Instep.bind(ColumnInfoSetGenerator::class.java, DefaultColumnInfoSetGenerator())
        }

        try {
            Instep.make(PreparedStatementGenerator::class.java)
        }
        catch (e: ServiceNotFoundException) {
            Instep.bind(PreparedStatementGenerator::class.java, DefaultPreparedStatementGenerator())
        }

        try {
            Instep.make(SQLPlanFactory::class.java)
        }
        catch (e: ServiceNotFoundException) {
            Instep.bind(SQLPlanFactory::class.java, SQLPlanFactory.Companion)
        }

        try {
            Instep.make(SQLPlanExecutor::class.java)
        }
        catch (e: ServiceNotFoundException) {
            Instep.bind(SQLPlanExecutor::class.java, DefaultSQLPlanExecutor())
        }

        try {
            Instep.make(SQLPlanExecutor::class.java)
        }
        catch (e: ServiceNotFoundException) {
            Instep.bind(SQLPlanExecutor::class.java, DefaultSQLPlanExecutor())
        }

        try {
            Instep.make(TableSelectPlanFactory::class.java)
        }
        catch (e: ServiceNotFoundException) {
            Instep.bind(TableSelectPlanFactory::class.java, TableSelectPlanFactory.Companion)
        }

        try {
            Instep.make(TableInsertPlanFactory::class.java)
        }
        catch (e: ServiceNotFoundException) {
            Instep.bind(TableInsertPlanFactory::class.java, TableInsertPlanFactory.Companion)
        }

        try {
            Instep.make(TableUpdatePlanFactory::class.java)
        }
        catch (e: ServiceNotFoundException) {
            Instep.bind(TableUpdatePlanFactory::class.java, TableUpdatePlanFactory.Companion)
        }

        try {
            Instep.make(TableDeletePlanFactory::class.java)
        }
        catch (e: ServiceNotFoundException) {
            Instep.bind(TableDeletePlanFactory::class.java, TableDeletePlanFactory.Companion)
        }

        runCatching {
            Instep.make(TypeConversion::class.java)
        }.onSuccess {
            it.getConverter(ResultSet::class.java, AssocArray::class.java) ?: it.register(ResultSetToDataRowConverter())
        }
    }
}
