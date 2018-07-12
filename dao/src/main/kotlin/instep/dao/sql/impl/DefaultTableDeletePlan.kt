package instep.dao.sql.impl

import instep.dao.DaoException
import instep.dao.sql.Column
import instep.dao.sql.Condition
import instep.dao.sql.Table
import instep.dao.sql.TableDeletePlan

open class DefaultTableDeletePlan(val table: Table, val params: MutableMap<Column<*>, Any?> = mutableMapOf()) : TableDeletePlan {
    override var where: Condition? = null

    private var pkValue: Any? = null

    override fun where(key: Any): TableDeletePlan {
        if (null == table.primaryKey) throw DaoException("Table ${table.tableName} should has primary key")

        pkValue = key
        return this
    }

    override val statement: String
        get() {
            var txt = "DELETE FROM ${table.tableName} "

            if (null == where) {
                pkValue?.let {
                    txt += "WHERE ${table.primaryKey!!.name}=?"
                }

                return txt
            }

            where!!.expression.let {
                if (it.isNotBlank()) {
                    txt += "WHERE $it"
                }
            }

            pkValue?.let {
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