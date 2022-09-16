package instep.dao.sql

import instep.dao.Expression
import instep.dao.Plan
import instep.dao.sql.impl.DefaultSQLPlan

@Suppress("unused")
interface SQLPlan<T : SQLPlan<T>> : Plan<T>, Expression<T> {
    val subPlans: MutableList<SQLPlan<*>>

    fun addSubPlan(plan: SQLPlan<*>): SQLPlan<*>

    fun addParameters(vararg parameters: Any?): T

    override fun parameterToLogFormat(): String {
        return super<Plan>.parameterToLogFormat()
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
