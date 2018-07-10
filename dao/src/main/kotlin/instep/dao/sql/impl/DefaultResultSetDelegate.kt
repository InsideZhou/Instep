package instep.dao.sql.impl

import instep.dao.sql.Dialect
import instep.dao.sql.ResultSetDelegate
import instep.dao.sql.dialect.AbstractDialect
import instep.dao.sql.dialect.HSQLDialect
import instep.dao.sql.dialect.MySQLDialect
import java.sql.ResultSet


open class DefaultResultSetDelegate : ResultSetDelegate {
    override fun getDelegate(dialect: Dialect, rs: ResultSet): AbstractDialect.ResultSet {
        return when (dialect) {
            is HSQLDialect -> HSQLDialect.ResultSet(rs)
            is MySQLDialect -> MySQLDialect.ResultSet(rs)
            else -> AbstractDialect.ResultSet(rs)
        }
    }
}