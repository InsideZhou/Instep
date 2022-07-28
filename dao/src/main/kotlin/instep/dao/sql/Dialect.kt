package instep.dao.sql

import instep.dao.DaoException
import instep.dao.sql.dialect.*
import java.sql.PreparedStatement
import java.time.temporal.Temporal
import java.util.*

/**
 * SQL dialect.
 */
interface Dialect {
    fun createTable(tableName: String, tableComment: String, columns: List<Column<*>>): SQLPlan<*>
    fun createTableIfNotExists(tableName: String, tableComment: String, columns: List<Column<*>>): SQLPlan<*>
    fun renameTable(tableName: String, newName: String): SQLPlan<*>

    fun dropTable(tableName: String): SQLPlan<*>
    fun dropTableIfExists(tableName: String): SQLPlan<*>


    fun addColumn(column: Column<*>): SQLPlan<*>
    fun dropColumn(column: Column<*>): SQLPlan<*>

    fun renameColumn(column: Column<*>, oldName: String): SQLPlan<*>

    fun alterColumnNotNull(column: Column<*>): SQLPlan<*>
    fun alterColumnDefault(column: Column<*>): SQLPlan<*>

    fun setParameterForPreparedStatement(stmt: PreparedStatement, index: Int, value: Any?)

    fun eq(column: Column<*>, value: Any): Condition
    fun notEQ(column: Column<*>, value: Any): Condition

    fun isNull(column: Column<*>): Condition
    fun isNotNull(column: Column<*>): Condition
    fun isNull(column: String): Condition
    fun isNotNull(column: String): Condition

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

    fun <T : Number> eq(column: String, value: T): Condition
    fun <T : Number> lt(column: String, value: T): Condition
    fun <T : Number> lte(column: String, value: T): Condition
    fun <T : Number> gt(column: String, value: T): Condition
    fun <T : Number> gte(column: String, value: T): Condition

    fun eq(column: StringColumn, value: String): Condition
    fun notEQ(column: StringColumn, value: String): Condition
    fun contains(column: StringColumn, value: String): Condition
    fun startsWith(column: StringColumn, value: String): Condition
    fun endsWith(column: StringColumn, value: String): Condition

    fun notInArray(column: StringColumn, value: Array<String>): Condition
    fun <T : Number> notInArray(column: NumberColumn<*>, value: Array<T>): Condition
    fun notInArray(column: IntegerColumn, value: Array<Enum<*>>): Condition

    fun inArray(column: StringColumn, value: Array<String>): Condition
    fun <T : Number> inArray(column: NumberColumn<*>, value: Array<T>): Condition
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
            Objects.requireNonNull(url)

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

            return dialect
        }
    }
}