package instep.dao.sql

import instep.dao.AbstractExpression
import instep.dao.Alias
import instep.dao.Expression
import instep.dao.sql.dialect.SQLServerDialect
import instep.dao.sql.impl.DefaultTableSelectPlan

interface TableSelectPlan : SQLPlan<TableSelectPlan>, WhereClause<TableSelectPlan>, Alias<TableSelectPlan> {
    val select: List<SelectExpression>
    val distinct: Boolean
    val from: Table
    val join: List<JoinItem<*>>
    val groupBy: List<Column<*>>
    val having: Condition
    val orderBy: List<OrderBy>
    val limit: Int
    val offset: Int

    fun select(vararg columns: Column<*>): TableSelectPlan {
        return selectExpression(*columns.map { ColumnSelectExpression(it) }.toTypedArray())
    }

    fun select(table: Table, aliasPrefix: String = "", vararg exceptions: Column<*>): TableSelectPlan {
        return selectExpression(
            *table.columns
                .filterNot { exceptions.contains(it) }
                .map {
                    val prefix = if (table != from) {
                        aliasPrefix.ifBlank { "${table.tableName}_" }
                    }
                    else {
                        aliasPrefix
                    }

                    ColumnSelectExpression(it, prefix + it.name)
                }
                .toTypedArray()
        )
    }

    fun selectExpression(vararg selectExpression: SelectExpression): TableSelectPlan
    fun distinct(): TableSelectPlan
    fun groupBy(vararg columns: Column<*>): TableSelectPlan
    fun having(vararg conditions: Condition): TableSelectPlan
    fun orderBy(vararg orderBys: OrderBy): TableSelectPlan
    fun limit(limit: Int): TableSelectPlan
    fun offset(offset: Int): TableSelectPlan

    fun join(joinItem: JoinItem<*>): TableSelectPlan
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

open class ColumnSelectExpression(val column: Column<*>, override var alias: String) : SelectExpression(column.qualifiedName, alias) {
    constructor(column: Column<*>) : this(column, "")
}

interface JoinItem<T : Expression<T>> : Expression<T> {
    val joinType: JoinType
    val joinCondition: Condition
}

open class TableJoinItem(
    val column: Column<*>,
    override val joinType: JoinType,
    override val joinCondition: Condition,
) : AbstractExpression<TableJoinItem>(column.table.tableName), JoinItem<TableJoinItem> {
    constructor(joinType: JoinType, column: Column<*>, condition: Condition) : this(column, joinType, condition)
}

enum class JoinType {
    Left, Inner, Right, Outer
}
