package instep.orm.sql

import instep.Instep
import instep.orm.PlanFromText
import instep.orm.sql.impl.DefaultSQLPlanExecutor
import instep.servicecontainer.ServiceNotFoundException
import java.sql.Connection
import java.sql.ResultSet

object InstepSQL {
    var transactionLevel = Connection.TRANSACTION_READ_COMMITTED

    init {
        try {
            Instep.make(SQLPlanExecutor::class.java)
        }
        catch(e: ServiceNotFoundException) {
            Instep.bind(SQLPlanExecutor::class.java, DefaultSQLPlanExecutor())
        }

        try {
            Instep.make(PlanFromText.Companion::class.java)
        }
        catch(e: ServiceNotFoundException) {
            Instep.bind(PlanFromText.Companion::class.java, PlanFromText.Companion)
        }

        try {
            Instep.make(ObjectSelectPlan.Companion::class.java)
        }
        catch(e: ServiceNotFoundException) {
            Instep.bind(ObjectSelectPlan.Companion::class.java, ObjectSelectPlan.Companion)
        }
    }

    fun plan(txt: String): PlanFromText {
        val factory = Instep.make(PlanFromText.Companion::class.java)
        return factory.createInstance(txt)
    }

    fun select(obj: Any): ObjectSelectPlan {
        val factory = Instep.make(ObjectSelectPlan.Companion::class.java)
        return factory.createInstance(obj)
    }

    /**
     * @see [SQLPlanExecutor.execute]
     */
    fun execute(txt: String) {
        val factory = Instep.make(PlanFromText.Companion::class.java)
        return factory.createInstance(txt).execute()
    }

    /**
     * @see [SQLPlanExecutor.execute]
     */
    fun <T : Any> execute(txt: String, cls: Class<T>): List<T> {
        val factory = Instep.make(PlanFromText.Companion::class.java)
        return factory.createInstance(txt).execute(cls)
    }

    /**
     * @see [SQLPlanExecutor.executeScalar]
     */
    fun executeScalar(txt: String): String {
        val factory = Instep.make(PlanFromText.Companion::class.java)
        return factory.createInstance(txt).executeScalar()
    }

    /**
     * @see [SQLPlanExecutor.executeUpdate]
     */
    fun executeUpdate(txt: String): Long {
        val factory = Instep.make(PlanFromText.Companion::class.java)
        return factory.createInstance(txt).executeUpdate()
    }

    /**
     * @see [SQLPlanExecutor.executeResultSet]
     */
    fun executeResultSet(txt: String, conn: Connection): ResultSet {
        val factory = Instep.make(PlanFromText.Companion::class.java)
        return factory.createInstance(txt).executeResultSet(conn)
    }

    fun <R : Any?> transaction(runner: TransactionContext.() -> R): R {
        return transaction(transactionLevel, runner)
    }

    fun <R : Any?> transaction(level: Int, runner: TransactionContext.() -> R): R {
        var transactionContext = TransactionContext.threadLocalTransactionContext.get()
        if (null == transactionContext) {
            val connMan = Instep.make(ConnectionProvider::class.java)
            val conn = connMan.getConnection()
            conn.transactionIsolation = level
            conn.autoCommit = false
            transactionContext = TransactionContext(conn)
        }
        else {
            transactionContext.depth += 1
        }

        TransactionContext.threadLocalTransactionContext.set(transactionContext)
        val conn = transactionContext.conn
        val sp = conn.setSavepoint()

        try {
            val result = runner(transactionContext)

            if (transactionContext.depth > 0) {
                conn.releaseSavepoint(sp)
            }
            else {
                TransactionContext.threadLocalTransactionContext.set(null)
                conn.commit()
            }

            return result
        }
        catch(e: TransactionContext.AbortException) {
            conn.rollback(sp)
            return null as R
        }
        catch(e: Exception) {
            conn.rollback(sp)
            throw e
        }
        finally {
            if (transactionContext.depth > 0) {
                transactionContext.depth -= 1
            }
            else {
                TransactionContext.threadLocalTransactionContext.set(null)
                conn.close()
            }
        }
    }
}
