package instep.dao.sql

import instep.dao.Plan
import instep.dao.sql.impl.DefaultObjectSelectPlan

interface ObjectSelectPlan : Plan<ObjectSelectPlan> {
    val select: List<String>
    val from: String
    val where: Condition?
    val groupBy: List<String>
    val having: Condition?
    val orderBy: List<String>
    val limit: Int
    val offset: Int

    fun where(vararg conditions: Condition): ObjectSelectPlan
    fun groupBy(vararg columns: String): ObjectSelectPlan
    fun having(vararg conditions: Condition): ObjectSelectPlan
    fun orderBy(vararg orderBys: String): ObjectSelectPlan
    fun limit(limit: Int): ObjectSelectPlan
    fun offset(offset: Int): ObjectSelectPlan

    companion object : ObjectSelectPlanFactory {
        override fun createInstance(obj: Any): ObjectSelectPlan {
            return DefaultObjectSelectPlan(obj)
        }
    }
}

interface ObjectSelectPlanFactory {
    fun createInstance(obj: Any): ObjectSelectPlan
}