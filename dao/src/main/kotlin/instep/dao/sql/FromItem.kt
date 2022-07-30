package instep.dao.sql

import instep.dao.Alias
import instep.dao.Expression
import instep.dao.impl.AbstractExpression


interface FromItem<T : Expression<T>> : Expression<T>, Alias<T> {
    val joinType: JoinType
    val condition: Condition
}

open class TableFromItem(override val joinType: JoinType, val column: Column<*>, override var alias: String, override val condition: Condition) :
    AbstractExpression<TableFromItem>(column.table.tableName), FromItem<TableFromItem> {

    constructor(joinType: JoinType, column: Column<*>, condition: Condition) : this(joinType, column, "", condition)
}

enum class JoinType {
    Left, Inner, Right, Outer
}
