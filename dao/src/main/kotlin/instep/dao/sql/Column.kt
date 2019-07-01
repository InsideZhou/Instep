package instep.dao.sql

@Suppress("UNCHECKED_CAST")
abstract class Column<T : Column<T>>(val name: String) {
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

abstract class NumberColumn<T : Column<T>>(name: String) : Column<T>(name)

class IntegerColumn(name: String, val type: IntegerColumnType) : NumberColumn<IntegerColumn>(name) {
    var autoIncrement = false

    @Suppress("unused")
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
