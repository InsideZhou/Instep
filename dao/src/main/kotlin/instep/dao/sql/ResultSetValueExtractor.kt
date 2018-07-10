package instep.dao.sql

import instep.dao.DaoException
import instep.dao.sql.dialect.AbstractDialect


interface ResultSetValueExtractor {
    @Throws(DaoException::class)
    fun extract(valueType: Class<*>, rs: AbstractDialect.ResultSet, colIndex: Int): Any?
}