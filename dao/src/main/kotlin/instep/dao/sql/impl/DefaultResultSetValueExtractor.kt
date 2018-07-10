package instep.dao.sql.impl

import instep.dao.sql.ResultSetValueExtractor
import instep.dao.sql.dialect.AbstractDialect
import java.io.InputStream
import java.io.Reader
import java.math.BigDecimal
import java.math.BigInteger
import java.time.*


open class DefaultResultSetValueExtractor : ResultSetValueExtractor {
    override fun extract(valueType: Class<*>, rs: AbstractDialect.ResultSet, colIndex: Int): Any? {
        return when (valueType) {
            Boolean::class.java -> rs.getBoolean(colIndex)
            Byte::class.java -> rs.getByte(colIndex)
            Short::class.java -> rs.getShort(colIndex)
            Int::class.java -> rs.getInt(colIndex)
            Long::class.java -> rs.getLong(colIndex)
            BigInteger::class.java -> rs.getLong(colIndex)
            BigDecimal::class.java -> rs.getBigDecimal(colIndex)
            Float::class.java -> rs.getFloat(colIndex)
            Double::class.java -> rs.getDouble(colIndex)
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