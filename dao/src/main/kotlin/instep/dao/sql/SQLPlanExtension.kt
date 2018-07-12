@file:Suppress("unused")

package instep.dao.sql

import instep.Instep
import instep.InstepLogger
import instep.typeconversion.TypeConversion
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException

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

@Suppress("unchecked_cast")
@Throws(SQLPlanExecutionException::class)
fun TableSelectPlan.execute(): List<TableRow> {
    Instep.make(ConnectionProvider::class.java).let { connMan ->
        val conn = connMan.getConnection()
        val result = mutableListOf<TableRow>()
        val rowFactory = Instep.make(TableRowFactory::class.java)

        try {
            val rs = planExecutor.executeResultSet(conn, this)
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
}

@Suppress("unchecked_cast")
@Throws(SQLPlanExecutionException::class)
fun <T : Any> TableSelectPlan.execute(cls: Class<T>): List<T> {
    val plan = this

    Instep.make(TypeConversion::class.java).run {
        if (canConvert(ResultSet::class.java, cls)) return planExecutor.execute(plan, cls)

        val rows = execute()

        if (canConvert(TableRow::class.java, cls)) return rows.map { row -> convert(row, cls) }

        val targetMirror = Instep.reflect(cls)
        val tableMirror = Instep.reflect(plan.from)

        return rows.map { row ->
            val instance = cls.newInstance()

            targetMirror.mutableProperties.forEach { p ->
                tableMirror.readableProperties.find {
                    p.field.name == it.field.name && Column::class.java.isAssignableFrom(it.field.type)
                }?.let {
                    val col = it.getter.invoke(plan.from) as Column<*>

                    row[col]?.let {
                        try {
                            p.setter.invoke(instance, it)
                        }
                        catch (e: IllegalArgumentException) {
                            InstepLogger.warning({ e.toString() }, InstepSQL::class.java)
                        }
                    }

                    return@forEach
                }
            }

            return@map instance
        }
    }
}
