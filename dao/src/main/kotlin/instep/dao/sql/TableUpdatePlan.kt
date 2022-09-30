package instep.dao.sql

import instep.dao.sql.impl.DefaultTableUpdatePlan

interface TableUpdatePlan : SQLPlan<TableUpdatePlan>, WhereClause<TableUpdatePlan> {
    fun step(column: NumberColumn<*>, value: Number): TableUpdatePlan
    fun set(column: Column<*>, value: Any?): TableUpdatePlan
    fun set(obj: Any): TableUpdatePlan
    fun set(colNameToValues: Map<String, *>): TableUpdatePlan
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

data class StepValue(val step: Number)
