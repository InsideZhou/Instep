package instep.dao.sql

import instep.Instep
import instep.UnexpectedCodeError
import java.io.InputStream
import java.math.BigDecimal
import java.sql.Blob
import java.sql.ResultSet
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime

/**
 * A table row filled with data.
 */
class TableRow {
    private val map = mutableMapOf<Column<*>, Any?>()

    operator fun get(column: Column<*>): Any? {
        return map[column]
    }

    operator fun get(column: BooleanColumn): Boolean {
        return map[column] as Boolean
    }

    operator fun get(column: IntegerColumn): Int {
        return map[column] as Int
    }

    @Suppress("unused")
    fun getLong(column: IntegerColumn): Long {
        return map[column] as Long
    }

    operator fun get(column: StringColumn): String {
        return map[column] as String
    }

    operator fun get(column: FloatingColumn): Double {
        val value = map[column]
        return when (value) {
            is Double -> value
            is Float -> value.toDouble()
            is BigDecimal -> value.toDouble()
            else -> throw UnsupportedOperationException("Converting $value to Double is not supported.")
        }
    }

    operator fun get(column: DateTimeColumn): OffsetDateTime? {
        val value = map[column]
        return when (value) {
            is LocalDateTime -> OffsetDateTime.of(value, OffsetDateTime.now().offset)
            is LocalDate -> OffsetDateTime.of(value, LocalTime.MIDNIGHT, OffsetDateTime.now().offset)
            is LocalTime -> OffsetDateTime.of(LocalDate.ofEpochDay(0), value, OffsetDateTime.now().offset)
            is OffsetDateTime -> value
            null -> null
            else -> throw UnsupportedOperationException("Converting $value to OffsetDateTime is not supported.")
        }
    }

    fun getLocalDateTime(column: DateTimeColumn): LocalDateTime? {
        val value = map[column]
        return when (value) {
            is LocalDateTime -> value
            is LocalDate -> LocalDateTime.of(value, LocalTime.MIDNIGHT)
            is LocalTime -> LocalDateTime.of(LocalDate.ofEpochDay(0), value)
            is OffsetDateTime -> value.toLocalDateTime()
            null -> null
            else -> throw UnsupportedOperationException("Converting $value to LocalDateTime is not supported.")
        }
    }

    operator fun get(column: BinaryColumn): InputStream {
        val value = map[column]
        return when (value) {
            is Blob -> value.binaryStream
            is ByteArray -> value.inputStream()
            else -> throw UnsupportedOperationException("Converting $value to InputStream is not supported.")
        }
    }

    operator fun set(column: Column<*>, value: Any?) {
        map[column] = value
    }
}

interface TableRowFactory {
    fun createInstance(table: Table, dialect: Dialect, resultSet: ResultSet): TableRow

    @Suppress("FoldInitializerAndIfToElvis")
    companion object : TableRowFactory {
        val resultSetDelegate = Instep.make(ResultSetDelegate::class.java)
        val columnInfoSetGenerator = Instep.make(ColumnInfoSetGenerator::class.java)

        override fun createInstance(table: Table, dialect: Dialect, resultSet: ResultSet): TableRow {
            val row = TableRow()
            val rs = resultSetDelegate.getDelegate(dialect, resultSet)

            val columnInfoSet = columnInfoSetGenerator.generate(rs.metaData)
            table.columns.forEach { col ->
                val info = columnInfoSet.find { it.label.equals(col.name, ignoreCase = true) }
                if (null == info) return@forEach

                row[col] = when (col) {
                    is BooleanColumn -> rs.getBoolean(info.index)

                    is IntegerColumn -> when (col.type) {
                        IntegerColumnType.Long -> rs.getLong(info.index)
                        else -> rs.getInt(info.index)
                    }

                    is StringColumn -> rs.getString(info.index)

                    is FloatingColumn -> when (col.type) {
                        FloatingColumnType.Float -> rs.getFloat(info.index)
                        FloatingColumnType.Double -> rs.getDouble(info.index)
                        else -> rs.getBigDecimal(info.index)
                    }

                    is DateTimeColumn -> when (col.type) {
                        DateTimeColumnType.Date -> rs.getLocalDate(info.index)
                        DateTimeColumnType.Time -> rs.getLocalTime(info.index)
                        DateTimeColumnType.DateTime -> rs.getLocalDateTime(info.index)
                        DateTimeColumnType.OffsetDateTime -> rs.getOffsetDateTime(info.index)
                        DateTimeColumnType.Instant -> rs.getInstant(info.index)
                    }

                    is BinaryColumn -> when (col.type) {
                        BinaryColumnType.BLOB -> rs.getBlob(info.index)
                        else -> rs.getBytes(info.index)
                    }

                    else -> throw UnexpectedCodeError()
                }
            }

            return row
        }
    }
}

