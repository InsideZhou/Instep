package instep.orm.sql

abstract class Column<T : Column<T>>(val name: String) {
    var primary = false
    var nullable = true
    var default = ""

    @Suppress("UNCHECKED_CAST")
    open fun primary(): T {
        primary = true
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    open fun nullable(nullable: Boolean): T {
        this.nullable = nullable
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    open fun default(exp: String): T {
        default = exp
        return this as T
    }
}

abstract class NumberColumn<T : Column<T>>(name: String) : Column<T>(name)

class IntegerColumn(name: String, val type: IntegerColumnType) : NumberColumn<IntegerColumn>(name) {
    var autoIncrement = false

    fun autoIncrement(): IntegerColumn {
        autoIncrement = true
        return this
    }
}

class BooleanColumn(name: String) : Column<BooleanColumn>(name)
class StringColumn(name: String, val type: StringColumnType, val length: Int = 256) : Column<StringColumn>(name)
class FloatingColumn(name: String, val type: FloatingColumnType, val precision: Int = 0, val scale: Int = 0) : NumberColumn<FloatingColumn>(name)
class DateTimeColumn(name: String, val type: DateTimeColumnType) : Column<DateTimeColumn>(name)
class BinaryColumn(name: String, val type: BinaryColumnType, val length: Int = 0) : Column<BinaryColumn>(name)

enum class StringColumnType {
    Char, Varchar, Text
}

enum class IntegerColumnType {
    Tiny, Small, Int, Long
}

enum class FloatingColumnType {
    Float, Double, Numeric
}

enum class DateTimeColumnType {
    Date, Time, DateTime, OffsetDateTime
}

enum class BinaryColumnType {
    Varying, BLOB
}
