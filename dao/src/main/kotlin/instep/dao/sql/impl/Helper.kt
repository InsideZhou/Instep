@file:Suppress("unused")

package instep.dao.sql.impl

import instep.dao.sql.Dialect
import instep.dao.sql.TableInsertPlan
import instep.dao.sql.dialect.AbstractDialect
import instep.dao.sql.dialect.HSQLDialect
import instep.dao.sql.dialect.MySQLDialect
import instep.reflection.Mirror
import java.io.InputStream
import java.io.Reader
import java.math.BigDecimal
import java.math.BigInteger
import java.sql.*
import java.time.*

object Helper {
    fun generateColumnInfoSet(meta: ResultSetMetaData): Set<ResultSetColumnInfo> {
        return (1..meta.columnCount).map { i ->
            ResultSetColumnInfo(i, meta.getColumnLabel(i), meta.getColumnType(i))
        }.toSet()
    }

    fun <T : Any> rowToInstanceAsInstanceFirst(rs: ResultSet, dialect: Dialect, mirror: Mirror<T>, columnInfoSet: Set<ResultSetColumnInfo>): T {
        val instance = mirror.type.newInstance()
        val resultSetDelegate = getResultSetDelegate(dialect, rs)
        val setters = mirror.setters

        setters.forEach { setter ->
            columnInfoSet.forEach columnLoop@{ col ->
                if (setter.name.contains(col.label, true)) {
                    setter.invoke(instance, extractColumnValue(setter.parameterTypes[0], resultSetDelegate, col.index))
                }
            }
        }

        return instance
    }

    fun <T : Any> rowToInstanceAsRowFirst(rs: ResultSet, dialect: Dialect, mirror: Mirror<T>, columnInfoSet: Set<ResultSetColumnInfo>): T {
        val instance = mirror.type.newInstance()
        val resultSetDelegate = getResultSetDelegate(dialect, rs)

        columnInfoSet.forEach { col ->
            val setter = mirror.findSetter(col.label)

            setter?.invoke(instance, extractColumnValue(setter.parameterTypes[0], resultSetDelegate, col.index))
        }

        return instance
    }

    fun extractColumnValue(paramType: Class<*>, rs: AbstractDialect.ResultSet, colIndex: Int): Any? {
        return when (paramType) {
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
            else -> rs.getObject(colIndex, paramType)
        }
    }

    fun generateStatement(conn: Connection, dialect: Dialect, plan: instep.dao.Plan<*>): PreparedStatement {
        val stmt = when (plan) {
            is TableInsertPlan -> conn.prepareStatement(plan.statement, Statement.RETURN_GENERATED_KEYS)
            else -> conn.prepareStatement(plan.statement)
        }

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