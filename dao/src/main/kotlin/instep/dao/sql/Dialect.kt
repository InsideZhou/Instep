package instep.dao.sql

import instep.Instep
import instep.dao.DaoException
import instep.dao.sql.dialect.*
import java.sql.PreparedStatement
import java.time.temporal.Temporal

/**
 * SQL dialect.
 */
interface Dialect {
    fun createTable(tableName: String, tableComment: String, columns: List<Column<*>>): SQLPlan<*>
    fun createTableIfNotExists(tableName: String, tableComment: String, columns: List<Column<*>>): SQLPlan<*>
    fun renameTable(tableName: String, newName: String): SQLPlan<*>

    fun addColumn(tableName: String, column: Column<*>): SQLPlan<*>
    fun dropColumn(tableName: String, column: Column<*>): SQLPlan<*>

    fun renameColumn(tableName: String, column: Column<*>, oldName: String): SQLPlan<*>

    fun alterColumnNotNull(tableName: String, column: Column<*>): SQLPlan<*>
    fun alterColumnDefault(tableName: String, column: Column<*>): SQLPlan<*>

    fun setParameterForPreparedStatement(stmt: PreparedStatement, index: Int, value: Any?)

    fun eqCondition(column: Column<*>, value: Any): Condition
    fun notEQCondition(column: Column<*>, value: Any): Condition

    fun isNullCondition(column: Column<*>, value: Any): Condition
    fun notNullCondition(column: Column<*>, value: Any): Condition

    fun <T : Number> lt(column: NumberColumn<*>, value: T): Condition
    fun <T : Number> lte(column: NumberColumn<*>, value: T): Condition
    fun <T : Enum<*>> lt(column: IntegerColumn, value: T): Condition
    fun <T : Enum<*>> lte(column: IntegerColumn, value: T): Condition
    fun <T : Temporal> lt(column: DateTimeColumn, value: T): Condition
    fun <T : Temporal> lte(column: DateTimeColumn, value: T): Condition

    fun <T : Number> gt(column: NumberColumn<*>, value: T): Condition
    fun <T : Number> gte(column: NumberColumn<*>, value: T): Condition
    fun <T : Enum<*>> gt(column: IntegerColumn, value: T): Condition
    fun <T : Enum<*>> gte(column: IntegerColumn, value: T): Condition
    fun <T : Temporal> gt(column: DateTimeColumn, value: T): Condition
    fun <T : Temporal> gte(column: DateTimeColumn, value: T): Condition

    fun contains(column: StringColumn, value: String): Condition
    fun startsWith(column: StringColumn, value: String): Condition
    fun endsWith(column: StringColumn, value: String): Condition

    fun inArray(column: StringColumn, value: Array<String>): Condition
    fun inArray(column: NumberColumn<*>, value: Array<Number>): Condition
    fun inArray(column: IntegerColumn, value: Array<Enum<*>>): Condition

    val defaultValueForInsert: String
    val returningClauseForInsert: String

    val parameterForUUIDType: String
    val parameterForJSONType: String

    val pagination: Pagination
    val offsetDateTimeSupported: Boolean
    val separatelyCommenting: Boolean

    companion object {
        /**
         * Infer dialect with given datasource url.
         */
        fun of(url: String): Dialect {
            val dialect = if (url.startsWith("jdbc:hsqldb", true)) {
                HSQLDialect()
            }
            else if (url.startsWith("jdbc:h2", true)) {
                H2Dialect()
            }
            else if (url.startsWith("jdbc:mysql", true)) {
                MySQLDialect()
            }
            else if (url.startsWith("jdbc:postgresql", true)) {
                PostgreSQLDialect()
            }
            else if (url.startsWith("jdbc:sqlserver", true)) {
                SQLServerDialect()
            }
            else {
                throw DaoException("cannot infer dialect for datasource $url")
            }

            Instep.bind(Dialect::class.java, dialect)

            return dialect
        }
    }
}