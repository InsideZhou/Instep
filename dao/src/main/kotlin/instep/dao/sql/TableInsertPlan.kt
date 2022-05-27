package instep.dao.sql

import instep.dao.sql.impl.DefaultTableInsertPlan

interface TableInsertPlan : SQLPlan<TableInsertPlan> {
    val table: Table

    fun addValue(column: Column<*>, value: Any?): TableInsertPlan

    fun set(obj: Any): TableInsertPlan

    fun returning(vararg columnOrAggregates: Any): TableInsertPlan
}

interface TableInsertPlanFactory<out T : TableInsertPlan> {
    fun createInstance(table: Table): T

    companion object : TableInsertPlanFactory<TableInsertPlan> {
        override fun createInstance(table: Table): TableInsertPlan {
            return DefaultTableInsertPlan(table)
        }
    }
}
