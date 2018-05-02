package instep.dao.sql.impl

import instep.Instep
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

    override fun set(obj: Any): TableUpdatePlan {
        val mirror = Instep.reflect(obj)
        mirror.fieldsWithGetter.forEach { field ->
            table.columns.find { it.name == field.name }?.apply {
                params.put(this, mirror.findGetter(field.name)!!.invoke(obj))
            }
        }

        return this
    }

    override fun where(key: Any): TableUpdatePlan {
        if (null == table.primaryKey) throw DaoException("Table ${table.tableName} should has primary key")

        pkValue = key
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

            where!!.expression.let {
                if (it.isNotBlank()) {
                    txt += "WHERE $it"
                }
            }

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