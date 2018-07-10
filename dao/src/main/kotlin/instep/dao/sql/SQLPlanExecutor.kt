package instep.dao.sql

import instep.dao.Plan
import instep.dao.PlanExecutor
import java.sql.Connection
import java.sql.ResultSet

interface SQLPlanExecutor : PlanExecutor {
    @Throws(SQLPlanExecutionException::class)
    override fun execute(plan: Plan<*>)

    @Throws(SQLPlanExecutionException::class)
    override fun executeScalar(plan: Plan<*>): String

    @Throws(SQLPlanExecutionException::class)
    override fun <T : Any> executeScalar(plan: Plan<*>, cls: Class<T>): T?

    @Throws(SQLPlanExecutionException::class)
    override fun <T : Any> execute(plan: Plan<*>, cls: Class<T>): List<T>

    @Throws(SQLPlanExecutionException::class)
    fun executeUpdate(plan: Plan<*>): Long

    @Throws(SQLPlanExecutionException::class)
    fun executeResultSet(conn: Connection, plan: Plan<*>): ResultSet
}