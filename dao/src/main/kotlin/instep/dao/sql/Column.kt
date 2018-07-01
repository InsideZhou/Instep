package instep.dao.sql

abstract class Column<T : Column<T>>(val name: String) {
    var primary = false
    var nullable = true
    var default = ""

    @Suppress("UNCHECKED_CAST")
    open protected fun primary(): T {
        primary = true
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun notnull(): T {
        this.nullable = false
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun default(exp: String): T {
        default = exp
        return this as T
    }
}

abstract class NumberColumn<T : Column<T>>(name: String) : Column<T>(name)

class IntegerColumn(name: String, val type: IntegerColumnType) : NumberColumn<IntegerColumn>(name) {
    var autoIncrement = false

    @Suppress("unused")
    fun autoIncrement(): IntegerColumn {
        autoIncrement = true
        return this
    }

    override public fun primary(): IntegerColumn {
        return super.primary()
    }
}

class BooleanColumn(name: String) : Column<BooleanColumn>(name)
class StringColumn(name: String, val type: StringColumnType, val length: Int = 256) : Column<StringColumn>(name) {
    override public fun primary(): StringColumn {
        return super.primary()
    }
}

class FloatingColumn(name: String, val type: FloatingColumnType, val precision: Int = 0, val scale: Int = 0) : NumberColumn<FloatingColumn>(name)
class DateTimeColumn(name: String, val type: DateTimeColumnType) : Column<DateTimeColumn>(name)
class BinaryColumn(name: String, val type: BinaryColumnType, val length: Int = 0) : Column<BinaryColumn>(name)

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
    Date, Time, DateTime, OffsetDateTime
}

enum class BinaryColumnType {
    Varying, BLOB
}
