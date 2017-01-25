package instep.orm.sql.impl

import instep.orm.OrmException
import instep.orm.sql.Column
import instep.orm.sql.Condition
import instep.orm.sql.Table
import instep.orm.sql.TableUpdatePlan

open class DefaultTableUpdatePlan(val table: Table, val params: MutableMap<Column<*>, Any?> = mutableMapOf()) : TableUpdatePlan {
    override var where: Condition? = null

    private var pkValue: Any? = null

    override fun set(column: Column<*>, value: Any?): TableUpdatePlan {
        if (table.columns.none { it == column }) throw OrmException("Column ${column.name} should belong to Table ${table.tableName}")

        params.put(column, value)

        return this
    }

    override fun where(vararg conditions: Condition): TableUpdatePlan {
        if (null == where) {
            where = conditions.reduce(Condition::and)
        }
        else {
            val cond = where
            cond?.andGroup(conditions.reduce(Condition::and))
        }

        return this
    }

    override fun where(value: Any): TableUpdatePlan {
        if (null == table.primaryKey) throw OrmException("Table ${table.tableName} should has primary key")

        pkValue = value
        return this
    }

    override val statement: String
        get() {
            val columns = params.entries
            var txt = "UPDATE ${table.tableName} SET ${columns.map { "${it.key.name}=?" }.joinToString(",")} "

            if (null == where) {
                pkValue?.apply {
                    txt += "WHERE ${table.primaryKey!!.name}=?"
                }

                return txt
            }

            txt += "WHERE "
            where!!.let { txt += it.expression }

            pkValue?.apply {
                txt += " AND ${table.primaryKey!!.name}=?"
            }

            return txt
        }

    override val parameters: List<Any?>
        get() {
            var result = params.values.toList()

            where?.let { result += it.parameters }
            pkValue?.let { result += it }

            return result
        }

    override fun clone(): DefaultTableUpdatePlan {
        val plan = DefaultTableUpdatePlan(table, params)
        plan.where = where
        return plan
    }
}