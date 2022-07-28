package instep.dao.sql

import instep.dao.Alias
import instep.dao.Expression
import instep.dao.impl.AbstractExpression


interface FromItem<T : Expression<T>> : Expression<T>, Alias<T> {
    val joinType: JoinType
}

open class TableFromItem(override val joinType: JoinType, val table: Table, override var alias: String) :
    AbstractExpression<TableFromItem>(table.tableName), FromItem<TableFromItem> {

    constructor(joinType: JoinType, table: Table) : this(joinType, table, table.tableName)
}

enum class JoinType {
    Left, Inner, Right, Outer
}