package instep.dao.sql

import instep.Instep
import instep.dao.DaoException
import instep.dao.sql.dialect.*
import java.sql.PreparedStatement

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

    val defaultInsertValue: String
    val placeholderForUUIDType: String
    val placeholderForJSONType: String

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