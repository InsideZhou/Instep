package instep.dao.sql.impl

import instep.dao.sql.Dialect
import instep.dao.sql.ResultSetDelegate
import instep.dao.sql.dialect.*
import java.sql.ResultSet


open class DefaultResultSetDelegate : ResultSetDelegate {
    override fun getDelegate(dialect: Dialect, rs: ResultSet): AbstractDialect.ResultSet {
        return when (dialect) {
            is HSQLDialect -> HSQLDialect.ResultSet(rs)
            is MySQLDialect -> MySQLDialect.ResultSet(rs)
            is SQLServerDialect -> SQLServerDialect.ResultSet(rs)
            is PostgreSQLDialect -> PostgreSQLDialect.ResultSet(rs)
            else -> AbstractDialect.ResultSet(rs)
        }
    }
}