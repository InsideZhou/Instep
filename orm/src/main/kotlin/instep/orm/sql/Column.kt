package instep.orm.sql

abstract class Column<T : Column<T>>(val table: Table, val name: String) {
    var primary = false
    var nullable = true
    var default = ""

    @Suppress("UNCHECKED_CAST")
    open protected fun primary(): T {
        primary = true
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun nullable(nullable: Boolean): T {
        this.nullable = nullable
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun default(exp: String): T {
        default = exp
        return this as T
    }
}

abstract class NumberColumn<T : Column<T>>(table: Table, name: String) : Column<T>(table, name)

class IntegerColumn(table: Table, name: String, val type: IntegerColumnType) : NumberColumn<IntegerColumn>(table, name) {
    var autoIncrement = false

    fun autoIncrement(): IntegerColumn {
        autoIncrement = true
        return this
    }

    override public fun primary(): IntegerColumn {
        return super.primary()
    }
}

class BooleanColumn(table: Table, name: String) : Column<BooleanColumn>(table, name)
class StringColumn(table: Table, name: String, val type: StringColumnType, val length: Int = 256) : Column<StringColumn>(table, name) {
    override public fun primary(): StringColumn {
        return super.primary()
    }
}

class FloatingColumn(table: Table, name: String, val type: FloatingColumnType, val precision: Int = 0, val scale: Int = 0) : NumberColumn<FloatingColumn>(table, name)
class DateTimeColumn(table: Table, name: String, val type: DateTimeColumnType) : Column<DateTimeColumn>(table, name)
class BinaryColumn(table: Table, name: String, val type: BinaryColumnType, val length: Int = 0) : Column<BinaryColumn>(table, name)

enum class StringColumnType {
    Char, Varchar, Text
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
    Date, Time, DateTime, OffsetDateTime
}

enum class BinaryColumnType {
    Varying, BLOB
}
