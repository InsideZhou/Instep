package instep.dao.sql

import instep.dao.PlanExecutor
import java.sql.Connection
import java.sql.ResultSet

interface SQLPlanExecutor<S : SQLPlan<*>> : PlanExecutor<S> {
    @Throws(SQLPlanExecutionException::class)
    override fun execute(plan: S)

    @Throws(SQLPlanExecutionException::class)
    override fun executeScalar(plan: S): String

    @Throws(SQLPlanExecutionException::class)
    override fun <T : Any> executeScalar(plan: S, cls: Class<T>): T?

    @Throws(SQLPlanExecutionException::class)
    override fun <T : Any> execute(plan: S, cls: Class<T>): List<T>

    @Throws(SQLPlanExecutionException::class)
    fun executeUpdate(plan: S): Int

    @Throws(SQLPlanExecutionException::class)
    fun executeResultSet(conn: Connection, plan: S): ResultSet

    @Throws(SQLPlanExecutionException::class)
    fun executeDataRow(plan: S): List<DataRow> {
        return execute(plan, DataRow::class.java)
    }
}