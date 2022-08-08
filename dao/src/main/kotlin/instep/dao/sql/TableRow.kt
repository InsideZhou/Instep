package instep.dao.sql

import instep.ImpossibleBranch
import instep.Instep
import instep.util.snakeToCamelCase
import java.io.InputStream
import java.math.BigDecimal
import java.sql.Blob
import java.sql.ResultSet
import java.time.*

@Suppress("unused", "MemberVisibilityCanBePrivate")
/**
 * A table row filled with data.
 */
open class TableRow {
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

    operator fun get(column: FloatingColumn): Double {
        return when (val value = map[column]) {
            is Double -> value
            is Float -> value.toDouble()
            is BigDecimal -> value.toDouble()
            else -> throw UnsupportedOperationException("Converting $value to Double is not supported.")
        }
    }

    operator fun get(column: StringColumn): String {
        return map[column] as String
    }

    operator fun get(column: DateTimeColumn): Instant {
        return map[column].let {
            when (it) {
                is LocalDateTime -> it.toInstant(offset)
                is Instant -> it
                is OffsetDateTime -> it.toInstant()
                else -> throw UnsupportedOperationException("Converting $it to Instant is not supported.")
            }
        }
    }

    operator fun get(column: BinaryColumn): InputStream {
        return when (val value = map[column]) {
            is Blob -> value.binaryStream
            is ByteArray -> value.inputStream()
            else -> throw UnsupportedOperationException("Converting $value to InputStream is not supported.")
        }
    }

    fun getLong(column: IntegerColumn): Long {
        return map[column] as Long
    }

    fun getFloat(column: FloatingColumn): Float {
        return map[column] as Float
    }

    fun getBigDecimal(column: FloatingColumn): BigDecimal {
        return map[column] as BigDecimal
    }

    fun getLocalDateTime(column: DateTimeColumn): LocalDateTime {
        return map[column].let {
            when (it) {
                is LocalDateTime -> it
                is Instant -> LocalDateTime.ofInstant(it, offset)
                is OffsetDateTime -> it.toLocalDateTime()
                else -> throw UnsupportedOperationException("Converting $it to LocalDateTime is not supported.")
            }
        }
    }

    fun getOffsetDateTime(column: DateTimeColumn): OffsetDateTime {
        return map[column].let {
            when (it) {
                is LocalDateTime -> OffsetDateTime.of(it, offset)
                is Instant -> OffsetDateTime.ofInstant(it, offset)
                is OffsetDateTime -> it
                else -> throw UnsupportedOperationException("Converting $it to OffsetDateTime is not supported.")
            }
        }
    }

    fun getLocalDate(column: DateTimeColumn): LocalDate {
        return map[column].let {
            when (it) {
                is LocalDate -> it
                is LocalDateTime -> it.toLocalDate()
                is Instant -> LocalDateTime.ofInstant(it, offset).toLocalDate()
                is OffsetDateTime -> it.toLocalDate()
                else -> throw UnsupportedOperationException("Converting $it to LocalDate is not supported.")
            }
        }
    }

    fun getLocalTime(column: DateTimeColumn): LocalTime {
        return map[column].let {
            when (it) {
                is LocalTime -> it
                is LocalDateTime -> it.toLocalTime()
                is Instant -> LocalDateTime.ofInstant(it, offset).toLocalTime()
                is OffsetDateTime -> it.toLocalTime()
                else -> throw UnsupportedOperationException("Converting $it to LocalTime is not supported.")
            }
        }
    }

    fun getNullable(column: BooleanColumn): Boolean? {
        if (null != map[column]) {
            return get(column)
        }

        return null
    }

    fun getNullable(column: IntegerColumn): Int? {
        if (null != map[column]) {
            return get(column)
        }

        return null
    }

    fun getNullableLong(column: IntegerColumn): Long? {
        if (null != map[column]) {
            return getLong(column)
        }

        return null
    }

    fun getNullable(column: FloatingColumn): Double? {
        if (null != map[column]) {
            return get(column)
        }

        return null
    }

    fun getNullableFloat(column: FloatingColumn): Float? {
        if (null != map[column]) {
            return getFloat(column)
        }

        return null
    }

