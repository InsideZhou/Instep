@file:Suppress("unused")

package instep.dao.sql


infix fun Column<*>.eq(value: Any): ColumnCondition = ColumnCondition(this, "= ${this.table.dialect.placeholderForParameter(this)}", value)
infix fun Column<*>.notEQ(value: Any): ColumnCondition = ColumnCondition(this, "<> ${this.table.dialect.placeholderForParameter(this)}", value)
infix fun Column<*>.gt(value: Any): ColumnCondition = ColumnCondition(this, "> ${this.table.dialect.placeholderForParameter(this)}", value)
infix fun Column<*>.gte(value: Any): ColumnCondition = ColumnCondition(this, ">= ${this.table.dialect.placeholderForParameter(this)}", value)
infix fun Column<*>.lt(value: Any): ColumnCondition = ColumnCondition(this, "< ${this.table.dialect.placeholderForParameter(this)}", value)
infix fun Column<*>.lte(value: Any): ColumnCondition = ColumnCondition(this, "<= ${this.table.dialect.placeholderForParameter(this)}", value)
fun Column<*>.isNull(): Condition = ColumnCondition(this, "IS NULL")
fun Column<*>.notNull(): Condition = ColumnCondition(this, "IS NOT NULL")

infix fun Column<*>.inArray(values: Array<*>): ColumnCondition {
    val placeholder = this.table.dialect.placeholderForParameter(this)
    val builder = StringBuilder("IN (")

    values.forEach { _ ->
        builder.append("$placeholder,")
    }

    builder.deleteCharAt(builder.length - 1)
    builder.append(")")

    val condition = ColumnCondition(this, builder.toString())
    condition.addParameters(*values)
    return condition
}

infix fun StringColumn.startsWith(value: String): Condition = ColumnCondition(this, "LIKE ${this.table.dialect.placeholderForParameter(this)}", "$value%")
infix fun StringColumn.endsWith(value: String): Condition = ColumnCondition(this, "LIKE ${this.table.dialect.placeholderForParameter(this)}", "%$value")
infix fun StringColumn.contains(value: String): Condition = ColumnCondition(this, "LIKE ${this.table.dialect.placeholderForParameter(this)}", "%$value%")


infix fun <T : Enum<*>> IntegerColumn.gt(value: T): Condition = gt(value.ordinal)
infix fun <T : Enum<*>> IntegerColumn.gte(value: T): Condition = gte(value.ordinal)
infix fun <T : Enum<*>> IntegerColumn.lt(value: T): Condition = lt(value.ordinal)
infix fun <T : Enum<*>> IntegerColumn.lte(value: T): Condition = lte(value.ordinal)
infix fun <T : Enum<*>> IntegerColumn.inArray(value: Array<T>): Condition = inArray(value.map { it.ordinal }.toTypedArray())


fun Column<*>.alias(alias: String = "") = SelectExpression(qualifiedName, alias.ifBlank { "${table.tableName}_$name" })
fun Column<*>.count(alias: String = "") = SelectExpression("count(${qualifiedName})", alias)

fun NumberColumn<*>.sum(alias: String = "") = SelectExpression("sum(${qualifiedName})", alias)

fun NumberColumn<*>.avg(alias: String = "") = SelectExpression("avg(${qualifiedName})", alias)

fun NumberColumn<*>.max(alias: String = "") = SelectExpression("max(${qualifiedName})", alias)

fun NumberColumn<*>.min(alias: String = "") = SelectExpression("min(${qualifiedName})", alias)

fun DateTimeColumn.max(alias: String = "") = SelectExpression("max(${qualifiedName})", alias)

fun DateTimeColumn.min(alias: String = "") = SelectExpression("min(${qualifiedName})", alias)

@JvmOverloads
fun Column<*>.asc(nullFirst: Boolean = false): OrderBy {
    val column = this

    return object : OrderBy {
        override val column: Column<*> = column
        override val descending: Boolean = false
        override val nullFirst: Boolean = nullFirst
    }
}

@JvmOverloads
fun Column<*>.desc(nullFirst: Boolean = false): OrderBy {
    val column = this

    return object : OrderBy {
        override val column: Column<*> = column
        override val descending: Boolean = true
        override val nullFirst: Boolean = nullFirst
    }
}
