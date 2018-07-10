package instep.dao

import instep.Instep
import instep.InstepLogger
import instep.dao.sql.*
import instep.dao.sql.impl.*
import instep.servicecontainer.ServiceNotFoundException
import java.io.Serializable

/**
 * Plan that is targeting relational database manipulated by SQL.
 */
interface Plan<T : Plan<T>> : Serializable {
    val statement: String
    /**
     * Order of parameters need to be same as order of statement's placeholders.
     */
    val parameters: List<Any?>

    @Suppress("UNCHECKED_CAST")
    fun debug(): T {
        val self = this
        InstepLogger.debug({ statement }, self.javaClass.name)
        InstepLogger.debug({ parameterToLogFormat() }, self.javaClass.name)
        return self as T
    }

    @Suppress("UNCHECKED_CAST")
    fun info(): T {
        val self = this
        InstepLogger.info({ statement }, self.javaClass.name)
        InstepLogger.info({ parameterToLogFormat() }, self.javaClass.name)
        return self as T
    }

    @Suppress("UNCHECKED_CAST")
    fun log(runner: (T) -> Unit): T {
        runner(this as T)
        return this
    }

    fun parameterToLogFormat(): String = parameters.map(Any?::toString).joinToString("|")

    companion object {
        init {
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
                Instep.make(ExpressionFactory::class.java)
            }
            catch (e: ServiceNotFoundException) {
                Instep.bind(ExpressionFactory::class.java, ExpressionFactory.Companion)
            }

            try {
                Instep.make(SQLPlanExecutor::class.java)
            }
            catch (e: ServiceNotFoundException) {
                Instep.bind(SQLPlanExecutor::class.java, DefaultSQLPlanExecutor())
            }

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
}