package instep.dao.sql.impl

import instep.dao.DaoException
import instep.dao.sql.Column
import instep.dao.sql.Condition
import instep.dao.sql.Table
import instep.dao.sql.TableDeletePlan

open class DefaultTableDeletePlan(val table: Table, val params: MutableMap<Column<*>, Any?> = mutableMapOf()) : TableDeletePlan {
    override var where: Condition? = null

    private var pkValue: Any? = null

    override fun where(vararg conditions: Condition): TableDeletePlan {
        if (null == where) {
            where = conditions.reduce(Condition::and)
        }
        else {
            val cond = where
            cond?.andGroup(conditions.reduce(Condition::and))
        }

        return this
    }

    override fun where(value: Any): TableDeletePlan {
        if (null == table.primaryKey) throw DaoException("Table ${table.tableName} should has primary key")

        pkValue = value
        return this
    }

    override val statement: String
        get() {
            var txt = "DELETE FROM ${table.tableName} "

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

    override fun clone(): DefaultTableDeletePlan {
        val plan = DefaultTableDeletePlan(table, params)
        plan.where = where
        return plan
    }
}