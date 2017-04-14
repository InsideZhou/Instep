package instep.dao.sql.impl

import instep.Instep
import instep.collection.AssocArray
import instep.dao.sql.ConnectionProvider
import instep.dao.sql.SQLPlanExecutor
import instep.reflection.Mirror
import instep.typeconvert.Converter
import instep.typeconvert.TypeConvert
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Types
import java.time.OffsetDateTime
import java.time.ZonedDateTime

open class DefaultSQLPlanExecutor(val connectionProvider: ConnectionProvider) : SQLPlanExecutor {
    constructor() : this(Instep.make(ConnectionProvider::class.java)) {
    }

    override fun execute(plan: instep.dao.Plan<*>) {
        val conn = connectionProvider.getConnection()
        try {
            val stmt = Helper.generateStatement(conn, plan)
            stmt.execute()
        }
        finally {
            conn.close()
        }
    }

    override fun executeScalar(plan: instep.dao.Plan<*>): String {
        val conn = connectionProvider.getConnection()

        try {
            val rs = executeResultSet(conn, plan)
            if (!rs.next() || rs.wasNull()) return ""

            return rs.getString(1)
        }
        finally {
            conn.close()
        }
    }

    override fun executeUpdate(plan: instep.dao.Plan<*>): Long {
        val conn = connectionProvider.getConnection()
        val stmt = Helper.generateStatement(conn, plan)
        try {
            return stmt.executeLargeUpdate()
        }
        catch (e: UnsupportedOperationException) {
            return stmt.executeUpdate().toLong()
        }
        finally {
            conn.close()
        }
    }

    override fun executeResultSet(conn: Connection, plan: instep.dao.Plan<*>): ResultSet {
        val stmt = Helper.generateStatement(conn, plan)
        return stmt.executeQuery()
    }

    override fun <T : Any> execute(plan: instep.dao.Plan<*>, cls: Class<T>): List<T> {
        val result = mutableListOf<T>()

        connectionProvider.getConnection().let { conn ->
            val typeconvert = Instep.make(TypeConvert::class.java)

            try {
                val rs = executeResultSet(conn, plan)

                if (typeconvert.canConvert(ResultSet::class.java, cls)) {
                    while (rs.next()) {
                        val instanceOfT = typeconvert.convert(rs, ResultSet::class.java, cls)
                        result.add(instanceOfT)
                    }

                    return@let
                }

                val mirror = Instep.reflect(cls)
                val columnInfoSet = Helper.generateColumnInfoSet(rs.metaData)

                try {
                    while (rs.next()) {
                        result.add(Helper.typeFirstRowToInstance(rs, mirror, columnInfoSet))
                    }
                }
                catch(e: Exception) {
                    throw RuntimeException("Can't create instance of ${cls.name} from result row", e)
                }
            }
            finally {
                conn.close()
            }
        }

        return result.toList()
    }

    companion object {
        init {
            val typeconvert = Instep.make(TypeConvert::class.java)

            if (!typeconvert.canConvert(ResultSet::class.java, AssocArray::class.java)) {
                typeconvert.register(object : Converter<ResultSet, AssocArray> {
                    override fun <T : ResultSet> convert(instance: T): AssocArray {
                        val array = AssocArray(true)

                        Helper.generateColumnInfoSet(instance.metaData).forEach { item ->
                            when (item.type) {
                                Types.TINYINT -> array[item.label] = instance.getByte(item.index)
                                Types.SMALLINT -> array[item.label] = instance.getShort(item.index)
                                Types.INTEGER -> array[item.label] = instance.getInt(item.index)
                                Types.BIGINT -> array[item.label] = instance.getLong(item.index)
                                Types.DECIMAL -> array[item.label] = instance.getBigDecimal(item.index)
                                Types.FLOAT -> array[item.label] = instance.getFloat(item.index)
                                Types.DOUBLE -> array[item.label] = instance.getDouble(item.index)
                                Types.DATE -> array[item.label] = instance.getDate(item.index).toLocalDate()
                                Types.TIME -> array[item.label] = instance.getTime(item.index).toLocalTime()
                                Types.TIMESTAMP -> array[item.label] = instance.getTimestamp(item.index).toLocalDateTime()
                                Types.TIMESTAMP_WITH_TIMEZONE -> array[item.label] = instance.getObject(item.index, OffsetDateTime::class.java)
                                Types.BINARY -> array[item.label] = instance.getBytes(item.index)
                                Types.CHAR, Types.VARCHAR, Types.LONGVARCHAR -> array[item.label] = instance.getString(item.index)
                                Types.NCHAR, Types.NVARCHAR, Types.LONGNVARCHAR -> array[item.label] = instance.getNString(item.index)
                                Types.CLOB -> array[item.label] = instance.getClob(item.index)
                                Types.BLOB -> array[item.label] = instance.getBlob(item.index)
                                else -> array[item.label] = instance.getObject(item.index)
                            }
                        }

                        return array
                    }

                    override val from: Class<ResultSet>
                        get() = ResultSet::class.java
                    override val to: Class<AssocArray>
                        get() = AssocArray::class.java
                })
            }
        }
    }
}

data class ResultSetColumnInfo(val index: Int, val label: String, val type: Int)