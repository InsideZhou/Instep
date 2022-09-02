package instep.dao.sql.impl

import instep.dao.DaoException
import instep.dao.sql.Condition
import instep.dao.sql.Table
import instep.dao.sql.TableDeletePlan

open class DefaultTableDeletePlan(val table: Table) : TableDeletePlan, AbstractTablePlan<TableDeletePlan>() {
    override var where: Condition = Condition.empty
    private var pkValue: Any? = null

    override fun whereKey(key: Any): TableDeletePlan {
        if (null == table.primaryKey) throw DaoException("Table ${table.tableName} should has primary key")

        pkValue = key
        return this
    }

    @Suppress("DuplicatedCode")
    override val statement: String
        get() {
            var txt = "DELETE FROM ${table.tableName}"
            if (where.text.isBlank() && null == pkValue) return txt

            txt += " WHERE ${where.text}"
            if (null == pkValue) return txt

            if (where.text.isNotBlank()) {
                txt += " AND "
            }

            val column = table.primaryKey!!
            txt += "${column.name}=${table.dialect.placeholderForParameter(column)}"

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