package instep.dao.sql.impl

import instep.dao.sql.Dialect
import instep.dao.sql.dialect.AbstractDialect
import instep.dao.sql.dialect.HSQLDialect
import instep.dao.sql.dialect.MySQLDialect
import instep.reflection.Mirror
import java.io.InputStream
import java.io.Reader
import java.lang.reflect.Method
import java.math.BigDecimal
import java.math.BigInteger
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.time.*

object Helper {
    fun generateColumnInfoSet(meta: ResultSetMetaData): Set<ResultSetColumnInfo> {
        return (1..meta.columnCount).map { i ->
            ResultSetColumnInfo(i, meta.getColumnLabel(i), meta.getColumnType(i))
        }.toSet()
    }

    fun <T : Any> rowToInstanceAsInstanceFirst(rs: ResultSet, dialect: Dialect, mirror: Mirror<T>, columnInfoSet: Set<ResultSetColumnInfo>): T {
        val instance = mirror.type.newInstance()
        val setters = mirror.setters

        setters.forEach { setter ->
            columnInfoSet.forEach columnLoop@ { col ->
                if (setter.name.contains(col.label, true)) {
                    evalInstance(setter.parameterTypes[0], instance, setter, getResultSetDelegate(dialect, rs), col)

                    return@columnLoop
                }
            }
        }

        return instance
    }

    fun <T : Any> rowToInstanceAsRowFirst(rs: ResultSet, dialect: Dialect, mirror: Mirror<T>, columnInfoSet: Set<ResultSetColumnInfo>): T {
        val instance = mirror.type.newInstance()

        columnInfoSet.forEach { col ->
            val setter = mirror.findSetter(col.label)
            if (null != setter) {
                evalInstance(setter.parameterTypes[0], instance, setter, getResultSetDelegate(dialect, rs), col)
            }
        }

        return instance
    }

    fun <T : Any> evalInstance(paramType: Class<*>, instance: T, setter: Method, rs: AbstractDialect.ResultSet, col: ResultSetColumnInfo) {
        when (paramType) {
            Boolean::class.java -> setter.invoke(instance, rs.getBoolean(col.index))
            Byte::class.java -> setter.invoke(instance, rs.getByte(col.index))
            Short::class.java -> setter.invoke(instance, rs.getShort(col.index))
            Int::class.java -> setter.invoke(instance, rs.getInt(col.index))
            Long::class.java -> setter.invoke(instance, rs.getLong(col.index))
            BigInteger::class.java -> setter.invoke(instance, rs.getLong(col.index))
            BigDecimal::class.java -> setter.invoke(instance, rs.getBigDecimal(col.index))
            Float::class.java -> setter.invoke(instance, rs.getFloat(col.index))
            Double::class.java -> setter.invoke(instance, rs.getDouble(col.index))
            Instant::class.java -> setter.invoke(instance, rs.getInstant(col.index))
            LocalDate::class.java -> setter.invoke(instance, rs.getLocalDate(col.index))
            LocalTime::class.java -> setter.invoke(instance, rs.getLocalTime(col.index))
            LocalDateTime::class.java -> setter.invoke(instance, rs.getLocalDateTime(col.index))
            OffsetDateTime::class.java -> setter.invoke(instance, rs.getOffsetDateTime(col.index))
            InputStream::class.java -> setter.invoke(instance, rs.getBinaryStream(col.index))
            ByteArray::class.java -> setter.invoke(instance, rs.getBytes(col.index))
            String::class.java -> setter.invoke(instance, rs.getString(col.index))
            Char::class.java -> setter.invoke(instance, rs.getString(col.index)?.toCharArray()?.firstOrNull())
            InputStream::class.java -> setter.invoke(instance, rs.getBinaryStream(col.index))
            Reader::class.java -> setter.invoke(instance, rs.getCharacterStream(col.index))
            else -> setter.invoke(instance, rs.getObject(col.index, paramType))
        }
    }

    fun generateStatement(conn: Connection, dialect: Dialect, plan: instep.dao.Plan<*>): PreparedStatement {
        val stmt = conn.prepareStatement(plan.statement)

        plan.parameters.forEachIndexed { i, value ->
            val paramIndex = i + 1

            dialect.setParameterForPreparedStatement(stmt, paramIndex, value)
        }

        return stmt
    }

    fun getResultSetDelegate(dialect: Dialect, rs: ResultSet): AbstractDialect.ResultSet {
        return when (dialect) {
            is HSQLDialect -> HSQLDialect.ResultSet(rs)
            is MySQLDialect -> MySQLDialect.ResultSet(rs)
            else -> AbstractDialect.ResultSet(rs)
        }
    }
}