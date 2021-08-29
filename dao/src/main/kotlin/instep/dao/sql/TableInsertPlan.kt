package instep.dao.sql

import instep.collection.AssocArray
import instep.dao.sql.impl.DefaultTableInsertPlan

interface TableInsertPlan : SQLPlan<TableInsertPlan> {
    val returning: AssocArray

    fun addValue(column: Column<*>, value: Any?): TableInsertPlan

    fun set(obj: Any): TableInsertPlan

    fun returning(vararg columnOrAggregates: Any): TableInsertPlan
}

interface TableInsertPlanFactory<out T : TableInsertPlan> {
    fun createInstance(table: Table, dialect: Dialect): T

    companion object : TableInsertPlanFactory<TableInsertPlan> {
        override fun createInstance(table: Table, dialect: Dialect): TableInsertPlan {
            return DefaultTableInsertPlan(table)
        }
    }
}
