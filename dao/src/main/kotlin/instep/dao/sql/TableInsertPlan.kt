package instep.dao.sql

import instep.dao.DaoException
import instep.dao.Plan
import instep.dao.sql.impl.DefaultTableInsertPlan

interface TableInsertPlan : Plan<TableInsertPlan> {
    override public fun clone(): TableInsertPlan

    @Throws(DaoException::class)
    fun addValue(column: Column<*>, value: Any?): TableInsertPlan

    @Throws(DaoException::class)
    fun addValues(vararg values: Any?): TableInsertPlan

    companion object : TableInsertPlanFactory<TableInsertPlan> {
        override fun createInstance(table: Table, dialect: Dialect): TableInsertPlan {
            return DefaultTableInsertPlan(table)
        }
    }
}

interface TableInsertPlanFactory<out T : TableInsertPlan> {
    fun createInstance(table: Table, dialect: Dialect): T
}
