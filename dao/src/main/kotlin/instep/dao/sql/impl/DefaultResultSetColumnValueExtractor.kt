package instep.dao.sql.impl

import instep.dao.sql.ResultSetColumnValueExtractor
import instep.dao.sql.dialect.AbstractDialect
import java.io.InputStream
import java.io.Reader
import java.math.BigDecimal
import java.math.BigInteger
import java.time.*


open class DefaultResultSetColumnValueExtractor : ResultSetColumnValueExtractor {
    override fun extract(valueType: Class<*>, rs: AbstractDialect.ResultSet, colIndex: Int): Any? {
        return when (valueType) {
            Boolean::class.java -> {
                val value = rs.getBoolean(colIndex)
                if (rs.wasNull()) {
                    null
                }
                else {
                    value
                }
            }
            Byte::class.java -> {
                val value = rs.getByte(colIndex)
                if (rs.wasNull()) {
                    null
                }
                else {
                    value
                }
            }
            Short::class.java -> {
                val value = rs.getShort(colIndex)
                if (rs.wasNull()) {
                    null
                }
                else {
                    value
                }
            }
            Int::class.java -> {
                val value = rs.getInt(colIndex)
                if (rs.wasNull()) {
                    null
                }
                else {
                    value
                }
            }
            Long::class.java -> {
                val value = rs.getLong(colIndex)
                if (rs.wasNull()) {
                    null
                }
                else {
                    value
                }
            }
            BigInteger::class.java -> {
                val value = rs.getLong(colIndex)
                if (rs.wasNull()) {
                    null
                }
                else {
                    value
                }
            }
            Float::class.java -> {
                val value = rs.getFloat(colIndex)
                if (rs.wasNull()) {
                    null
                }
                else {
                    value
                }
            }
            Double::class.java -> {
                val value = rs.getDouble(colIndex)
                if (rs.wasNull()) {
                    null
                }
                else {
                    value
                }
            }
            BigDecimal::class.java -> rs.getBigDecimal(colIndex)
            Instant::class.java -> rs.getInstant(colIndex)
            LocalDate::class.java -> rs.getLocalDate(colIndex)
            LocalTime::class.java -> rs.getLocalTime(colIndex)
            LocalDateTime::class.java -> rs.getLocalDateTime(colIndex)
            OffsetDateTime::class.java -> rs.getOffsetDateTime(colIndex)
            InputStream::class.java -> rs.getBinaryStream(colIndex)
            ByteArray::class.java -> rs.getBytes(colIndex)
            String::class.java -> rs.getString(colIndex)
            Char::class.java -> rs.getString(colIndex)?.toCharArray()?.firstOrNull()
            InputStream::class.java -> rs.getBinaryStream(colIndex)
            Reader::class.java -> rs.getCharacterStream(colIndex)
            else -> rs.getObject(colIndex, valueType)
        }
    }
}