package instep.orm.sql

import instep.collection.AssocArray
import instep.orm.Plan
import instep.orm.sql.impl.DefaultTableSelectPlan

interface TableSelectPlan : Plan {
    val select: AssocArray
    val from: Table
    val where: Condition?
    val groupBy: List<Column<*>>
    val having: Condition?
    val orderBy: List<OrderBy>
    val limit: Int
    val offset: Int

    fun where(vararg conditions: Condition): TableSelectPlan
    fun groupBy(vararg columns: Column<*>): TableSelectPlan
    fun having(vararg conditions: Condition): TableSelectPlan
    fun orderBy(vararg orderBys: OrderBy): TableSelectPlan
    fun limit(limit: Int): TableSelectPlan
    fun offset(offset: Int): TableSelectPlan

    companion object {
        fun createInstance(table: Table): TableSelectPlan {
            return DefaultTableSelectPlan(table)
        }
    }
}