package instep.dao.sql.impl

import instep.dao.DaoException
import instep.dao.sql.*

open class DefaultTableDeletePlan(val table: Table) : TableDeletePlan, SubSQLPlan<TableDeletePlan>() {
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

            val column = table.primaryKey
            txt += if (column is StringColumn && column.type == StringColumnType.UUID) {
                "${table.primaryKey!!.name}=${table.dialect.parameterForUUIDType}"
            }
            else {
                "${table.primaryKey!!.name}=?"
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