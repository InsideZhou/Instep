package instep.dao.sql

@Suppress("UNCHECKED_CAST")
abstract class Column<T : Column<T>>(val name: String, val table: Table) {
    var primary = false
    var nullable = true
    var unique = false
    var default = ""
    var comment = ""

    fun primary(): T {
        primary = true
        return this as T
    }

    fun notnull(): T {
        this.nullable = false
        return this as T
    }

    fun unique(): T {
        this.unique = true
        return this as T
    }

    fun default(exp: String): T {
        default = exp
        return this as T
    }

    fun comment(txt: String): T {
        comment = txt
        return this as T
    }

    /**
     * for java interop.
     */
    fun defaultValue(exp: String): T = default(exp)
}

abstract class NumberColumn<T : Column<T>>(name: String, table: Table) : Column<T>(name, table)

class IntegerColumn(name: String, table: Table, val type: IntegerColumnType) : NumberColumn<IntegerColumn>(name, table) {
    var autoIncrement = false

    @Suppress("unused")
    fun autoIncrement(): IntegerColumn {
        autoIncrement = true
        return this
    }
}

class BooleanColumn(name: String, table: Table) : Column<BooleanColumn>(name, table)
class StringColumn(name: String, table: Table, val type: StringColumnType, val length: Int = 256) : Column<StringColumn>(name, table)
class FloatingColumn(name: String, table: Table, val type: FloatingColumnType, val precision: Int = 0, val scale: Int = 0) : NumberColumn<FloatingColumn>(name, table)
class DateTimeColumn(name: String, table: Table, val type: DateTimeColumnType) : Column<DateTimeColumn>(name, table)
class BinaryColumn(name: String, table: Table, val type: BinaryColumnType, val length: Int = 0) : Column<BinaryColumn>(name, table)
class ArbitraryColumn(name: String, table: Table, val definition: String) : Column<ArbitraryColumn>(name, table)

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
