@file:Suppress("unused")

package instep.dao.sql

import instep.Instep
import instep.InstepLogger
import instep.dao.Plan
import instep.typeconversion.TypeConversion
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException

/**
 * @see [SQLPlanExecutor.execute]
 */
@Throws(SQLPlanExecutionException::class)
fun Plan<*>.execute() {
    val planExec = Instep.make(SQLPlanExecutor::class.java)
    return planExec.execute(this)
}

/**
 * @see [SQLPlanExecutor.execute]
 */
@Throws(SQLPlanExecutionException::class)
fun <T : Any> Plan<*>.execute(cls: Class<T>): List<T> {
    val planExec = Instep.make(SQLPlanExecutor::class.java)
    return planExec.execute(this, cls)
}

/**
 * @see [SQLPlanExecutor.executeScalar]
 */
@Throws(SQLPlanExecutionException::class)
fun Plan<*>.executeScalar(): String {
    val planExec = Instep.make(SQLPlanExecutor::class.java)
    return planExec.executeScalar(this)
}

/**
 * @see [SQLPlanExecutor.executeScalar]
 */
@Throws(SQLPlanExecutionException::class)
fun <T : Any> Plan<*>.executeScalar(cls: Class<T>): T? {
    val planExec = Instep.make(SQLPlanExecutor::class.java)
    return planExec.executeScalar(this, cls)
}

/**
 * @see [SQLPlanExecutor.executeUpdate]
 */
@Throws(SQLPlanExecutionException::class)
fun Plan<*>.executeUpdate(): Long {
    val planExec = Instep.make(SQLPlanExecutor::class.java)
    return planExec.executeUpdate(this)
}

/**
 * @see [SQLPlanExecutor.executeResultSet]
 */
@Throws(SQLPlanExecutionException::class)
fun Plan<*>.executeResultSet(conn: Connection): ResultSet {
    val planExec = Instep.make(SQLPlanExecutor::class.java)
    return planExec.executeResultSet(conn, this)
}

@Suppress("unchecked_cast")
@Throws(SQLPlanExecutionException::class)
fun TableSelectPlan.execute(): List<TableRow> {
    val planExec = Instep.make(SQLPlanExecutor::class.java)
    val connMan = Instep.make(ConnectionProvider::class.java)
    val conn = connMan.getConnection()
    val result = mutableListOf<TableRow>()
    val rowFactory = Instep.make(TableRowFactory::class.java)

    try {
        val rs = planExec.executeResultSet(conn, this)
        while (rs.next()) {
            result.add(rowFactory.createInstance(this.from, connMan.dialect, rs))
        }
        return result
    }
    catch (e: SQLException) {
        throw SQLPlanExecutionException(e)
    }
    finally {
        conn.close()
    }
}

@Suppress("unchecked_cast")
@Throws(SQLPlanExecutionException::class)
fun <T : Any> TableSelectPlan.execute(cls: Class<T>): List<T> {
    val typeconvert = Instep.make(TypeConversion::class.java)
    val planExec = Instep.make(SQLPlanExecutor::class.java)

    if (typeconvert.canConvert(ResultSet::class.java, cls)) {
        return planExec.execute(this, cls)
    }

    val rows = execute()

    if (typeconvert.canConvert(TableRow::class.java, cls)) {
        return rows.map { typeconvert.convert(it, cls) }
    }

    val targetMirror = Instep.reflect(cls)
    val tableMirror = Instep.reflect(this.from)

    return rows.map { row ->
        val instance = targetMirror.type.newInstance()
        targetMirror.fieldsWithSetter.forEach { field ->
            val col = tableMirror.findGetter(field.name)?.invoke(this.from)
            if (null == col) return@forEach

            val value = row[col as Column<*>]
            if (null != value) {
                try {
                    targetMirror.findSetter(field.name)?.invoke(instance, value)
                }
                catch (e: IllegalArgumentException) {
                    InstepLogger.warning({ e.toString() }, InstepSQL::class.java)
                }
            }
        }

        return@map instance
    }
}
