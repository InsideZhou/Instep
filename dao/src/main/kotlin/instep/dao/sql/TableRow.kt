package instep.dao.sql

import instep.ImpossibleBranch
import instep.Instep
import java.io.InputStream
import java.math.BigDecimal
import java.sql.Blob
import java.sql.ResultSet
import java.time.*

@Suppress("unused")
/**
 * A table row filled with data.
 */
class TableRow {
    private val offset = OffsetDateTime.now().offset

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

    fun getOffsetDateTime(column: DateTimeColumn): OffsetDateTime? {
        return map[column]?.let {
            when (it) {
                is LocalDateTime -> OffsetDateTime.of(it, offset)
                is Instant -> OffsetDateTime.ofInstant(it, offset)
                is OffsetDateTime -> it
                else -> throw UnsupportedOperationException("Converting $it to OffsetDateTime is not supported.")
            }
        }
    }

    fun getLocalDateTime(column: DateTimeColumn): LocalDateTime? {
        return map[column]?.let {
            when (it) {
                is LocalDateTime -> it
                is Instant -> LocalDateTime.ofInstant(it, offset)
                is OffsetDateTime -> it.toLocalDateTime()
                else -> throw UnsupportedOperationException("Converting $it to LocalDateTime is not supported.")
            }
        }
    }

    fun getLocalDate(column: DateTimeColumn): LocalDate? {
        return map[column]?.let {
            when (it) {
                is LocalDateTime -> it.toLocalDate()
                is Instant -> LocalDateTime.ofInstant(it, offset).toLocalDate()
                is OffsetDateTime -> it.toLocalDate()
                else -> throw UnsupportedOperationException("Converting $it to LocalDate is not supported.")
            }
        }
    }

    fun getLocalTime(column: DateTimeColumn): LocalTime? {
        return map[column]?.let {
            when (it) {
                is LocalDateTime -> it.toLocalTime()
                is Instant -> LocalDateTime.ofInstant(it, offset).toLocalTime()
                is OffsetDateTime -> it.toLocalTime()
                else -> throw UnsupportedOperationException("Converting $it to LocalTime is not supported.")
            }
        }
    }

    operator fun get(column: DateTimeColumn): Instant? {
        return map[column]?.let {
            when (it) {
                is LocalDateTime -> it.toInstant(offset)
                is Instant -> it
                is OffsetDateTime -> it.toInstant()
                else -> throw UnsupportedOperationException("Converting $it to Instant is not supported.")
            }
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

                    else -> throw ImpossibleBranch()
                }
            }

            return row
        }
    }
}

