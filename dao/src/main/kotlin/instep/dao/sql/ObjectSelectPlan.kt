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

    override public fun clone(): ObjectSelectPlan

    fun where(vararg conditions: Condition): ObjectSelectPlan
    fun groupBy(vararg columns: String): ObjectSelectPlan
    fun having(vararg conditions: Condition): ObjectSelectPlan
    fun orderBy(vararg orderBys: String): ObjectSelectPlan
    fun limit(limit: Int): ObjectSelectPlan
    fun offset(offset: Int): ObjectSelectPlan

    companion object : ObjectSelectPlanFactory<ObjectSelectPlan> {
        override fun createInstance(obj: Any): ObjectSelectPlan {
            return DefaultObjectSelectPlan(obj)
        }
    }
}

interface ObjectSelectPlanFactory<out T : ObjectSelectPlan> {
    fun createInstance(obj: Any): T
}