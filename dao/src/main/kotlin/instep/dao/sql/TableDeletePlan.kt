package instep.dao.sql

import instep.dao.sql.impl.DefaultTableDeletePlan

interface TableDeletePlan : SQLPlan<TableDeletePlan>, WhereClause<TableDeletePlan> {
    fun whereKey(key: Any): TableDeletePlan
}

interface TableDeletePlanFactory<out T : TableDeletePlan> {
    fun createInstance(table: Table): T

    companion object : TableDeletePlanFactory<TableDeletePlan> {
        override fun createInstance(table: Table): TableDeletePlan {
            return DefaultTableDeletePlan(table)
        }
    }
}
