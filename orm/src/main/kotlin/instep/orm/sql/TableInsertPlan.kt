package instep.orm.sql

import instep.orm.OrmException
import instep.orm.Plan
import instep.orm.sql.impl.DefaultTableInsertPlan

interface TableInsertPlan : Plan<TableInsertPlan> {
    @Throws(OrmException::class)
    fun addValue(column: Column<*>, value: Any?): TableInsertPlan

    @Throws(OrmException::class)
    fun addValues(vararg values: Any?): TableInsertPlan

    companion object {
        fun createInstance(table: Table): TableInsertPlan {
            return DefaultTableInsertPlan(table)
        }
    }
}