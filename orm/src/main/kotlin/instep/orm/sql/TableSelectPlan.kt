package instep.orm.sql

import instep.Instep
import instep.collection.AssocArray
import instep.orm.Plan
import instep.orm.sql.impl.DefaultTableSelectPlan
import instep.servicecontainer.ServiceNotFoundException

interface TableSelectPlan : Plan<TableSelectPlan>, WhereClause<TableSelectPlan> {
    val select: AssocArray
    val from: Table
    val groupBy: List<Column<*>>
    val having: Condition?
    val orderBy: List<OrderBy>
    val limit: Int
    val offset: Int


    fun select(vararg columnOrAggregates: Any): TableSelectPlan
    fun groupBy(vararg columns: Column<*>): TableSelectPlan
    fun having(vararg conditions: Condition): TableSelectPlan
    fun orderBy(vararg orderBys: OrderBy): TableSelectPlan
    fun limit(limit: Int): TableSelectPlan
    fun offset(offset: Int): TableSelectPlan

    companion object {
        init {
            try {
                Instep.make(TableRow.Companion::class.java)
            }
            catch(e: ServiceNotFoundException) {
                Instep.bind(TableRow.Companion::class.java, TableRow.Companion)
            }
        }

        fun createInstance(table: Table): TableSelectPlan {
            return DefaultTableSelectPlan(table)
        }
    }
}