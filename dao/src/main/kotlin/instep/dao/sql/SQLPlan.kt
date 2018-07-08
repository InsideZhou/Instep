package instep.dao.sql

import instep.Instep
import instep.dao.Plan
import instep.dao.sql.impl.DefaultSQLPlan

interface SQLPlan<T : SQLPlan<T>> : Plan<T>

interface SQLPlanFactory<out T : SQLPlan<*>> {
    fun createInstance(txt: String): T

    companion object : SQLPlanFactory<SQLPlan<*>> {
        init {
            Instep.bind(SQLPlanFactory::class.java, SQLPlanFactory.Companion)
        }

        override fun createInstance(txt: String): SQLPlan<*> {
            return DefaultSQLPlan(txt)
        }
    }
}
