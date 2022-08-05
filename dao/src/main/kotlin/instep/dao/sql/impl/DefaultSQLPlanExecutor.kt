package instep.dao.sql.impl

import instep.Instep
import instep.InstepLogger
import instep.dao.sql.*
import instep.typeconversion.Converter
import instep.typeconversion.ConverterEligible
import instep.typeconversion.TypeConversion
import instep.util.path
import instep.util.snakeToCamelCase
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

@Suppress("MemberVisibilityCanBePrivate", "UNCHECKED_CAST")
open class DefaultSQLPlanExecutor<S : SQLPlan<*>>(
    val connectionProvider: ConnectionProvider,
    val resultSetColumnValueExtractor: ResultSetColumnValueExtractor,
    val resultSetDelegate: ResultSetDelegate,
    val preparedStatementGenerator: PreparedStatementGenerator,
    val typeconvert: TypeConversion,
) : SQLPlanExecutor<S> {
    constructor(connectionProvider: ConnectionProvider) : this(
        connectionProvider,
        Instep.make(ResultSetColumnValueExtractor::class.java),
        Instep.make(ResultSetDelegate::class.java),
        Instep.make(PreparedStatementGenerator::class.java),
        Instep.make(TypeConversion::class.java),
    )

    constructor() : this(Instep.make(ConnectionProvider::class.java))

    private val logger = InstepLogger.getLogger(DefaultSQLPlanExecutor::class.java)

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
        } catch (e: SQLException) {
            throw SQLPlanExecutionException(e)
        }
        finally {
            connectionProvider.releaseConnection(conn)
        }
    }

    override fun executeScalar(plan: S): String {
        val conn = connectionProvider.getConnection()

        try {
            val rs = executeResultSet(conn, plan)
            if (!rs.next() || rs.wasNull()) return ""

            return rs.getString(1)
        } catch (e: SQLException) {
            throw SQLPlanExecutionException(e)
        }
        finally {
            connectionProvider.releaseConnection(conn)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> executeScalar(plan: S, cls: Class<T>): T? {
        val conn = connectionProvider.getConnection()

        try {
            val rs = executeResultSet(conn, plan)

            if (!rs.next()) return null

            return resultSetColumnValueExtractor.extract(cls, resultSetDelegate.getDelegate(connectionProvider.dialect, rs), 1) as T?
        } catch (e: SQLException) {
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
        } catch (e: SQLException) {
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
        val dataRows = try {
            val rs = executeResultSet(conn, plan)
            val dataRows = mutableListOf<DataRow>()

            typeconvert.getConverter(ResultSet::class.java, DataRow::class.java)!!.let { converter ->
                while (rs.next()) {
                    dataRows.add(converter.convert(rs))
                }
            }

            dataRows
        } catch (e: SQLException) {
            throw SQLPlanExecutionException(e)
        }
        finally {
            connectionProvider.releaseConnection(conn)
        }

        if (cls.isAssignableFrom(DataRow::class.java)) {
            return dataRows.map { it as T }
        }

        typeconvert.getConverter(DataRow::class.java, cls)?.let { converter ->
            return dataRows.map { converter.convert(it) }
        }

        if (TableRow::class.java.isAssignableFrom(cls)) {
            when (plan) {
                is TableSelectPlan -> return dataRows.map { TableRow.createInstance(it, plan.from, connectionProvider.dialect) } as List<T>
                is TableInsertPlan -> return dataRows.map { TableRow.createInstance(it, plan.table, connectionProvider.dialect) } as List<T>
                else -> Unit
            }
        }

        val targetMutableProperties = Instep.reflectFromClass(cls).getMutablePropertiesUntil(Any::class.java)
        return dataRows.map instance@{ row ->
            val instance = cls.getDeclaredConstructor().newInstance()

            row.entries.filter { null != it.second }.forEach { pair ->
                val property = targetMutableProperties.find { it.field.name.equals(pair.first.toString().snakeToCamelCase()) } ?: return@forEach
                val setterType = property.setter.parameterTypes.first()
                val value = pair.second!!
                val path = instance.javaClass.path(property.field)

                logger.message("setting value to target by setter")
                    .context("property", path)
                    .context("property_type", setterType.name)
                    .context("value", value)
                    .trace()

                property.setter.getAnnotationsByType(ConverterEligible::class.java)
                    .firstNotNullOfOrNull { converterEligible ->
                        (typeconvert.getConverter(converterEligible.type.java, setterType, path) as? Converter<Any, Any>)
                    }
                    ?.let { converter ->
                        property.setter.invoke(instance, converter.convert(value))
                        return@forEach
                    }

                (typeconvert.getConverter(value.javaClass, setterType) as? Converter<Any, Any>)?.let { converter ->
                    property.setter.invoke(instance, converter.convert(value))
                    return@forEach
                }

                when (value) {
                    is String -> {
                        when {
                            setterType.isEnum -> {
                                property.setter.invoke(instance, setterType.enumConstants.first { it.toString() == value })
                            }

                            else -> property.setter.invoke(instance, value)
                        }
                    }

                    is LocalDateTime -> {
                        when (setterType) {
                            Instant::class.java -> property.setter.invoke(instance, value.toInstant(ZoneOffset.UTC))
                            else -> property.setter.invoke(instance, value)
                        }
                    }

                    else -> property.setter.invoke(instance, value)
                }
            }

            return@instance instance as T
        }
    }
}

data class ResultSetColumnInfo(val index: Int, val label: String, val type: Int, val typeName: String)