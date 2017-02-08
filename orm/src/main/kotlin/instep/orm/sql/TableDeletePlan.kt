package instep.orm.sql

import instep.orm.OrmException
import instep.orm.Plan
import instep.orm.sql.impl.DefaultTableDeletePlan

interface TableDeletePlan : Plan<TableDeletePlan>, WhereClause<TableDeletePlan> {
    @Throws(OrmException::class)
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
