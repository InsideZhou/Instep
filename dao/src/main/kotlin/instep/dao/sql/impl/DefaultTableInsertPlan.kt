package instep.dao.sql.impl

import instep.collection.AssocArray
import instep.dao.DaoException
import instep.dao.sql.Column
import instep.dao.sql.Table
import instep.dao.sql.TableInsertPlan
import instep.dao.sql.dialect.PostgreSQLDialect

open class DefaultTableInsertPlan(val table: Table, protected val params: AssocArray) : TableInsertPlan {
    constructor(table: Table) : this(table, AssocArray())

    protected var usingColumn = false
    protected var usingPositional = false

    override fun addValue(column: Column<*>, value: Any?): TableInsertPlan {
        if (usingPositional) throw DaoException("Cannot use column and positional value together.")
        if (!usingColumn) {
            usingColumn = true
        }

        if (table.columns.none { it == column }) throw DaoException("Column ${column.name} should belong to Table ${table.tableName}")

        params.add(column.name to value)

        return this
    }

    override fun addValues(vararg values: Any?): TableInsertPlan {
        if (usingColumn) throw DaoException("Cannot use column and positional value together.")
        if (!usingPositional) {
            usingPositional = true
        }

        params.add(*values)

        return this
    }

    override val statement: String
        get() {
            var txt = "INSERT INTO ${table.tableName} "
            val columns = params.entries

            if (usingColumn) {
                txt += columns.map { it.first }.joinToString(",", "(", ")")
            }

            txt += "\nVALUES (${columns.map {
                if (it.second == Table.DefaultInsertValue) {
                    return@map when (table.dialect) {
                        is PostgreSQLDialect -> "DEFAULT"
                        else -> "NULL"
                    }
                }

                "?"
            }.joinToString(",")})"

            return txt
        }

    override val parameters: List<Any?>
        get() = params.filterNot { it == Table.DefaultInsertValue }
}