package instep.dao.sql.dialect

import instep.InstepLogger
import instep.UnexpectedCodeError
import instep.dao.PlaceHolder
import instep.dao.PlaceHolderRemainingException
import instep.dao.Plan
import instep.dao.sql.*
import java.io.InputStream
import java.io.Reader
import java.math.BigDecimal
import java.math.BigInteger
import java.sql.*
import java.time.*
import java.util.*

abstract class AbstractDialect : Dialect {
    open class ResultSet(private val rs: java.sql.ResultSet) : java.sql.ResultSet by rs {
        open fun getInstant(index: Int): Instant? {
            return rs.getTimestamp(index)?.let(Timestamp::toInstant)
        }

        open fun getInstant(label: String): Instant? {
            return rs.getTimestamp(label)?.let(Timestamp::toInstant)
        }

        open fun getLocalDate(index: Int): LocalDate? {
            return rs.getDate(index)?.let(java.sql.Date::toLocalDate)
        }

        open fun getLocalDate(label: String): LocalDate? {
            return rs.getDate(label)?.let(java.sql.Date::toLocalDate)
        }

        open fun getLocalTime(index: Int): LocalTime? {
            return rs.getTime(index)?.let(Time::toLocalTime)
        }

        open fun getLocalTime(label: String): LocalTime? {
            return rs.getTime(label)?.let(Time::toLocalTime)
        }

        open fun getLocalDateTime(index: Int): LocalDateTime? {
            return rs.getTimestamp(index)?.let(Timestamp::toLocalDateTime)
        }

        open fun getLocalDateTime(label: String): LocalDateTime? {
            return rs.getTimestamp(label)?.let(Timestamp::toLocalDateTime)
        }

        open fun getOffsetDateTime(index: Int): OffsetDateTime? {
            return rs.getObject(index, OffsetDateTime::class.java)
        }

        open fun getOffsetDateTime(label: String): OffsetDateTime? {
            return rs.getObject(label, OffsetDateTime::class.java)
        }
    }

    override val defaultInsertValue: String = "NULL"
    override val placeholderForJSONType: String = "?"
    override val placeholderForUUIDType: String = "?"

    override val pagination: Pagination = StandardPagination()
    override val isOffsetDateTimeSupported: Boolean = true

    override fun createTable(tableName: String, columns: List<Column<*>>): Plan<*> {
        val ddl = "CREATE TABLE IF NOT EXISTS $tableName (\n"

        if (columns.isEmpty()) {
            InstepLogger.warning({ "Table $tableName has no columns." }, this.javaClass.name)
        }

        return InstepSQL.plan(ddl + definitionForColumns(*columns.toTypedArray()) + "\n)")
    }

    override fun addColumn(tableName: String, column: Column<*>): Plan<*> {
        val columnDefinition = definitionForColumns(column)
        return InstepSQL.plan("ALTER TABLE $tableName ADD COLUMN $columnDefinition")
    }

    override fun setParameterForPreparedStatement(stmt: PreparedStatement, index: Int, value: Any?) {
        when (value) {
            is PlaceHolder -> throw PlaceHolderRemainingException(value)
            is Boolean -> stmt.setBoolean(index, value)
            is Char -> stmt.setString(index, value.toString())
            is String -> stmt.setString(index, value)
            is Byte -> stmt.setByte(index, value)
            is Short -> stmt.setShort(index, value)
            is Int -> stmt.setInt(index, value)
            is Long -> stmt.setLong(index, value)
            is BigInteger -> stmt.setLong(index, value.toLong())
            is BigDecimal -> stmt.setBigDecimal(index, value)
            is Float -> stmt.setFloat(index, value)
            is Double -> stmt.setDouble(index, value)
            is ByteArray -> stmt.setBytes(index, value)
            is Instant -> stmt.setTimestamp(index, java.sql.Timestamp.from(value))
            is LocalDate -> stmt.setDate(index, java.sql.Date.valueOf(value))
            is LocalTime -> stmt.setTime(index, java.sql.Time.valueOf(value))
            is LocalDateTime -> stmt.setTimestamp(index, java.sql.Timestamp.valueOf(value))
            is OffsetDateTime -> value.toZonedDateTime().let {
                stmt.setTimestamp(index, java.sql.Timestamp.from(value.toInstant()), Calendar.getInstance(TimeZone.getTimeZone(it.zone)))
            }
            is InputStream -> stmt.setBinaryStream(index, value)
            is Reader -> stmt.setCharacterStream(index, value)
            else -> stmt.setObject(index, value)
        }
    }

