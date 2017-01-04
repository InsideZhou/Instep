package instep.orm.sql

import instep.orm.Plan
import java.io.Serializable

/**
 * SQL dialect.
 */
interface Dialect : Serializable {
    fun createTable(tableName: String, columns: List<Column<*>>): Plan
    fun dropTable(tableName: String): Plan

    fun addColumns(tableName: String, columns: List<Column<*>>): Plan
    fun dropColumns(tableName: String, columns: List<Column<*>>): Plan

    val pagination: Pagination
}