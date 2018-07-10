package instep.dao.sql

import instep.Instep

@Suppress("unused")
object InstepSQL {
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
