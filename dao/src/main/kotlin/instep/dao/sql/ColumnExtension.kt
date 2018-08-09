@file:Suppress("unused")

package instep.dao.sql

import instep.Instep
import java.time.temporal.Temporal

val dialect = Instep.make(Dialect::class.java)


infix fun BooleanColumn.eq(value: Boolean): Condition = Condition.eq(name, value)
infix fun BooleanColumn.notEQ(value: Boolean): Condition = Condition.notEQ(name, value)

infix fun StringColumn.eq(value: String): Condition = when (this.type) {
    StringColumnType.UUID -> Condition.eq(name, value, dialect.placeholderForUUIDType)
    StringColumnType.JSON -> Condition.eq(name, value, dialect.placeholderForJSONType)
    else -> Condition.eq(name, value)
}

infix fun StringColumn.notEQ(value: String): Condition = when (this.type) {
    StringColumnType.UUID -> Condition.notEQ(name, value, dialect.placeholderForUUIDType)
    StringColumnType.JSON -> Condition.notEQ(name, value, dialect.placeholderForJSONType)
    else -> Condition.notEQ(name, value)
}

infix fun StringColumn.startsWith(value: String): Condition = when (this.type) {
    StringColumnType.UUID -> Condition.startsWith(name, value, dialect.placeholderForUUIDType)
    StringColumnType.JSON -> Condition.startsWith(name, value, dialect.placeholderForJSONType)
    else -> Condition.startsWith(name, value)
}

infix fun StringColumn.endsWith(value: String): Condition = when (this.type) {
    StringColumnType.UUID -> Condition.endsWith(name, value, dialect.placeholderForUUIDType)
    StringColumnType.JSON -> Condition.endsWith(name, value, dialect.placeholderForJSONType)
    else -> Condition.endsWith(name, value)
}

infix fun StringColumn.contains(value: String): Condition = when (this.type) {
    StringColumnType.UUID -> Condition.contains(name, value, dialect.placeholderForUUIDType)
    StringColumnType.JSON -> Condition.contains(name, value, dialect.placeholderForJSONType)
    else -> Condition.contains(name, value)
}

infix fun StringColumn.inArray(value: Array<String>): Condition = when (this.type) {
    StringColumnType.UUID -> Condition.inArray(name, value, dialect.placeholderForUUIDType)
    StringColumnType.JSON -> Condition.inArray(name, value, dialect.placeholderForJSONType)
    else -> Condition.inArray(name, value)
}

infix fun StringColumn.eq(value: Enum<*>): Condition = eq(value.name)
infix fun StringColumn.notEQ(value: Enum<*>): Condition = notEQ(value.name)
infix fun StringColumn.startsWith(value: Enum<*>): Condition = startsWith(value.name)
infix fun StringColumn.endsWith(value: Enum<*>): Condition = endsWith(value.name)
infix fun StringColumn.contains(value: Enum<*>): Condition = contains(value.name)
infix fun StringColumn.inArray(value: Array<Enum<*>>): Condition = inArray(value.map { it.name }.toTypedArray())


infix fun <T : Enum<*>> IntegerColumn.eq(value: T): Condition = Condition.eq(name, value.ordinal)
infix fun <T : Enum<*>> IntegerColumn.notEQ(value: T): Condition = Condition.notEQ(name, value.ordinal)
infix fun <T : Enum<*>> IntegerColumn.gt(value: T): Condition = Condition.gt(name, value.ordinal)
infix fun <T : Enum<*>> IntegerColumn.gte(value: T): Condition = Condition.gte(name, value.ordinal)
infix fun <T : Enum<*>> IntegerColumn.lt(value: T): Condition = Condition.lt(name, value.ordinal)
infix fun <T : Enum<*>> IntegerColumn.lte(value: T): Condition = Condition.lte(name, value.ordinal)
infix fun <T : Enum<*>> IntegerColumn.inArray(value: Array<T>): Condition = Condition.inArray(name, value.map { it.ordinal }.toTypedArray())

infix fun <T : Number> NumberColumn<*>.eq(value: T): Condition = Condition.eq(name, value)
infix fun <T : Number> NumberColumn<*>.notEQ(value: T): Condition = Condition.notEQ(name, value)
infix fun <T : Number> NumberColumn<*>.gt(value: T): Condition = Condition.gt(name, value)
infix fun <T : Number> NumberColumn<*>.gte(value: T): Condition = Condition.gte(name, value)
infix fun <T : Number> NumberColumn<*>.lt(value: T): Condition = Condition.lt(name, value)
infix fun <T : Number> NumberColumn<*>.lte(value: T): Condition = Condition.lte(name, value)
infix fun <T : Number> NumberColumn<*>.inArray(value: Array<T>): Condition = Condition.inArray(name, value)


infix fun <T : Temporal> DateTimeColumn.eq(value: T): Condition = Condition.eq(name, value)
infix fun <T : Temporal> DateTimeColumn.notEQ(value: T): Condition = Condition.notEQ(name, value)
infix fun <T : Temporal> DateTimeColumn.gt(value: T): Condition = Condition.gt(name, value)
infix fun <T : Temporal> DateTimeColumn.gte(value: T): Condition = Condition.gte(name, value)
infix fun <T : Temporal> DateTimeColumn.lt(value: T): Condition = Condition.lt(name, value)
infix fun <T : Temporal> DateTimeColumn.lte(value: T): Condition = Condition.lte(name, value)


fun Column<*>.count(): Aggregate {
    return object : Aggregate {
        override val expression = "count($name)"
        override val alias = "${name}_count"
    }
}

fun NumberColumn<*>.sum(): Aggregate {
    return object : Aggregate {
        override val expression = "sum($name)"
        override val alias = "${name}_sum"
    }
}

fun NumberColumn<*>.avg(): Aggregate {
    return object : Aggregate {
        override val expression = "avg($name)"
        override val alias = "${name}_avg"
    }
}

fun NumberColumn<*>.max(): Aggregate {
    return object : Aggregate {
        override val expression = "max($name)"
        override val alias = "${name}_max"
    }
}

fun NumberColumn<*>.min(): Aggregate {
    return object : Aggregate {
        override val expression = "min($name)"
        override val alias = "${name}_min"
    }
}

fun DateTimeColumn.max(): Aggregate {
    return object : Aggregate {
        override val expression = "max($name)"
        override val alias = "${name}_max"
    }
}

fun DateTimeColumn.min(): Aggregate {
    return object : Aggregate {
        override val expression = "min($name)"
        override val alias = "${name}_min"
    }
}

fun Column<*>.isNull(): Condition = Condition.isNull(name)
fun Column<*>.notNull(): Condition = Condition.isNotNull(name)

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
