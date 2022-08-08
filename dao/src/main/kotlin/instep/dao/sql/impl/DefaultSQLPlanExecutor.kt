package instep.dao.sql.impl

import instep.Instep
import instep.dao.sql.*
import instep.typeconversion.TypeConversion
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.time.*
import java.time.temporal.Temporal

@Suppress("MemberVisibilityCanBePrivate", "UNCHECKED_CAST")
open class DefaultSQLPlanExecutor<S : SQLPlan<*>>(
    val connectionProvider: ConnectionProvider,
    val preparedStatementGenerator: PreparedStatementGenerator,
    val typeconvert: TypeConversion,
) : SQLPlanExecutor<S> {
    constructor(connectionProvider: ConnectionProvider) : this(
        connectionProvider,
        Instep.make(PreparedStatementGenerator::class.java),
        Instep.make(TypeConversion::class.java),
    )

    constructor() : this(Instep.make(ConnectionProvider::class.java))

    override fun execute(plan: S) {
        val conn = connectionProvider.getConnection()
        try {
            val stmt = preparedStatementGenerator.generate(conn, connectionProvider.dialect, plan)
            stmt.execute()

            if (plan.subPlans.isNotEmpty()) {
                plan.subPlans.forEach {
                    execute(it as S)
                }
            }
        }
        catch (e: SQLException) {
            throw SQLPlanExecutionException(e)
        }
        finally {
            connectionProvider.releaseConnection(conn)
        }
    }

    override fun executeString(plan: S): String {
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
            connectionProvider.releaseConnection(conn)
        }
    }

    override fun executeLong(plan: S): Long {
        val conn = connectionProvider.getConnection()

        try {
            val rs = executeResultSet(conn, plan)
            if (!rs.next() || rs.wasNull()) return 0L

            return rs.getLong(1)
        }
        catch (e: SQLException) {
            throw SQLPlanExecutionException(e)
        }
        finally {
            connectionProvider.releaseConnection(conn)
        }
    }

    override fun executeDouble(plan: S): Double {
        val conn = connectionProvider.getConnection()

        try {
            val rs = executeResultSet(conn, plan)
            if (!rs.next() || rs.wasNull()) return 0.0

            return rs.getDouble(1)
        }
        catch (e: SQLException) {
            throw SQLPlanExecutionException(e)
        }
        finally {
            connectionProvider.releaseConnection(conn)
        }
    }

    override fun <R : Temporal> executeTemporal(plan: S, cls: Class<R>): R? {
        val conn = connectionProvider.getConnection()

        try {
            val resultSet = executeResultSet(conn, plan)
            if (!resultSet.next() || resultSet.wasNull()) return null

            val resultSetDelegate = Instep.make(ResultSetDelegate::class.java)
            val rs = resultSetDelegate.getDelegate(connectionProvider.dialect, resultSet)
            val colIndex = 1

            return when (cls) {
                Instant::class.java -> rs.getInstant(colIndex) as R
                LocalDate::class.java -> rs.getLocalDate(colIndex) as R
                LocalTime::class.java -> rs.getLocalTime(colIndex) as R
                LocalDateTime::class.java -> rs.getLocalDateTime(colIndex) as R
                OffsetDateTime::class.java -> rs.getOffsetDateTime(colIndex) as R
                else -> rs.getObject(colIndex, cls)
            }
        }
        catch (e: SQLException) {
            throw SQLPlanExecutionException(e)
        }
        finally {
            connectionProvider.releaseConnection(conn)
        }
    }

    @Suppress("LiftReturnOrAssignment")
    override fun executeUpdate(plan: S): Int {
        val conn = connectionProvider.getConnection()
        try {
            return preparedStatementGenerator.generate(conn, connectionProvider.dialect, plan).executeUpdate()
        }
        catch (e: SQLException) {
            throw SQLPlanExecutionException(e)
        }
        finally {
            connectionProvider.releaseConnection(conn)
        }
    }

    override fun executeResultSet(conn: Connection, plan: S): ResultSet {
        val stmt = preparedStatementGenerator.generate(conn, connectionProvider.dialect, plan)

        return when (plan) {
            is TableInsertPlan -> {
                stmt.executeUpdate()
                stmt.generatedKeys
            }

            else -> stmt.executeQuery()
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> execute(plan: S, cls: Class<T>): List<T> {
        val conn = connectionProvider.getConnection()

        val tableRows = mutableListOf<TableRow>()
        val dataRows = mutableListOf<DataRow>()

        try {
            val rs = executeResultSet(conn, plan)
            when {
                plan is TableSelectPlan && plan.join.isEmpty() && !DataRow::class.java.isAssignableFrom(cls) -> {
                    while (rs.next()) {
                        tableRows.add(TableRow.createInstance(plan.from, rs))
                    }
                }
                plan is TableInsertPlan && !DataRow::class.java.isAssignableFrom(cls) -> {
                    while (rs.next()) {
                        tableRows.add(TableRow.createInstance(plan.table, rs))
                    }
                }
                else -> {
                    typeconvert.getConverter(ResultSet::class.java, DataRow::class.java)!!.let { converter ->
                        while (rs.next()) {
                            dataRows.add(converter.convert(rs))
                        }
                    }
                }
            }
        }
        catch (e: SQLException) {
            throw SQLPlanExecutionException(e)
        }
        finally {
            connectionProvider.releaseConnection(conn)
        }

        if (tableRows.size > 0) return tableRowsToObjects(tableRows, cls)
        if (dataRows.size > 0) return dataRowsToObjects(dataRows, cls)

        return emptyList()
    }

    protected open fun <T : Any> tableRowsToObjects(rows: List<TableRow>, cls: Class<T>): List<T> {
        if (cls.isAssignableFrom(TableRow::class.java)) {
            return rows.map { it as T }
        }

        typeconvert.getConverter(TableRow::class.java, cls)?.let { converter ->
            return rows.map { converter.convert(it) }
        }

        return rows.map { row -> row.fillUp(cls) }
    }

    protected open fun <T : Any> dataRowsToObjects(rows: List<DataRow>, cls: Class<T>): List<T> {
        if (cls.isAssignableFrom(DataRow::class.java)) {
            return rows.map { it as T }
        }

        typeconvert.getConverter(DataRow::class.java, cls)?.let { converter ->
            return rows.map { converter.convert(it) }
        }

        return rows.map { row -> row.fillUp(cls) }
    }
}

data class ResultSetColumnInfo(val index: Int, val label: String, val type: Int, val typeName: String)