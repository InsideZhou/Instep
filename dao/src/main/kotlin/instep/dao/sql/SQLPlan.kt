package instep.dao.sql

import instep.dao.Plan
import instep.dao.sql.impl.DefaultSQLPlan

interface SQLPlan<T : SQLPlan<T>> : Plan<T> {
    val subPlans: MutableList<SQLPlan<*>>

    fun addSubPlan(plan: SQLPlan<*>): SQLPlan<*>
}

abstract class SubSQLPlan<T : SQLPlan<T>> : SQLPlan<T> {
    override val subPlans: MutableList<SQLPlan<*>> = mutableListOf()

    override fun addSubPlan(plan: SQLPlan<*>): SQLPlan<*> {
        subPlans.add(plan)

        return this
    }
}

interface SQLPlanFactory<out T : SQLPlan<*>> {
    fun createInstance(txt: String): T

    companion object : SQLPlanFactory<SQLPlan<*>> {
        override fun createInstance(txt: String): SQLPlan<*> {
            return DefaultSQLPlan(txt)
        }
    }
}
