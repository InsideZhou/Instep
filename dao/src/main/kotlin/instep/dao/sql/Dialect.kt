package instep.dao.sql

import instep.dao.DaoException
import instep.dao.Plan
import instep.dao.sql.dialect.H2Dialect
import instep.dao.sql.dialect.HSQLDialect
import instep.dao.sql.dialect.MySQLDialect
import instep.dao.sql.dialect.PostgreSQLDialect
import java.io.Serializable
import java.sql.PreparedStatement

/**
 * SQL dialect.
 */
interface Dialect : Serializable {
    fun createTable(tableName: String, columns: List<Column<*>>): Plan<*>
    fun addColumn(tableName: String, column: Column<*>): Plan<*>

    fun setParameterForPreparedStatement(stmt: PreparedStatement, index: Int, value: Any?)

    val pagination: Pagination
    val isOffsetDateTimeSupported: Boolean

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
            else {
                throw DaoException("cannot infer dialect for datasource $url")
            }

            return dialect
        }
    }
}