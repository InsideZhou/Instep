package instep.dao.sql

import instep.Instep
import instep.dao.ExpressionFactory
import instep.dao.sql.impl.DefaultSQLPlanExecutor
import instep.servicecontainer.ServiceNotFoundException

@Suppress("unused")
object InstepSQL {
    init {
        try {
            Instep.make(SQLPlanFactory::class.java)
        }
        catch (e: ServiceNotFoundException) {
            Instep.bind(SQLPlanFactory::class.java, SQLPlanFactory.Companion)
        }

        try {
            Instep.make(SQLPlanExecutor::class.java)
        }
        catch (e: ServiceNotFoundException) {
            Instep.bind(SQLPlanExecutor::class.java, DefaultSQLPlanExecutor())
        }

        try {
            Instep.make(ExpressionFactory::class.java)
        }
        catch (e: ServiceNotFoundException) {
            Instep.bind(ExpressionFactory::class.java, ExpressionFactory.Companion)
        }
    }

    fun plan(txt: String): SQLPlan<*> {
        val factory = Instep.make(SQLPlanFactory::class.java)
        return factory.createInstance(txt)
    }

    fun executor(): SQLPlanExecutor {
        return Instep.make(SQLPlanExecutor::class.java)
    }

    fun transaction(): TransactionTemplate {
        return TransactionTemplate
    }
}
