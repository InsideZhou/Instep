package instep.dao.sql

import instep.Instep
import instep.dao.Plan
import instep.dao.sql.impl.DefaultSQLPlanExecutor
import instep.servicecontainer.ServiceNotFoundException
import java.sql.Connection
import java.sql.ResultSet

private val init = run {
    try {
        Instep.make(SQLPlanExecutor::class.java)
    }
    catch(e: ServiceNotFoundException) {
        Instep.bind(SQLPlanExecutor::class.java, DefaultSQLPlanExecutor())
    }
}

/**
 * @see [SQLPlanExecutor.execute]
 */
fun Plan<*>.execute() {
    val planExec = Instep.make(SQLPlanExecutor::class.java)
    return planExec.execute(this)
}

/**
 * @see [SQLPlanExecutor.execute]
 */
fun <T : Any> Plan<*>.execute(cls: Class<T>): List<T> {
    val planExec = Instep.make(SQLPlanExecutor::class.java)
    return planExec.execute(this, cls)
}

/**
 * @see [SQLPlanExecutor.executeScalar]
 */
fun Plan<*>.executeScalar(): String {
    val planExec = Instep.make(SQLPlanExecutor::class.java)
    return planExec.executeScalar(this)
}

/**
 * @see [SQLPlanExecutor.executeUpdate]
 */
fun Plan<*>.executeUpdate(): Long {
    val planExec = Instep.make(SQLPlanExecutor::class.java)
    return planExec.executeUpdate(this)
}

/**
 * @see [SQLPlanExecutor.executeResultSet]
 */
fun Plan<*>.executeResultSet(conn: Connection): ResultSet {
    val planExec = Instep.make(SQLPlanExecutor::class.java)
    return planExec.executeResultSet(conn, this)
}

@Suppress("unchecked_cast")
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
    finally {
        conn.close()
    }
}
