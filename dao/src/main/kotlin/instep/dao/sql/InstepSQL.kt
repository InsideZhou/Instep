package instep.dao.sql

import instep.Instep
import instep.dao.Expression
import instep.dao.ExpressionFactory
import instep.dao.PlanFromText
import instep.dao.PlanFromTextFactory
import instep.dao.sql.impl.DefaultSQLPlanExecutor
import instep.servicecontainer.ServiceNotFoundException
import java.sql.Connection
import java.sql.ResultSet

@Suppress("unused")
object InstepSQL {
    const val LoggerName = "instep.dao.sql"

    init {
        try {
            Instep.make(SQLPlanExecutor::class.java)
        }
        catch(e: ServiceNotFoundException) {
            Instep.bind(SQLPlanExecutor::class.java, DefaultSQLPlanExecutor())
        }

        try {
            Instep.make(ExpressionFactory::class.java)
        }
        catch(e: ServiceNotFoundException) {
            Instep.bind(ExpressionFactory::class.java, Expression.Companion)
        }

        try {
            Instep.make(PlanFromTextFactory::class.java)
        }
        catch(e: ServiceNotFoundException) {
            Instep.bind(PlanFromTextFactory::class.java, PlanFromText.Companion)
        }

        try {
            Instep.make(ObjectSelectPlanFactory::class.java)
        }
        catch(e: ServiceNotFoundException) {
            Instep.bind(ObjectSelectPlanFactory::class.java, ObjectSelectPlan.Companion)
        }
    }

    fun plan(txt: String): PlanFromText {
        val factory = Instep.make(PlanFromTextFactory::class.java)
        return factory.createInstance(txt)
    }

    fun select(obj: Any): ObjectSelectPlan {
        val factory = Instep.make(ObjectSelectPlanFactory::class.java)
        return factory.createInstance(obj)
    }

    /**
     * @see [SQLPlanExecutor.execute]
     */
    fun execute(txt: String) {
        val factory = Instep.make(PlanFromTextFactory::class.java)
        return factory.createInstance(txt).execute()
    }

    /**
     * @see [SQLPlanExecutor.execute]
     */
    fun <T : Any> execute(txt: String, cls: Class<T>): List<T> {
        val factory = Instep.make(PlanFromTextFactory::class.java)
        return factory.createInstance(txt).execute(cls)
    }

    /**
     * @see [SQLPlanExecutor.executeScalar]
     */
    fun executeScalar(txt: String): String {
        val factory = Instep.make(PlanFromTextFactory::class.java)
        return factory.createInstance(txt).executeScalar()
    }

    /**
     * @see [SQLPlanExecutor.executeUpdate]
     */
    fun executeUpdate(txt: String): Long {
        val factory = Instep.make(PlanFromTextFactory::class.java)
        return factory.createInstance(txt).executeUpdate()
    }

    /**
     * @see [SQLPlanExecutor.executeResultSet]
     */
    fun executeResultSet(txt: String, conn: Connection): ResultSet {
        val factory = Instep.make(PlanFromTextFactory::class.java)
        return factory.createInstance(txt).executeResultSet(conn)
    }

    fun <R : Any?> uncommittedTransaction(runner: TransactionContext.() -> R): R {
        return TransactionTemplate.uncommitted(runner)
    }

    fun <R : Any?> committedTransaction(runner: TransactionContext.() -> R): R {
        return TransactionTemplate.committed(runner)
    }

    fun <R : Any?> repeatableTransaction(runner: TransactionContext.() -> R): R {
        return TransactionTemplate.repeatable(runner)
    }

    fun <R : Any?> serializableTransaction(runner: TransactionContext.() -> R): R {
        return TransactionTemplate.serializable(runner)
    }
}
