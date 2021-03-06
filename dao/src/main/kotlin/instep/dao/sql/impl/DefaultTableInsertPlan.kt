package instep.dao.sql.impl

import instep.Instep
import instep.dao.DaoException
import instep.dao.sql.*
import instep.typeconversion.JsonType
import instep.typeconversion.TypeConversion

open class DefaultTableInsertPlan(val table: Table) : TableInsertPlan, SubSQLPlan<TableInsertPlan>() {
    protected val params = mutableMapOf<Column<*>, Any?>()

    private val typeConversion = Instep.make(TypeConversion::class.java)

    override fun addValue(column: Column<*>, value: Any?): TableInsertPlan {
        if (table.columns.none { it == column }) throw DaoException("Column ${column.name} should belong to Table ${table.tableName}")

        setValue(column, value)

        return this
    }

    override fun set(obj: Any): TableInsertPlan {
        val mirror = Instep.reflect(obj)

        mirror.readableProperties.forEach { p ->
            table.columns.find { it.name == p.field.name }?.let {
                setValue(it, p.getter.invoke(obj))
            }
        }

        return this
    }

    private fun setValue(column: Column<*>, value: Any?) {
        when (value) {
            is Enum<*> -> params[column] = if (IntegerColumn::class.java == column.javaClass) value.ordinal else value.name
            else -> params[column] = value
        }
    }

    override val statement: String
        get() {
            var txt = "INSERT INTO ${table.tableName} "
            val columns = params.entries

            txt += columns.map { it.key.name }.joinToString(",", "(", ")")

            txt += "\nVALUES (${columns.map {
                val col = it.key

                if (it.value == Table.DefaultInsertValue) {
                    return@map table.dialect.defaultInsertValue
                }
                else if (col is StringColumn) {
                    if (col.type == StringColumnType.UUID) {
                        return@map table.dialect.placeholderForUUIDType
                    }
                    else if (col.type == StringColumnType.JSON) {
                        return@map table.dialect.placeholderForJSONType
                    }
                }

                "?"
            }.joinToString(",")})"

            return txt
        }

    override val parameters: List<Any?>
        get() = params.filterNot { it.value == Table.DefaultInsertValue }.map {
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
        }
}