    protected open fun definitionForBooleanColumn(column: BooleanColumn): String = "BOOLEAN"

    abstract protected fun definitionForAutoIncrementColumn(column: IntegerColumn): String

    protected open fun definitionForJSONColumn(column: StringColumn): String {
        throw NotImplementedError("JSON Column is not supported by ${this.javaClass.simpleName}")
    }

    protected open fun definitionForUUIDColumn(column: StringColumn): String {
        throw NotImplementedError("UUID Column is not supported by ${this.javaClass.simpleName}")
    }

    protected open fun definitionForIntegerColumn(column: IntegerColumn): String {
        return when (column.type) {
            IntegerColumnType.Tiny -> "TINYINT"
            IntegerColumnType.Small -> "SMALLINT"
            IntegerColumnType.Int -> "INTEGER"
            IntegerColumnType.Long -> "BIGINT"
        }
    }

    protected open fun definitionForStringColumn(column: StringColumn): String {
        return when (column.type) {
            StringColumnType.Char -> "CHAR(${column.length})"
            StringColumnType.Varchar -> "VARCHAR(${column.length})"
            StringColumnType.Text -> if (column.length > 0) "TEXT(${column.length})" else "TEXT"
            StringColumnType.JSON -> definitionForJSONColumn(column)
            StringColumnType.UUID -> definitionForUUIDColumn(column)
        }
    }

    protected open fun definitionForFloatingColumn(column: FloatingColumn): String {
        return when (column.type) {
            FloatingColumnType.Float -> "REAL"
            FloatingColumnType.Double -> "DOUBLE"
            FloatingColumnType.Numeric -> "NUMERIC(${column.precision},${column.scale})"
        }
    }

    protected open fun definitionForDateTimeColumn(column: DateTimeColumn): String {
        return when (column.type) {
            DateTimeColumnType.Date -> "DATE"
            DateTimeColumnType.Time -> "TIME"
            DateTimeColumnType.DateTime -> "TIMESTAMP"
            DateTimeColumnType.OffsetDateTime -> "TIMESTAMP WITH TIME ZONE"
        }
    }

    protected open fun definitionForBinaryColumn(column: BinaryColumn): String {
        return when (column.type) {
            BinaryColumnType.Varying -> if (column.length > 0) "BINARY(${column.length})" else "BINARY"
            BinaryColumnType.BLOB -> if (column.length > 0) "BLOB(${column.length})" else "BLOB"
        }
    }

    protected open fun definitionForColumn(column: Column<*>): String {
        var txt = "\t${column.name}"

        txt += " " + when (column) {
            is BooleanColumn -> definitionForBooleanColumn(column)
            is StringColumn -> definitionForStringColumn(column)
            is IntegerColumn ->
                if (column.autoIncrement) {
                    definitionForAutoIncrementColumn(column)
                }
                else {
                    definitionForIntegerColumn(column)
                }
            is FloatingColumn -> definitionForFloatingColumn(column)
            is DateTimeColumn -> definitionForDateTimeColumn(column)
            is BinaryColumn -> definitionForBinaryColumn(column)
            else -> throw UnexpectedCodeError()
        }

        if (!column.nullable) {
            txt += " NOT NULL"
        }

        if (column.default.isNotBlank()) {
            txt += " DEFAULT ${column.default}"
        }

        if (column.primary) {
            txt += " PRIMARY KEY"
        }

        return txt
    }

    private fun definitionForColumns(vararg columns: Column<*>): String {
        return columns.map { definitionForColumn(it) }.joinToString(",\n")
    }
}