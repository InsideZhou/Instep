package instep.dao.sql.impl

import instep.Instep
import instep.collection.AssocArray
import instep.dao.sql.ColumnInfoSetGenerator
import instep.dao.sql.dialect.AbstractDialect
import instep.typeconversion.Converter
import instep.typeconversion.TypeConversion
import java.sql.ResultSet
import java.sql.Types
import java.time.OffsetDateTime

@Suppress("unused")
open class ResultSetToAssocArrayConverter : Converter<ResultSet, AssocArray> {
    override fun <T : ResultSet> convert(instance: T): AssocArray {
        val array = AssocArray()

        columnInfoSetGenerator.generate(instance.metaData).forEach { item ->
            val label = item.label
            when (item.type) {
                Types.BOOLEAN -> array[label] =
                    {
                        val value = instance.getBoolean(item.index)
                        if (instance.wasNull()) {
                            null
                        }
                        else {
                            value
                        }
                    }

                Types.BIT, Types.TINYINT -> array[label] = {
                    val value = instance.getByte(item.index)
                    if (instance.wasNull()) {
                        null
                    }
                    else {
                        value
                    }
                }

                Types.SMALLINT -> array[label] = {
                    val value = instance.getShort(item.index)
                    if (instance.wasNull()) {
                        null
                    }
                    else {
                        value
                    }
                }

                Types.INTEGER -> array[label] = {
                    val value = instance.getInt(item.index)
                    if (instance.wasNull()) {
                        null
                    }
                    else {
                        value
                    }
                }

                Types.BIGINT -> array[label] = {
                    val value = instance.getLong(item.index)
                    if (instance.wasNull()) {
                        null
                    }
                    else {
                        value
                    }
                }

                Types.FLOAT, Types.REAL -> array[label] = {
                    val value = instance.getFloat(item.index)
                    if (instance.wasNull()) {
                        null
                    }
                    else {
                        value
                    }
                }

                Types.DOUBLE -> array[label] = {
                    val value = instance.getDouble(item.index)
                    if (instance.wasNull()) {
                        null
                    }
                    else {
                        value
                    }
                }

                Types.DATE -> array[label] = when (instance) {
                    is AbstractDialect.ResultSet -> instance.getLocalDate(item.index)
                    else -> instance.getDate(item.index)?.toLocalDate()
                }

                Types.TIME -> array[label] = when (instance) {
                    is AbstractDialect.ResultSet -> instance.getLocalTime(item.index)
                    else -> instance.getTime(item.index)?.toLocalTime()
                }

                Types.TIMESTAMP -> array[label] = when (instance) {
                    is AbstractDialect.ResultSet -> instance.getLocalDateTime(item.index)
                    else -> instance.getTimestamp(item.index)?.toLocalDateTime()
                }

                Types.TIMESTAMP_WITH_TIMEZONE -> array[label] = when (instance) {
                    is AbstractDialect.ResultSet -> instance.getOffsetDateTime(item.index)
                    else -> instance.getObject(item.index, OffsetDateTime::class.java)
                }

                Types.NUMERIC, Types.DECIMAL -> array[label] = instance.getBigDecimal(item.index)
                Types.BINARY -> array[label] = instance.getBytes(item.index)
                Types.CHAR, Types.VARCHAR, Types.LONGVARCHAR -> array[label] = instance.getString(item.index)
                Types.NCHAR, Types.NVARCHAR, Types.LONGNVARCHAR -> array[label] = instance.getNString(item.index)
                Types.CLOB -> array[label] = instance.getClob(item.index)
                Types.BLOB -> array[label] = instance.getBlob(item.index)
                Types.ARRAY -> array[label] = instance.getArray(item.index)
                Types.NULL -> array[label] = null
                else -> array[label] = instance.getObject(item.index)
            }
        }

        return array
    }

    override val from: Class<ResultSet>
        get() = ResultSet::class.java
    override val to: Class<AssocArray>
        get() = AssocArray::class.java

    companion object {
        private val columnInfoSetGenerator = Instep.make(ColumnInfoSetGenerator::class.java)

        init {
            val typeconvert = Instep.make(TypeConversion::class.java)
            typeconvert.getConverter(ResultSet::class.java, AssocArray::class.java) ?: typeconvert.register(ResultSetToAssocArrayConverter())
        }
    }
}