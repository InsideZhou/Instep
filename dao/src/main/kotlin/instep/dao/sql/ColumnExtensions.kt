@file:Suppress("unused")

package instep.dao.sql

import instep.Instep
import java.time.temporal.Temporal

internal object PackageObject {
    val dialect = Instep.make(Dialect::class.java)
}

infix fun BooleanColumn.eq(value: Boolean): Condition = PackageObject.dialect.eq(this, value)
infix fun BooleanColumn.notEQ(value: Boolean): Condition = PackageObject.dialect.notEQ(this, value)

infix fun StringColumn.eq(value: String): Condition = PackageObject.dialect.eq(this, value)
infix fun StringColumn.notEQ(value: String): Condition = PackageObject.dialect.notEQ(this, value)
infix fun StringColumn.startsWith(value: String): Condition = PackageObject.dialect.startsWith(this, value)
infix fun StringColumn.endsWith(value: String): Condition = PackageObject.dialect.endsWith(this, value)
infix fun StringColumn.contains(value: String): Condition = PackageObject.dialect.contains(this, value)
infix fun StringColumn.inArray(value: Array<String>): Condition = PackageObject.dialect.inArray(this, value)


infix fun StringColumn.eq(value: Enum<*>): Condition = eq(value.name)
infix fun StringColumn.notEQ(value: Enum<*>): Condition = notEQ(value.name)
infix fun StringColumn.startsWith(value: Enum<*>): Condition = startsWith(value.name)
infix fun StringColumn.endsWith(value: Enum<*>): Condition = endsWith(value.name)
infix fun StringColumn.contains(value: Enum<*>): Condition = contains(value.name)
infix fun StringColumn.inArray(value: Array<Enum<*>>): Condition = inArray(value.map { it.name }.toTypedArray())


infix fun <T : Enum<*>> IntegerColumn.eq(value: T): Condition = PackageObject.dialect.eq(this, value.ordinal)
infix fun <T : Enum<*>> IntegerColumn.notEQ(value: T): Condition = PackageObject.dialect.notEQ(this, value.ordinal)
infix fun <T : Enum<*>> IntegerColumn.gt(value: T): Condition = PackageObject.dialect.gt(this, value.ordinal)
infix fun <T : Enum<*>> IntegerColumn.gte(value: T): Condition = PackageObject.dialect.gte(this, value.ordinal)
infix fun <T : Enum<*>> IntegerColumn.lt(value: T): Condition = PackageObject.dialect.lt(this, value.ordinal)
infix fun <T : Enum<*>> IntegerColumn.lte(value: T): Condition = PackageObject.dialect.lte(this, value.ordinal)
infix fun <T : Enum<*>> IntegerColumn.inArray(value: Array<T>): Condition = PackageObject.dialect.inArray(this, value.map { it.ordinal }.toTypedArray())

infix fun <T : Number> NumberColumn<*>.eq(value: T): Condition = PackageObject.dialect.eq(this, value)
infix fun <T : Number> NumberColumn<*>.notEQ(value: T): Condition = PackageObject.dialect.notEQ(this, value)
infix fun <T : Number> NumberColumn<*>.gt(value: T): Condition = PackageObject.dialect.gt(this, value)
infix fun <T : Number> NumberColumn<*>.gte(value: T): Condition = PackageObject.dialect.gte(this, value)
infix fun <T : Number> NumberColumn<*>.lt(value: T): Condition = PackageObject.dialect.lt(this, value)
infix fun <T : Number> NumberColumn<*>.lte(value: T): Condition = PackageObject.dialect.lte(this, value)
infix fun <T : Number> NumberColumn<*>.inArray(value: Array<T>): Condition = PackageObject.dialect.inArray(this, value)


infix fun <T : Temporal> DateTimeColumn.eq(value: T): Condition = PackageObject.dialect.eq(this, value)
infix fun <T : Temporal> DateTimeColumn.notEQ(value: T): Condition = PackageObject.dialect.notEQ(this, value)
infix fun <T : Temporal> DateTimeColumn.gt(value: T): Condition = PackageObject.dialect.gt(this, value)
infix fun <T : Temporal> DateTimeColumn.gte(value: T): Condition = PackageObject.dialect.gte(this, value)
infix fun <T : Temporal> DateTimeColumn.lt(value: T): Condition = PackageObject.dialect.lt(this, value)
infix fun <T : Temporal> DateTimeColumn.lte(value: T): Condition = PackageObject.dialect.lte(this, value)


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

fun Column<*>.isNull(): Condition = PackageObject.dialect.isNull(this)
fun Column<*>.notNull(): Condition = PackageObject.dialect.isNotNull(this)

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
