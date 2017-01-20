package instep.orm.sql.impl

import instep.collection.AssocArray
import instep.orm.OrmException
import instep.orm.sql.Column
import instep.orm.sql.Table
import instep.orm.sql.TableInsertPlan

open class DefaultTableInsertPlan(val table: Table, protected val params: AssocArray) : TableInsertPlan {
    constructor(table: Table) : this(table, AssocArray())

    private var usingColumn = false
    private var usingPositional = false

    override fun addValue(column: Column<*>, value: Any?): TableInsertPlan {
        if (usingPositional) throw OrmException("Cannot use column and positional value together.")
        if (!usingColumn) {
            usingColumn = true
        }

        if (table.columns().none { it == column }) throw OrmException("Column ${column.name} not belong to Table ${table.tableName}")

        params.add(Pair(column.name, value))

        return this
    }

    override fun addValues(vararg values: Any?): TableInsertPlan {
        if (usingColumn) throw OrmException("Cannot use column and positional value together.")
        if (!usingPositional) {
            usingPositional = true
        }

        params.add(*values)

        return this
    }

    override val statement: String
        get() {
            var txt = "INSERT INTO ${table.tableName} "
            val columns = params.entries()

            if (usingColumn) {
                txt += columns.map { it.first }.joinToString(",", "(", ")")
            }

            txt += "\nVALUES (${columns.map { "?" }.joinToString(",")})"

            return txt
        }

    override val parameters: List<Any?>
        get() {
            return params.toList()
        }

    override fun clone(): DefaultTableInsertPlan {
        val plan = DefaultTableInsertPlan(table, params)
        plan.usingColumn = usingColumn
        plan.usingPositional = usingPositional
        return plan
    }
}