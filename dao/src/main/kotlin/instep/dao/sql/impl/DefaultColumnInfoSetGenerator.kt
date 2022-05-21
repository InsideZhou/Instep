package instep.dao.sql.impl

import instep.dao.sql.ColumnInfoSetGenerator
import java.sql.ResultSetMetaData


open class DefaultColumnInfoSetGenerator : ColumnInfoSetGenerator {
    override fun generate(meta: ResultSetMetaData): Set<ResultSetColumnInfo> {
        return (1..meta.columnCount).map { i ->
            ResultSetColumnInfo(i, meta.getColumnLabel(i), meta.getColumnType(i))
        }.toSet()
    }
}