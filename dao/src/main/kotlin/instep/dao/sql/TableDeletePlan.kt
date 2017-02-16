package instep.dao.sql

import instep.dao.DaoException
import instep.dao.Plan
import instep.dao.sql.impl.DefaultTableDeletePlan

interface TableDeletePlan : Plan<TableDeletePlan>, WhereClause<TableDeletePlan> {
    @Throws(DaoException::class)
    fun where(value: Any): TableDeletePlan

    companion object : TableDeletePlanFactory {
        override fun createInstance(table: Table): TableDeletePlan {
            return DefaultTableDeletePlan(table)
        }
    }
}

interface TableDeletePlanFactory {
    fun createInstance(table: Table): TableDeletePlan
}
