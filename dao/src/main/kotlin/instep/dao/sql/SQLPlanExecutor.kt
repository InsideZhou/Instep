package instep.dao.sql

import instep.dao.PlanExecutor
import java.sql.Connection
import java.sql.ResultSet
import java.time.temporal.Temporal

interface SQLPlanExecutor<S : SQLPlan<*>> : PlanExecutor<S> {
    @Throws(SQLPlanExecutionException::class)
    override fun execute(plan: S)

    @Throws(SQLPlanExecutionException::class)
    override fun executeString(plan: S): String

    @Throws(SQLPlanExecutionException::class)
    override fun executeLong(plan: S): Long

    @Throws(SQLPlanExecutionException::class)
    override fun executeDouble(plan: S): Double

    @Throws(SQLPlanExecutionException::class)
    override fun <R : Temporal> executeTemporal(plan: S, cls: Class<R>): R?

    @Throws(SQLPlanExecutionException::class)
    override fun <T : Any> execute(plan: S, cls: Class<T>): List<T>

    @Throws(SQLPlanExecutionException::class)
    fun executeUpdate(plan: S): Int

    @Throws(SQLPlanExecutionException::class)
    fun executeResultSet(conn: Connection, plan: S): ResultSet
}