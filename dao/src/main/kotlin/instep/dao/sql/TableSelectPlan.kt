package instep.dao.sql

import instep.dao.Alias
import instep.dao.impl.AbstractExpression
import instep.dao.sql.dialect.SQLServerDialect
import instep.dao.sql.impl.DefaultTableSelectPlan

interface TableSelectPlan : SQLPlan<TableSelectPlan>, WhereClause<TableSelectPlan>, Alias<TableSelectPlan> {
    val select: List<SelectExpression>
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

    fun selectExpression(vararg selectExpression: SelectExpression): TableSelectPlan
    fun distinct(): TableSelectPlan
    fun groupBy(vararg columnExpressions: ColumnExpression): TableSelectPlan
    fun having(vararg conditions: Condition): TableSelectPlan
    fun orderBy(vararg orderBys: OrderBy): TableSelectPlan
    fun limit(limit: Int): TableSelectPlan
    fun offset(offset: Int): TableSelectPlan

    fun join(fromItem: FromItem<*>): TableSelectPlan
    fun join(from: Column<*>, to: Column<*>): TableSelectPlan
    fun join(to: Column<*>): TableSelectPlan

    fun leftJoin(from: Column<*>, to: Column<*>): TableSelectPlan
    fun rightJoin(from: Column<*>, to: Column<*>): TableSelectPlan
    fun outerJoin(from: Column<*>, to: Column<*>): TableSelectPlan
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

open class SelectExpression(txt: String, override var alias: String) : AbstractExpression<SelectExpression>(txt), Alias<SelectExpression>

class ColumnExpression(val column: Column<*>, override var alias: String) : SelectExpression(column.name, alias) {
    constructor(column: Column<*>) : this(column, "")
}
