package instep.dao.sql

import instep.Instep
import instep.collection.AssocArray
import instep.dao.Plan
import instep.dao.sql.impl.DefaultTableSelectPlan
import instep.servicecontainer.ServiceNotFoundException

interface TableSelectPlan : Plan<TableSelectPlan>, WhereClause<TableSelectPlan> {
    val select: AssocArray
    val distinct: Boolean
    val from: Table
    val groupBy: List<Column<*>>
    val having: Condition?
    val orderBy: List<OrderBy>
    val limit: Int
    val offset: Int

    override public fun clone(): TableSelectPlan

    fun select(vararg columnOrAggregates: Any): TableSelectPlan
    fun distinct(): TableSelectPlan
    fun groupBy(vararg columns: Column<*>): TableSelectPlan
    fun having(vararg conditions: Condition): TableSelectPlan
    fun orderBy(vararg orderBys: OrderBy): TableSelectPlan
    fun limit(limit: Int): TableSelectPlan
    fun offset(offset: Int): TableSelectPlan

    companion object : TableSelectPlanFactory<TableSelectPlan> {
        init {
            try {
                Instep.make(TableRowFactory::class.java)
            }
            catch(e: ServiceNotFoundException) {
                Instep.bind(TableRowFactory::class.java, TableRow.Companion)
            }
        }

        override fun createInstance(table: Table): TableSelectPlan {
            return DefaultTableSelectPlan(table)
        }
    }
}

interface TableSelectPlanFactory<out T : TableSelectPlan> {
    fun createInstance(table: Table): T
}

