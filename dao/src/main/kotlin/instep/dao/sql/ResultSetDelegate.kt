package instep.dao.sql

import instep.dao.sql.dialect.AbstractDialect
import java.sql.ResultSet


interface ResultSetDelegate {
    fun getDelegate(dialect: Dialect, rs: ResultSet): AbstractDialect.ResultSet
}