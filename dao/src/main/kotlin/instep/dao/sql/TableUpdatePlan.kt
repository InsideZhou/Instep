package instep.dao.sql

import instep.dao.DaoException
import instep.dao.sql.impl.DefaultTableUpdatePlan

interface TableUpdatePlan : SQLPlan<TableUpdatePlan>, WhereClause<TableUpdatePlan> {
    @Throws(DaoException::class)
    fun set(column: Column<*>, value: Any?): TableUpdatePlan

    fun set(obj: Any): TableUpdatePlan

    @Throws(DaoException::class)
    fun whereKey(key: Any): TableUpdatePlan
}

interface TableUpdatePlanFactory<out T : TableUpdatePlan> {
    fun createInstance(table: Table): T

    companion object : TableUpdatePlanFactory<TableUpdatePlan> {
        override fun createInstance(table: Table): TableUpdatePlan {
            return DefaultTableUpdatePlan(table)
        }
    }
}
