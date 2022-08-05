package instep.dao.sql

import instep.Instep
import instep.collection.AssocArray
import instep.dao.sql.dialect.AbstractDialect
import instep.typeconversion.Converter
import instep.typeconversion.TypeConversion
import java.sql.ResultSet
import java.sql.Types
import java.time.OffsetDateTime

@Suppress("unused")
open class ResultSetToDataRowConverter : Converter<ResultSet, DataRow> {
    override fun <T : ResultSet> convert(instance: T): DataRow {
        val rowMeta = instance.metaData
        val row = DataRow()

        (1..instance.metaData.columnCount).map { index ->
            val label = rowMeta.getColumnLabel(index)
            val type = rowMeta.getColumnType(index)
            val typeName = rowMeta.getColumnTypeName(index)

            when (type) {
                Types.BOOLEAN -> {
                    val value = instance.getBoolean(index)
                    row[label] = if (instance.wasNull()) {
                        null
                    }
                    else {
                        value
                    }
                }

                Types.BIT, Types.TINYINT -> {
                    val value = if (typeName.contains("bool", true)) {
                        instance.getBoolean(index)
                    }
                    else {
                        instance.getByte(index)
                    }

                    row[label] = if (instance.wasNull()) {
                        null
                    }
                    else {
                        value
                    }
                }

                Types.SMALLINT -> {
                    val value = instance.getShort(index)
                    row[label] = if (instance.wasNull()) {
                        null
                    }
                    else {
                        value
                    }
                }

                Types.INTEGER -> {
                    val value = instance.getInt(index)
                    row[label] =
                        if (instance.wasNull()) {
                            null
                        }
                        else {
                            value
                        }
                }

                Types.BIGINT -> {
                    val value = instance.getLong(index)
                    row[label] = if (instance.wasNull()) {
                        null
                    }
                    else {
                        value
                    }
                }

                Types.FLOAT, Types.REAL -> {
                    val value = instance.getFloat(index)
                    row[label] = if (instance.wasNull()) {
                        null
                    }
                    else {
                        value
                    }
                }

                Types.DOUBLE -> {
                    val value = instance.getDouble(index)
                    row[label] = if (instance.wasNull()) {
                        null
                    }
                    else {
                        value
                    }
                }

                Types.DATE -> row[label] = when (instance) {
                    is AbstractDialect.ResultSet -> instance.getLocalDate(index)
                    else -> instance.getDate(index)?.toLocalDate()
                }

                Types.TIME -> row[label] = when (instance) {
                    is AbstractDialect.ResultSet -> instance.getLocalTime(index)
                    else -> instance.getTime(index)?.toLocalTime()
                }

                Types.TIMESTAMP -> row[label] = when (instance) {
                    is AbstractDialect.ResultSet -> instance.getLocalDateTime(index)
                    else -> instance.getTimestamp(index)?.toLocalDateTime()
                }

                Types.TIMESTAMP_WITH_TIMEZONE -> row[label] = when (instance) {
                    is AbstractDialect.ResultSet -> instance.getOffsetDateTime(index)
                    else -> instance.getObject(index, OffsetDateTime::class.java)
                }

                Types.NUMERIC, Types.DECIMAL -> row[label] = instance.getBigDecimal(index)
                Types.BINARY -> row[label] = instance.getBytes(index)
                Types.CHAR, Types.VARCHAR, Types.LONGVARCHAR -> row[label] = instance.getString(index)
                Types.NCHAR, Types.NVARCHAR, Types.LONGNVARCHAR -> row[label] = instance.getNString(index)
                Types.CLOB -> row[label] = instance.getClob(index)
                Types.BLOB -> row[label] = instance.getBlob(index)
                Types.ARRAY -> row[label] = instance.getArray(index)
                Types.NULL -> row[label] = null
                else -> row[label] = instance.getObject(index)
            }
        }

        return row
    }

    override val from: Class<ResultSet>
        get() = ResultSet::class.java
    override val to: Class<DataRow>
        get() = DataRow::class.java


    companion object {
        init {
            val typeconvert = Instep.make(TypeConversion::class.java)
            typeconvert.getConverter(ResultSet::class.java, AssocArray::class.java) ?: typeconvert.register(ResultSetToDataRowConverter())
        }
    }
}