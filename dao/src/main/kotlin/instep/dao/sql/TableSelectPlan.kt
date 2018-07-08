package instep.dao.sql

import instep.collection.AssocArray
import instep.dao.sql.impl.DefaultTableSelectPlan

interface TableSelectPlan : SQLPlan<TableSelectPlan>, WhereClause<TableSelectPlan> {
    val select: AssocArray
    val distinct: Boolean
    val from: Table
    val groupBy: List<Column<*>>
    val having: Condition?
    val orderBy: List<OrderBy>
    val limit: Int
    val offset: Int

    fun select(vararg columnOrAggregates: Any): TableSelectPlan
    fun distinct(): TableSelectPlan
    fun groupBy(vararg columns: Column<*>): TableSelectPlan
    fun having(vararg conditions: Condition): TableSelectPlan
    fun orderBy(vararg orderBys: OrderBy): TableSelectPlan
    fun limit(limit: Int): TableSelectPlan
    fun offset(offset: Int): TableSelectPlan
}

interface TableSelectPlanFactory<out T : TableSelectPlan> {
    fun createInstance(table: Table): T

    companion object : TableSelectPlanFactory<TableSelectPlan> {
        override fun createInstance(table: Table): TableSelectPlan {
            return DefaultTableSelectPlan(table)
        }
    }
}

