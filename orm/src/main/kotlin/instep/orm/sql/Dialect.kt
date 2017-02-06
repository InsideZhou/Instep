package instep.orm.sql

import instep.orm.Plan
import java.io.Serializable

/**
 * SQL dialect.
 */
interface Dialect : Serializable {
    fun createTable(tableName: String, columns: List<Column<*>>): Plan<*>
    fun addColumn(tableName: String, column: Column<*>): Plan<*>

    val pagination: Pagination
}