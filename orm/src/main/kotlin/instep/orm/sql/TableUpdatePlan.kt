package instep.orm.sql

import instep.orm.OrmException
import instep.orm.Plan
import instep.orm.sql.impl.DefaultTableUpdatePlan

interface TableUpdatePlan : Plan<TableUpdatePlan>, WhereClause<TableUpdatePlan> {
    @Throws(OrmException::class)
    fun set(column: Column<*>, value: Any?): TableUpdatePlan

    @Throws(OrmException::class)
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
