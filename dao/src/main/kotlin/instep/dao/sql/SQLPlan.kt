package instep.dao.sql

import instep.dao.Plan
import instep.dao.sql.impl.DefaultSQLPlan

interface SQLPlan<T : SQLPlan<T>> : Plan<T>

interface SQLPlanFactory<out T : SQLPlan<*>> {
    fun createInstance(txt: String): T

    companion object : SQLPlanFactory<SQLPlan<*>> {
        override fun createInstance(txt: String): SQLPlan<*> {
            return DefaultSQLPlan(txt)
        }
    }
}
