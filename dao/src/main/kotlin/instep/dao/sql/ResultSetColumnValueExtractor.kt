package instep.dao.sql

import instep.dao.sql.dialect.AbstractDialect


interface ResultSetColumnValueExtractor {
    fun extract(valueType: Class<*>, rs: AbstractDialect.ResultSet, colIndex: Int): Any?
}