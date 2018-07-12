package instep.dao.sql

import instep.Instep
import instep.dao.ExpressionFactory
import instep.dao.sql.impl.*
import instep.servicecontainer.ServiceNotFoundException

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

    fun transaction(): TransactionTemplate {
        return TransactionTemplate
    }

    fun <R> transaction(level: Int, runner: TransactionContext.() -> R): R {
        return TransactionTemplate.template(level, runner)
    }

    init {
        try {
            Instep.make(ResultSetValueExtractor::class.java)
        }
        catch (e: ServiceNotFoundException) {
            Instep.bind(ResultSetValueExtractor::class.java, DefaultResultSetValueExtractor())
        }

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
            Instep.make(TableRowFactory::class.java)
        }
        catch (e: ServiceNotFoundException) {
            Instep.bind(TableRowFactory::class.java, TableRowFactory.Companion)
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
            Instep.bind(SQLPlanExecutor::class.java, DefaultSQLPlanExecutor<SQLPlan<*>>())
        }

        try {
            Instep.make(ExpressionFactory::class.java)
        }
        catch (e: ServiceNotFoundException) {
            Instep.bind(ExpressionFactory::class.java, ExpressionFactory.Companion)
        }

        try {
            Instep.make(SQLPlanExecutor::class.java)
        }
        catch (e: ServiceNotFoundException) {
            Instep.bind(SQLPlanExecutor::class.java, DefaultSQLPlanExecutor<SQLPlan<*>>())
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
    }
}
