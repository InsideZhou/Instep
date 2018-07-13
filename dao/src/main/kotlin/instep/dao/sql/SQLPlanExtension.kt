@file:Suppress("unused")

package instep.dao.sql

import instep.Instep
import java.sql.Connection
import java.sql.ResultSet

@Suppress("UNCHECKED_CAST")
val planExecutor = Instep.make(SQLPlanExecutor::class.java) as SQLPlanExecutor<SQLPlan<*>>

/**
 * @see [SQLPlanExecutor.execute]
 */
@Throws(SQLPlanExecutionException::class)
fun SQLPlan<*>.execute() {
    planExecutor.execute(this)
}

/**
 * @see [SQLPlanExecutor.execute]
 */
@Throws(SQLPlanExecutionException::class)
fun <T : Any> SQLPlan<*>.execute(cls: Class<T>): List<T> {
    return planExecutor.execute(this, cls)
}

/**
 * @see [SQLPlanExecutor.executeScalar]
 */
@Throws(SQLPlanExecutionException::class)
fun SQLPlan<*>.executeScalar(): String {
    return planExecutor.executeScalar(this)
}

/**
 * @see [SQLPlanExecutor.executeScalar]
 */
@Throws(SQLPlanExecutionException::class)
fun <T : Any> SQLPlan<*>.executeScalar(cls: Class<T>): T? {
    return planExecutor.executeScalar(this, cls)
}

/**
 * @see [SQLPlanExecutor.executeUpdate]
 */
@Throws(SQLPlanExecutionException::class)
fun SQLPlan<*>.executeUpdate(): Long {
    return planExecutor.executeUpdate(this)
}

/**
 * @see [SQLPlanExecutor.executeResultSet]
 */
@Throws(SQLPlanExecutionException::class)
fun SQLPlan<*>.executeResultSet(conn: Connection): ResultSet {
    return planExecutor.executeResultSet(conn, this)
}

/**
 * @see [SQLPlanExecutor.execute]
 */
@Throws(SQLPlanExecutionException::class)
fun TableSelectPlan.execute(): List<TableRow> {
    return planExecutor.execute(this, TableRow::class.java)
}

/**
 * @see [SQLPlanExecutor.execute]
 */
@Throws(SQLPlanExecutionException::class)
fun <T : Any> TableSelectPlan.execute(cls: Class<T>): List<T> {
    return planExecutor.execute(this, cls)
}
