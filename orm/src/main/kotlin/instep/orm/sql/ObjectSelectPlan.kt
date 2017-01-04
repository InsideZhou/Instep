package instep.orm.sql

import instep.orm.Plan

interface ObjectSelectPlan : Plan {
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
}