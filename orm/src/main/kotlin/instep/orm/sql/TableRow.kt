package instep.orm.sql

import instep.Instep
import instep.UnexpectedTouch
import instep.orm.sql.impl.Helper
import instep.servicecontainer.ServiceNotFoundException
import java.sql.ResultSet
import java.time.OffsetDateTime

/**
 * A table row filled with data.
 */
class TableRow<out T : Table>(val table: T) {
    private val map = mutableMapOf<Column<*>, Any?>()

    @Suppress("unchecked_cast")
    operator fun <Result : Any> get(column: Column<*>): Result? {
        return map[column] as? Result?
    }

    operator fun get(column: IntegerColumn): Int {
        return map[column] as Int
    }

    operator fun get(column: StringColumn): String {
        return map[column] as String
    }

    operator fun set(column: Column<*>, value: Any?) {
        map[column] = value
    }

    companion object {
        init {
            try {
                Instep.make(TableRow.Companion::class.java)
            }
            catch(e: ServiceNotFoundException) {
                Instep.bind(TableRow.Companion::class.java, TableRow.Companion)
            }
        }

        fun <T : Table> createInstance(table: T, rs: ResultSet): TableRow<T> {
            val row = TableRow(table)

            val columnInfoSet = Helper.generateColumnInfoSet(rs.metaData)
            table.columns().forEach { col ->
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
