package instep.dao.sql.impl

import instep.Instep
import instep.collection.AssocArray
import instep.dao.sql.*
import instep.typeconversion.Converter
import instep.typeconversion.TypeConversion
import java.sql.*
import java.time.OffsetDateTime

@Suppress("MemberVisibilityCanBePrivate")
open class DefaultSQLPlanExecutor(
    val connectionProvider: ConnectionProvider,
    val resultSetValueExtractor: ResultSetValueExtractor,
    val resultSetDelegate: ResultSetDelegate,
    val columnInfoSetGenerator: ColumnInfoSetGenerator,
    val preparedStatementGenerator: PreparedStatementGenerator,
    val typeconvert: TypeConversion
) : SQLPlanExecutor {

    constructor() : this(
        Instep.make(ConnectionProvider::class.java),
        Instep.make(ResultSetValueExtractor::class.java),
        Instep.make(ResultSetDelegate::class.java),
        Instep.make(ColumnInfoSetGenerator::class.java),
        Instep.make(PreparedStatementGenerator::class.java),
        Instep.make(TypeConversion::class.java)
    )

    override fun execute(plan: instep.dao.Plan<*>) {
        val conn = connectionProvider.getConnection()
        try {
            val stmt = preparedStatementGenerator.generate(conn, connectionProvider.dialect, plan)
            stmt.execute()
        }
        catch (e: SQLException) {
            throw SQLPlanExecutionException(e)
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
        catch (e: SQLException) {
            throw SQLPlanExecutionException(e)
        }
        finally {
            conn.close()
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> executeScalar(plan: instep.dao.Plan<*>, cls: Class<T>): T? {
        val conn = connectionProvider.getConnection()

        try {
            val rs = executeResultSet(conn, plan)
            if (!rs.next() || rs.wasNull()) return null

            return resultSetValueExtractor.extract(cls, resultSetDelegate.getDelegate(connectionProvider.dialect, rs), 1) as T
        }
        catch (e: SQLException) {
            throw SQLPlanExecutionException(e)
        }
        finally {
            conn.close()
        }
    }

    @Suppress("LiftReturnOrAssignment")
    override fun executeUpdate(plan: instep.dao.Plan<*>): Long {
        var stmt: PreparedStatement? = null
        val conn = connectionProvider.getConnection()
        try {
            stmt = preparedStatementGenerator.generate(conn, connectionProvider.dialect, plan)
            return stmt.executeLargeUpdate()
        }
        catch (e: UnsupportedOperationException) {
            if (null == stmt) throw e

            return stmt.executeUpdate().toLong()
        }
        catch (e: SQLException) {
            throw SQLPlanExecutionException(e)
        }
        finally {
            conn.close()
        }
    }

    override fun executeResultSet(conn: Connection, plan: instep.dao.Plan<*>): ResultSet {
        val stmt = preparedStatementGenerator.generate(conn, connectionProvider.dialect, plan)

        return when (plan) {
            is TableInsertPlan -> {
                stmt.executeUpdate()
                stmt.generatedKeys
            }
            else -> stmt.executeQuery()
        }
    }

    override fun <T : Any> execute(plan: instep.dao.Plan<*>, cls: Class<T>): List<T> {
        val result = mutableListOf<T>()

        val conn = connectionProvider.getConnection()
        try {
            val rs = executeResultSet(conn, plan)

            if (typeconvert.canConvert(ResultSet::class.java, cls)) {
                while (rs.next()) {
                    val instanceOfT = typeconvert.convert(rs, ResultSet::class.java, cls)
                    result.add(instanceOfT)
                }

                return result
            }

            val mirror = Instep.reflect(cls)
            val columnInfoSet = columnInfoSetGenerator.generate(rs.metaData)

            try {
                while (rs.next()) {
                    result.add(Helper.resultSetToInstanceByInstanceFirst(rs, connectionProvider.dialect, mirror, columnInfoSet))
                }
            }
            catch (e: Exception) {
                throw RuntimeException("Can't create instance of ${cls.name} from result row", e)
            }
        }
        catch (e: SQLException) {
            throw SQLPlanExecutionException(e)
        }
        finally {
            conn.close()
        }

        return result
    }

    companion object {
        init {
            val columnInfoSetGenerator = Instep.make(ColumnInfoSetGenerator::class.java)
            val typeconvert = Instep.make(TypeConversion::class.java)

            if (!typeconvert.canConvert(ResultSet::class.java, AssocArray::class.java)) {
                typeconvert.register(object : Converter<ResultSet, AssocArray> {
                    override fun <T : ResultSet> convert(instance: T): AssocArray {
                        val array = AssocArray(true)

                        columnInfoSetGenerator.generate(instance.metaData).forEach { item ->
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