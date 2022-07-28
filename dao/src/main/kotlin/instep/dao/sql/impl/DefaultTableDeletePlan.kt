package instep.dao.sql.impl

import instep.dao.DaoException
import instep.dao.sql.Condition
import instep.dao.sql.SubSQLPlan
import instep.dao.sql.Table
import instep.dao.sql.TableDeletePlan

open class DefaultTableDeletePlan(val table: Table) : TableDeletePlan, SubSQLPlan<TableDeletePlan>() {
    override var where: Condition = Condition.empty
    private var pkValue: Any? = null

    override fun whereKey(key: Any): TableDeletePlan {
        if (null == table.primaryKey) throw DaoException("Table ${table.tableName} should has primary key")

        pkValue = key
        return this
    }

    override val statement: String
        get() {
            var txt = "DELETE FROM ${table.tableName} "

            if (where.text.isBlank()) {
                pkValue?.let {
                    txt += "WHERE ${table.primaryKey!!.name}=?"
                }

                return txt
            }

            txt += where.text

            pkValue?.let {
                txt += " AND ${table.primaryKey!!.name}=?"
            }

            return txt
        }

    override val parameters: List<Any?>
        get() {
            val result = mutableListOf<Any?>()

            where.let { result.addAll(it.parameters) }
            pkValue?.let { result.add(it) }

            return result
        }
}