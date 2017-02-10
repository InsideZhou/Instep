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
        val transactionScope = Instep.make(TransactionScope::class.java)
        return transactionScope.template(transactionLevel, runner)
    }

    fun <R : Any?> transaction(level: Int, runner: TransactionContext.() -> R): R {
        val transactionScope = Instep.make(TransactionScope::class.java)
        return transactionScope.template(level, runner)
    }
}
