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
        val row = DataRow()

        columnInfoSetGenerator.generate(instance.metaData).forEach { item ->
            val label = item.label
            when (item.type) {
                Types.BOOLEAN -> row[label] =
                    {
                        val value = instance.getBoolean(item.index)
                        if (instance.wasNull()) {
                            null
                        }
                        else {
                            value
                        }
                    }

                Types.BIT, Types.TINYINT -> row[label] = {
                    val value = instance.getByte(item.index)
                    if (instance.wasNull()) {
                        null
                    }
                    else {
                        value
                    }
                }

                Types.SMALLINT -> row[label] = {
                    val value = instance.getShort(item.index)
                    if (instance.wasNull()) {
                        null
                    }
                    else {
                        value
                    }
                }

                Types.INTEGER -> row[label] = {
                    val value = instance.getInt(item.index)
                    if (instance.wasNull()) {
                        null
                    }
                    else {
                        value
                    }
                }

                Types.BIGINT -> row[label] = {
                    val value = instance.getLong(item.index)
                    if (instance.wasNull()) {
                        null
                    }
                    else {
                        value
                    }
                }

                Types.FLOAT, Types.REAL -> row[label] = {
                    val value = instance.getFloat(item.index)
                    if (instance.wasNull()) {
                        null
                    }
                    else {
                        value
                    }
                }

                Types.DOUBLE -> row[label] = {
                    val value = instance.getDouble(item.index)
                    if (instance.wasNull()) {
                        null
                    }
                    else {
                        value
                    }
                }

                Types.DATE -> row[label] = when (instance) {
                    is AbstractDialect.ResultSet -> instance.getLocalDate(item.index)
                    else -> instance.getDate(item.index)?.toLocalDate()
                }

                Types.TIME -> row[label] = when (instance) {
                    is AbstractDialect.ResultSet -> instance.getLocalTime(item.index)
                    else -> instance.getTime(item.index)?.toLocalTime()
                }

                Types.TIMESTAMP -> row[label] = when (instance) {
                    is AbstractDialect.ResultSet -> instance.getLocalDateTime(item.index)
                    else -> instance.getTimestamp(item.index)?.toLocalDateTime()
                }

                Types.TIMESTAMP_WITH_TIMEZONE -> row[label] = when (instance) {
                    is AbstractDialect.ResultSet -> instance.getOffsetDateTime(item.index)
                    else -> instance.getObject(item.index, OffsetDateTime::class.java)
                }

                Types.NUMERIC, Types.DECIMAL -> row[label] = instance.getBigDecimal(item.index)
                Types.BINARY -> row[label] = instance.getBytes(item.index)
                Types.CHAR, Types.VARCHAR, Types.LONGVARCHAR -> row[label] = instance.getString(item.index)
                Types.NCHAR, Types.NVARCHAR, Types.LONGNVARCHAR -> row[label] = instance.getNString(item.index)
                Types.CLOB -> row[label] = instance.getClob(item.index)
                Types.BLOB -> row[label] = instance.getBlob(item.index)
                Types.ARRAY -> row[label] = instance.getArray(item.index)
                Types.NULL -> row[label] = null
                else -> row[label] = instance.getObject(item.index)
            }
        }

        return row
    }

    override val from: Class<ResultSet>
        get() = ResultSet::class.java
    override val to: Class<DataRow>
        get() = DataRow::class.java


    companion object {
        private val columnInfoSetGenerator = Instep.make(ColumnInfoSetGenerator::class.java)

        init {
            val typeconvert = Instep.make(TypeConversion::class.java)
            typeconvert.getConverter(ResultSet::class.java, AssocArray::class.java) ?: typeconvert.register(ResultSetToDataRowConverter())
        }
    }
}