package instep.dao.sql

@Suppress("UNCHECKED_CAST")
abstract class Column<T : Column<T>>(val name: String, val table: Table) {
    val qualifiedName = "${table.tableName}.${name}"

    var primary = false
    var nullable = true
    var unique = false
    var default = ""
    var comment = ""

    open fun primary(): T {
        primary = true
        return this as T
    }

    open fun notnull(): T {
        this.nullable = false
        return this as T
    }

    open fun unique(): T {
        this.unique = true
        return this as T
    }

    open fun defaultValue(exp: String): T {
        default = exp
        return this as T
    }

    open fun comment(txt: String): T {
        comment = txt
        return this as T
    }
}

abstract class NumberColumn<T : Column<T>>(name: String, table: Table) : Column<T>(name, table)

open class IntegerColumn(name: String, table: Table, val type: IntegerColumnType) : NumberColumn<IntegerColumn>(name, table) {
    var autoIncrement = false

    open fun autoIncrement(): IntegerColumn {
        autoIncrement = true
        return this
    }
}

open class BooleanColumn(name: String, table: Table) : Column<BooleanColumn>(name, table)
open class StringColumn(name: String, table: Table, val type: StringColumnType, val length: Int = 256) : Column<StringColumn>(name, table)
open class FloatingColumn(name: String, table: Table, val type: FloatingColumnType, val precision: Int = 0, val scale: Int = 0) :
    NumberColumn<FloatingColumn>(name, table)

open class DateTimeColumn(name: String, table: Table, val type: DateTimeColumnType) : Column<DateTimeColumn>(name, table)
open class BinaryColumn(name: String, table: Table, val type: BinaryColumnType, val length: Int = 0) : Column<BinaryColumn>(name, table)
open class ArbitraryColumn(name: String, table: Table, val definition: String) : Column<ArbitraryColumn>(name, table)

enum class StringColumnType {
    Char, Varchar, Text, JSON, UUID
}

enum class IntegerColumnType {
    Tiny, Small, Int, Long
}

/**
 * @remark Float&Double should ignore precision and scale indication.
 */
enum class FloatingColumnType {
    Float, Double, Numeric
}

enum class DateTimeColumnType {
    Date, Time, DateTime, OffsetDateTime, Instant
}

enum class BinaryColumnType {
    Varying, BLOB
}