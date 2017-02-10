package instep.orm.sql

import instep.UnexpectedTouch
import instep.orm.sql.impl.Helper
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

    operator fun get(column: DateTimeColumn): LocalDateTime {
        val value = map[column]
        return when (value) {
            is LocalDateTime -> value
            is LocalDate -> LocalDateTime.from(value)
            is LocalTime -> LocalDateTime.from(value)
            is OffsetDateTime -> LocalDateTime.from(value)
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

    companion object : TableRowFactory {
        override fun createInstance(table: Table, rs: ResultSet): TableRow {
            val row = TableRow()

            val columnInfoSet = Helper.generateColumnInfoSet(rs.metaData)
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
                        DateTimeColumnType.Date -> rs.getDate(info.index).toLocalDate()
                        DateTimeColumnType.Time -> rs.getTime(info.index).toLocalTime()
                        DateTimeColumnType.DateTime -> rs.getTimestamp(info.index).toLocalDateTime()
                        DateTimeColumnType.OffsetDateTime -> rs.getObject(info.index) as? OffsetDateTime
                    }

                    is BinaryColumn -> when (col.type) {
                        BinaryColumnType.BLOB -> rs.getBlob(info.index)
                        else -> rs.getBytes(info.index)
                    }

                    else -> throw UnexpectedTouch()
                }
            }

            return row
        }
    }
}

interface TableRowFactory {
    fun createInstance(table: Table, rs: ResultSet): TableRow
}

