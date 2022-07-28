package instep.dao.sql

import instep.dao.Alias
import instep.dao.sql.dialect.SQLServerDialect
import instep.dao.sql.impl.DefaultTableSelectPlan

interface TableSelectPlan : SQLPlan<TableSelectPlan>, WhereClause<TableSelectPlan>, Alias<TableSelectPlan> {
    val select: List<ColumnExpression>
    val distinct: Boolean
    val from: Table
    val join: List<FromItem<*>>
    val groupBy: List<ColumnExpression>
    val having: Condition
    val orderBy: List<OrderBy>
    val limit: Int
    val offset: Int

    fun select(vararg columns: Column<*>): TableSelectPlan {
        return selectExpression(*columns.map { ColumnExpression(it) }.toTypedArray())
    }

    fun selectExpression(vararg columnExpressions: ColumnExpression): TableSelectPlan
    fun distinct(): TableSelectPlan
    fun groupBy(vararg columnExpressions: ColumnExpression): TableSelectPlan
    fun having(vararg conditions: Condition): TableSelectPlan
    fun orderBy(vararg orderBys: OrderBy): TableSelectPlan
    fun limit(limit: Int): TableSelectPlan
    fun offset(offset: Int): TableSelectPlan

    fun join(fromItem: FromItem<*>): TableSelectPlan
    fun leftJoin(table: Table): TableSelectPlan
    fun join(table: Table): TableSelectPlan
    fun rightJoin(table: Table): TableSelectPlan
    fun outerJoin(table: Table): TableSelectPlan
}

interface TableSelectPlanFactory<out T : TableSelectPlan> {
    fun createInstance(table: Table): T

    companion object : TableSelectPlanFactory<TableSelectPlan> {
        override fun createInstance(table: Table): TableSelectPlan = when (table.dialect) {
            is SQLServerDialect -> SQLServerDialect.SelectPlan(table)
            else -> DefaultTableSelectPlan(table)
        }
    }
}
