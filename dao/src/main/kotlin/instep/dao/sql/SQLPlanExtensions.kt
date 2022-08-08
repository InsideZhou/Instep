@file:Suppress("unused")

package instep.dao.sql

import instep.Instep
import java.sql.Connection
import java.sql.ResultSet
import java.time.temporal.Temporal

@Suppress("UNCHECKED_CAST")
val planExecutor: SQLPlanExecutor<SQLPlan<*>>
    get() = Instep.make(SQLPlanExecutor::class.java) as SQLPlanExecutor<SQLPlan<*>>

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
 * @see [SQLPlanExecutor.executeString]
 */
@Throws(SQLPlanExecutionException::class)
fun SQLPlan<*>.executeString(): String {
    return planExecutor.executeString(this)
}

/**
 * @see [SQLPlanExecutor.executeLong]
 */
@Throws(SQLPlanExecutionException::class)
fun SQLPlan<*>.executeLong(): Long {
    return planExecutor.executeLong(this)
}

/**
 * @see [SQLPlanExecutor.executeDouble]
 */
@Throws(SQLPlanExecutionException::class)
fun SQLPlan<*>.executeDouble(): Double {
    return planExecutor.executeDouble(this)
}

/**
 * @see [SQLPlanExecutor.executeTemporal]
 */
@Throws(SQLPlanExecutionException::class)
fun <R : Temporal> SQLPlan<*>.executeTemporal(cls: Class<R>): R? {
    return planExecutor.executeTemporal(this, cls)
}

/**
 * @see [SQLPlanExecutor.executeUpdate]
 */
@Throws(SQLPlanExecutionException::class)
fun SQLPlan<*>.executeUpdate(): Int {
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
