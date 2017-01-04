package instep.orm.sql

import instep.Instep
import instep.orm.PlanFromText
import instep.orm.planbuild.DefaultPlanFromText
import instep.orm.sql.impl.DefaultObjectSelectPlan
import instep.orm.sql.impl.DefaultSQLPlanExecutor
import instep.orm.sql.impl.DefaultTableSelectPlan
import instep.servicecontainer.ServiceNotFoundException
import java.sql.Connection
import java.sql.ResultSet

object InstepSQL {
    var transactionLevel = Connection.TRANSACTION_READ_COMMITTED;

    init {
        try {
            Instep.make(SQLPlanExecutor::class.java)
        }
        catch(e: ServiceNotFoundException) {
            Instep.bind(SQLPlanExecutor::class.java, DefaultSQLPlanExecutor())
        }
    }

    fun plan(txt: String): PlanFromText {
        return DefaultPlanFromText(txt)
    }

    fun select(table: Table): TableSelectPlan {
        return DefaultTableSelectPlan(table)
    }

    fun select(obj: Any): ObjectSelectPlan {
        return DefaultObjectSelectPlan(obj)
    }

    /**
     * @see [SQLPlanExecutor.execute]
     */
    fun execute(txt: String) {
        return DefaultPlanFromText(txt).execute()
    }

    /**
     * @see [SQLPlanExecutor.execute]
     */
    fun <T : Any> execute(txt: String, cls: Class<T>): List<T> {
        return DefaultPlanFromText(txt).execute(cls)
    }

    /**
     * @see [SQLPlanExecutor.executeScalar]
     */
    fun executeScalar(txt: String): String {
        return DefaultPlanFromText(txt).executeScalar()
    }

    /**
     * @see [SQLPlanExecutor.executeUpdate]
     */
    fun executeUpdate(txt: String): Long {
        return DefaultPlanFromText(txt).executeUpdate()
    }

    /**
     * @see [SQLPlanExecutor.executeResultSet]
     */
    fun executeResultSet(txt: String, conn: Connection): ResultSet {
        return DefaultPlanFromText(txt).executeResultSet(conn)
    }

    inline fun <R : Any?> transaction(runner: () -> R): R {
        return transaction(transactionLevel, runner)
    }

    inline fun <R : Any?> transaction(level: Int, runner: () -> R): R {
        val connMan = Instep.make(ConnectionManager::class.java)
        val conn = connMan.getConnection()
        try {
            conn.transactionIsolation = level

            conn.setSavepoint()
            val result = runner()
            conn.commit()

            return result
        }
        catch(e: Exception) {
            conn.rollback()

            throw e
        }
        finally {
            conn.close()
        }
    }
}

