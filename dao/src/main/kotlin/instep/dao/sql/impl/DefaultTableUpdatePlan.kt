package instep.dao.sql.impl

import instep.Instep
import instep.dao.DaoException
import instep.dao.sql.*
import instep.typeconversion.JsonType
import instep.typeconversion.TypeConversion

open class DefaultTableUpdatePlan(val table: Table) : TableUpdatePlan {
    protected val params = mutableMapOf<Column<*>, Any?>()

    override var where: Condition? = null

    private var pkValue: Any? = null
    private val typeConversion = Instep.make(TypeConversion::class.java)

    override fun set(column: Column<*>, value: Any?): TableUpdatePlan {
        if (table.columns.none { it == column }) throw DaoException("Column ${column.name} should belong to Table ${table.tableName}")

        params[column] = value

        return this
    }

    override fun set(obj: Any): TableUpdatePlan {
        val mirror = Instep.reflect(obj)

        mirror.readableProperties.forEach { p ->
            table.columns.find { it.name == p.field.name }?.let {
                params[it] = p.getter.invoke(obj)
            }
        }

        return this
    }

    override fun whereKey(key: Any): TableUpdatePlan {
        if (null == table.primaryKey) throw DaoException("Table ${table.tableName} should has primary key")

        pkValue = key
        return this
    }

    override val statement: String
        get() {
            val columns = params.entries

            var txt = "UPDATE ${table.tableName} SET ${columns.map {
                val column = it.key

                if (column is StringColumn) {
                    if (column.type == StringColumnType.UUID) {
                        return@map "${it.key.name}=${table.dialect.placeholderForUUIDType}"
                    }
                    else if (column.type == StringColumnType.JSON) {
                        return@map "${it.key.name}=${table.dialect.placeholderForJSONType}"
                    }
                }

                "${it.key.name}=?"
            }.joinToString(",")} "

            if (null == where) {
                pkValue?.let {
                    val column = table.primaryKey

                    if (column is StringColumn && column.type == StringColumnType.UUID) {
                        txt += "WHERE ${table.primaryKey!!.name}=${table.dialect.placeholderForUUIDType}"
                    }
                    else {
                        txt += "WHERE ${table.primaryKey!!.name}=?"
                    }
                }

                return txt
            }

            where!!.expression.let {
                if (it.isNotBlank()) {
                    txt += "WHERE $it"
                }
            }

            pkValue?.let {
                val column = table.primaryKey

                if (column is StringColumn && column.type == StringColumnType.UUID) {
                    txt += " AND ${table.primaryKey!!.name}=${table.dialect.placeholderForUUIDType}"
                }
                else {
                    txt += " AND ${table.primaryKey!!.name}=?"
                }
            }

            return txt
        }

    override val parameters: List<Any?>
        get() {
            var result = params.map {
                val column = it.key
                val value = it.value

                if (column is StringColumn &&
                    column.type == StringColumnType.JSON &&
                    null != value &&
                    value !is String) {

                    if (typeConversion.canConvert(value.javaClass, JsonType::class.java)) {
                        typeConversion.convert(value, JsonType::class.java).value
                    }
                    else {
                        value.toString()
                    }
                }
                else {
                    value
                }
            }.toList()

            where?.let { result += it.parameters }
            pkValue?.let { result += it }

            return result
        }
}