    fun getNullableBigDecimal(column: FloatingColumn): BigDecimal? {
        if (null != map[column]) {
            return getBigDecimal(column)
        }

        return null
    }

    fun getNullable(column: StringColumn): String? {
        if (null != map[column]) {
            return get(column)
        }

        return null
    }

    fun getNullable(column: DateTimeColumn): Instant? {
        if (null != map[column]) {
            return get(column)
        }

        return null
    }

    fun getNullableLocalDateTime(column: DateTimeColumn): LocalDateTime? {
        if (null != map[column]) {
            return getLocalDateTime(column)
        }

        return null
    }

    fun getNullableOffsetDateTime(column: DateTimeColumn): OffsetDateTime? {
        if (null != map[column]) {
            return getOffsetDateTime(column)
        }

        return null
    }

    fun getNullableLocalDate(column: DateTimeColumn): LocalDate? {
        if (null != map[column]) {
            return getLocalDate(column)
        }

        return null
    }

    fun getNullableLocalTime(column: DateTimeColumn): LocalTime? {
        if (null != map[column]) {
            return getLocalTime(column)
        }

        return null
    }

    fun getNullable(column: BinaryColumn): InputStream? {
        if (null != map[column]) {
            return get(column)
        }

        return null
    }

    operator fun set(column: Column<*>, value: Any?) {
        map[column] = value
    }

    fun <R : Any> fillUp(cls: Class<R>): R {
        val instance = cls.getDeclaredConstructor().newInstance()
        fillUp(instance)
        return instance
    }

    fun fillUp(pojo: Any) {
        val targetMutableProperties = Instep.reflect(pojo).getMutablePropertiesUntil(Any::class.java)
        targetMutableProperties.forEach { p ->
            val setterType = p.setter.parameterTypes.first()

            map.filter { entry ->
                entry.value != null && p.field.name.equals(entry.key.name.snakeToCamelCase(), true) && setterType.isAssignableFrom(entry.value!!.javaClass)
            }.firstNotNullOfOrNull { entry ->
                entry.value
            }?.let {
                p.setter.invoke(pojo, it)
            }
        }
    }

    companion object {
        fun <T : Table> createInstance(table: T, resultSet: ResultSet): TableRow {
            val resultSetDelegate = Instep.make(ResultSetDelegate::class.java)
            val columnInfoSetGenerator = Instep.make(ColumnInfoSetGenerator::class.java)
            val row = TableRow()
            val rs = resultSetDelegate.getDelegate(table.dialect, resultSet)

            val columnInfoSet = columnInfoSetGenerator.generate(rs.metaData)
            table.columns.forEach { col ->
                val info = columnInfoSet.find { it.label.equals(col.name, ignoreCase = true) } ?: return@forEach

                row[col] = when (col) {
                    is BooleanColumn -> {
                        val b = rs.getBoolean(info.index)
                        if (rs.wasNull()) {
                            null
                        }
                        else {
                            b
                        }
                    }

                    is IntegerColumn -> when (col.type) {
                        IntegerColumnType.Long -> {
                            val num = rs.getLong(info.index)
                            if (0L == num && rs.wasNull()) {
                                null
                            }
                            else {
                                num
                            }
                        }
                        else -> {
                            val num = rs.getInt(info.index)
                            if (0 == num && rs.wasNull()) {
                                null
                            }
                            else {
                                num
                            }
                        }
                    }

                    is StringColumn -> rs.getString(info.index)

                    is FloatingColumn -> when (col.type) {
                        FloatingColumnType.Float -> {
                            val num = rs.getFloat(info.index)
                            if (0f == num && rs.wasNull()) {
                                null
                            }
                            else {
                                num
                            }
                        }
                        FloatingColumnType.Double -> {
                            val num = rs.getDouble(info.index)
                            if (0.0 == num && rs.wasNull()) {
                                null
                            }
                            else {
                                num
                            }
                        }
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

                    is ArbitraryColumn -> {
                        rs.getObject(info.index)
                    }

                    else -> throw ImpossibleBranch()
                }
            }

            return row
        }
    }
}
