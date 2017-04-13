package instep.dao.sql

import instep.dao.DaoException
import instep.dao.Plan
import instep.dao.sql.impl.DefaultTableInsertPlan

interface TableInsertPlan : Plan<TableInsertPlan> {
    @Throws(DaoException::class)
    fun addValue(column: Column<*>, value: Any?): TableInsertPlan

    @Throws(DaoException::class)
    fun addValues(vararg values: Any?): TableInsertPlan

    companion object : TableInsertPlanFactory {
        override fun createInstance(table: Table): TableInsertPlan {
            return DefaultTableInsertPlan(table)
        }
    }
}

interface TableInsertPlanFactory {
    fun createInstance(table: Table): TableInsertPlan
}