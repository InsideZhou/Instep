package instep.dao.sql

import java.time.temporal.Temporal


infix fun BooleanColumn.eq(value: Boolean): Condition = Condition.eq(name, value)


infix fun StringColumn.eq(value: String): Condition = Condition.eq(name, value)

infix fun StringColumn.startsWith(value: String): Condition = Condition.startsWith(name, value)

infix fun StringColumn.endsWith(value: String): Condition = Condition.endsWith(name, value)

infix fun StringColumn.contains(value: String): Condition = Condition.contains(name, value)


infix fun <T : Number> NumberColumn<*>.eq(value: T): Condition = Condition.eq(name, value)

infix fun <T : Number> NumberColumn<*>.gt(value: T): Condition = Condition.gt(name, value)

infix fun <T : Number> NumberColumn<*>.gte(value: T): Condition = Condition.gte(name, value)

infix fun <T : Number> NumberColumn<*>.lt(value: T): Condition = Condition.lt(name, value)

infix fun <T : Number> NumberColumn<*>.lte(value: T): Condition = Condition.lte(name, value)


infix fun <T : Temporal> DateTimeColumn.eq(value: T): Condition = Condition.eq(name, value)

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


fun Column<*>.asc(nullFirst: Boolean = false): OrderBy {
    val column = this

    return object : OrderBy {
        override val column: Column<*> = column
        override val descending: Boolean = false
        override val nullFirst: Boolean = nullFirst
    }
}

fun Column<*>.desc(nullFirst: Boolean = false): OrderBy {
    val column = this

    return object : OrderBy {
        override val column: Column<*> = column
        override val descending: Boolean = true
        override val nullFirst: Boolean = nullFirst
    }
}