package instep.dao.sql.impl

import instep.dao.DaoException
import instep.dao.impl.AbstractPlan
import instep.dao.sql.Column
import instep.dao.sql.Condition
import instep.dao.sql.Table
import instep.dao.sql.TableUpdatePlan

open class DefaultTableUpdatePlan(val table: Table, val params: MutableMap<Column<*>, Any?> = mutableMapOf()) : AbstractPlan<TableUpdatePlan>(), TableUpdatePlan {
    override var where: Condition? = null

    private var pkValue: Any? = null

    override fun set(column: Column<*>, value: Any?): TableUpdatePlan {
        if (table.columns.none { it == column }) throw DaoException("Column ${column.name} should belong to Table ${table.tableName}")

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
        if (null == table.primaryKey) throw DaoException("Table ${table.tableName} should has primary key")

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
}