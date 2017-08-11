package instep.dao.sql

import instep.dao.DaoException
import instep.dao.Plan
import instep.dao.sql.impl.DefaultTableDeletePlan

interface TableDeletePlan : Plan<TableDeletePlan>, WhereClause<TableDeletePlan> {
    override public fun clone(): TableDeletePlan

    @Throws(DaoException::class)
    fun where(value: Any): TableDeletePlan

    companion object : TableDeletePlanFactory<TableDeletePlan> {
        override fun createInstance(table: Table): TableDeletePlan {
            return DefaultTableDeletePlan(table)
        }
    }
}

interface TableDeletePlanFactory<out T : TableDeletePlan> {
    fun createInstance(table: Table): T
}
