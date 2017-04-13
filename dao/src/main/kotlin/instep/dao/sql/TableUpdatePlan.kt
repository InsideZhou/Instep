package instep.dao.sql

import instep.dao.DaoException
import instep.dao.Plan
import instep.dao.sql.impl.DefaultTableUpdatePlan

interface TableUpdatePlan : Plan<TableUpdatePlan>, WhereClause<TableUpdatePlan> {
    @Throws(DaoException::class)
    fun set(column: Column<*>, value: Any?): TableUpdatePlan

    @Throws(DaoException::class)
    fun where(value: Any): TableUpdatePlan

    companion object : TableUpdatePlanFactory {
        override fun createInstance(table: Table): TableUpdatePlan {
            return DefaultTableUpdatePlan(table)
        }
    }
}

interface TableUpdatePlanFactory {
    fun createInstance(table: Table): TableUpdatePlan
}