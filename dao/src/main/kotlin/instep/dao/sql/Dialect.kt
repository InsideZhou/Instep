package instep.dao.sql

import instep.dao.DaoException
import instep.dao.sql.dialect.*
import java.sql.PreparedStatement
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

    fun placeholderForParameter(column: Column<*>): String

    val defaultValueForInsert: String
    val returningClauseForInsert: String

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