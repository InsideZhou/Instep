package instep.dao.sql

import instep.dao.DaoException
import instep.dao.Plan
import instep.dao.sql.impl.DefaultTableUpdatePlan

interface TableUpdatePlan : Plan<TableUpdatePlan>, WhereClause<TableUpdatePlan> {
    override public fun clone(): TableUpdatePlan

    @Throws(DaoException::class)
    fun set(column: Column<*>, value: Any?): TableUpdatePlan

    @Throws(DaoException::class)
    fun where(value: Any): TableUpdatePlan

    companion object : TableUpdatePlanFactory<TableUpdatePlan> {
        override fun createInstance(table: Table): TableUpdatePlan {
            return DefaultTableUpdatePlan(table)
        }
    }
}

interface TableUpdatePlanFactory<out T : TableUpdatePlan> {
    fun createInstance(table: Table): T
}
