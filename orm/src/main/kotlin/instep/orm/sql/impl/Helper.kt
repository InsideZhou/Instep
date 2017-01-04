package instep.orm.sql.impl

import instep.orm.PlaceHolder
import instep.orm.PlaceHolderRemainingException
import instep.reflection.Mirror
import java.io.InputStream
import java.io.Reader
import java.lang.reflect.Method
import java.math.BigDecimal
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime

object Helper {
    fun generateColumnInfoSet(meta: ResultSetMetaData): Set<ResultSetColumnInfo> {
        return (1..meta.columnCount).map { i ->
            ResultSetColumnInfo(i, meta.getColumnLabel(i), meta.getColumnType(i))
        }.toSet()
    }

    fun <T : Any> typeFirstRowToInstance(rs: ResultSet, mirror: Mirror<T>, columnInfoSet: Set<ResultSetColumnInfo>): T {
        val instance = mirror.type.newInstance()
        val setters = mirror.setters

        setters.forEach { setter ->
            columnInfoSet.forEach columnLoop@ { col ->
                if (setter.name.contains(col.label, true)) {
                    evalInstance(setter.parameterTypes[0], instance, setter, rs, col)

                    return@columnLoop
                }
            }
        }

        return instance
    }

    fun <T : Any> resultFirstRowToInstance(rs: ResultSet, mirror: Mirror<T>, columnInfoSet: Set<ResultSetColumnInfo>): T {
        val instance = mirror.type.newInstance()

        columnInfoSet.forEach { col ->
            val setter = mirror.findSetter(col.label)
            if (null != setter) {
                evalInstance(setter.parameterTypes[0], instance, setter, rs, col)
            }
        }

        return instance
    }

    private fun <T : Any> evalInstance(paramType: Class<*>, instance: T, setter: Method, rs: ResultSet, col: ResultSetColumnInfo) {
        when (paramType) {
            Byte::class.java -> setter.invoke(instance, rs.getByte(col.index))
            Short::class.java -> setter.invoke(instance, rs.getShort(col.index))
            Int::class.java -> setter.invoke(instance, rs.getInt(col.index))
            Long::class.java -> setter.invoke(instance, rs.getLong(col.index))
            BigDecimal::class.java -> setter.invoke(instance, rs.getBigDecimal(col.index))
            Float::class.java -> setter.invoke(instance, rs.getFloat(col.index))
            Double::class.java -> setter.invoke(instance, rs.getDouble(col.index))
            LocalDate::class.java -> setter.invoke(instance, rs.getDate(col.index).toLocalDate())
            LocalTime::class.java -> setter.invoke(instance, rs.getTime(col.index).toLocalTime())
            LocalDateTime::class.java -> setter.invoke(instance, rs.getTimestamp(col.index).toLocalDateTime())
            OffsetDateTime::class.java -> setter.invoke(instance, rs.getObject(col.index, OffsetDateTime::class.java))
            InputStream::class.java -> setter.invoke(instance, rs.getBinaryStream(col.index))
            ByteArray::class.java -> setter.invoke(instance, rs.getBytes(col.index))
            String::class.java -> setter.invoke(instance, rs.getString(col.index))
            Char::class.java -> setter.invoke(instance, rs.getString(col.index).toCharArray().firstOrNull())
            InputStream::class.java -> setter.invoke(instance, rs.getBinaryStream(col.index))
            Reader::class.java -> setter.invoke(instance, rs.getCharacterStream(col.index))
            else -> setter.invoke(instance, rs.getObject(col.index, paramType))
        }
    }

    fun generateStatement(conn: Connection, plan: instep.orm.Plan): PreparedStatement {
        val stmt = conn.prepareStatement(plan.statement)

        plan.parameters.forEachIndexed { i, value ->
            val paramIndex = i + 1

            when (value) {
                is PlaceHolder -> throw PlaceHolderRemainingException(value)
                is Boolean -> stmt.setBoolean(paramIndex, value)
                is Char -> stmt.setString(paramIndex, value.toString())
                is String -> stmt.setString(paramIndex, value)
                is Byte -> stmt.setByte(paramIndex, value)
                is Short -> stmt.setShort(paramIndex, value)
                is Int -> stmt.setInt(paramIndex, value)
                is Long -> stmt.setLong(paramIndex, value)
                is BigDecimal -> stmt.setBigDecimal(paramIndex, value)
                is Float -> stmt.setFloat(paramIndex, value)
                is Double -> stmt.setDouble(paramIndex, value)
                is ByteArray -> stmt.setBytes(paramIndex, value)
                is LocalDate -> stmt.setDate(paramIndex, java.sql.Date.valueOf(value))
                is LocalTime -> stmt.setTime(paramIndex, java.sql.Time.valueOf(value))
                is LocalDateTime -> stmt.setTimestamp(paramIndex, java.sql.Timestamp.valueOf(value))
                is OffsetDateTime -> stmt.setObject(paramIndex, value)
                is InputStream -> stmt.setBinaryStream(paramIndex, value)
                is Reader -> stmt.setCharacterStream(paramIndex, value)
                else -> stmt.setObject(paramIndex, value)
            }
        }

        return stmt
    }
}