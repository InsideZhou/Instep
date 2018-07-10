package instep.dao.sql

import instep.dao.sql.impl.ResultSetColumnInfo
import java.sql.ResultSetMetaData


interface ColumnInfoSetGenerator {
    fun generate(meta: ResultSetMetaData): Set<ResultSetColumnInfo>
}