package instep.dao.sql.impl

import instep.Instep
import instep.dao.DaoException
import instep.dao.sql.*
import instep.typeconversion.JsonType
import instep.typeconversion.TypeConversion

open class DefaultTableUpdatePlan(val table: Table) : TableUpdatePlan, SubSQLPlan<TableUpdatePlan>() {
    protected val params = mutableMapOf<Column<*>, Any?>()

    override var where: Condition? = null

    private var pkValue: Any? = null
    private val typeConversion = Instep.make(TypeConversion::class.java)

    override fun step(column: NumberColumn<*>, value: Number): TableUpdatePlan {
        assertColumnBelongToMe(column)
        params[column] = StepValue(value)

        return this;
    }

    private fun assertColumnBelongToMe(column: Column<*>) {
        if (table.columns.none { it == column }) throw DaoException("Column ${column.name} should belong to Table ${table.tableName}")
    }

    override fun set(column: Column<*>, value: Any?): TableUpdatePlan {
        assertColumnBelongToMe(column)

        when (value) {
            is Enum<*> -> params[column] = if (IntegerColumn::class.java == column.javaClass) value.ordinal else value.name
            else -> params[column] = value
        }

        return this
    }

    override fun set(obj: Any): TableUpdatePlan {
        val mirror = Instep.reflect(obj)
        val tableMirror = Instep.reflect(table)

        mirror.readableProperties.forEach { p ->
            tableMirror.getPropertiesUntil(Table::class.java)
                .find {
                    p.field.name == it.field.name && Column::class.java.isAssignableFrom(it.field.type)
                }?.let {
                    val col = if (null != it.getter) {
                        it.getter!!.invoke(table)
                    }
                    else {
                        it.field.get(table)
                    } as Column<*>

                    if (!col.primary) {
                        params[col] = p.getter.invoke(obj)
                    }
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
            var txt = "UPDATE ${table.tableName} SET ${params.entries.map {
                val column = it.key
                val value = it.value

                val standardSetClause = "${it.key.name}=?"

                when (column) {
                    is StringColumn -> when (column.type) {
                        StringColumnType.UUID -> "${it.key.name}=${table.dialect.placeholderForUUIDType}"
                        StringColumnType.JSON -> "${it.key.name}=${table.dialect.placeholderForJSONType}"
                        else -> standardSetClause
                    }
                    is NumberColumn -> when (value) {
                        is StepValue -> "${it.key.name}=(${it.key.name} + ?)"
                        else -> standardSetClause
                    }
                    else -> standardSetClause
                }

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

                when {
                    (column is StringColumn &&
                        column.type == StringColumnType.JSON &&
                        null != value &&
                        value !is String) ->
                        if (typeConversion.canConvert(value.javaClass, JsonType::class.java)) {
                            typeConversion.convert(value, JsonType::class.java).value
                        }
                        else {
                            value.toString()
                        }

                    value is StepValue -> value.step

                    else -> value
                }
            }.toList()

            where?.let { result += it.parameters }
            pkValue?.let { result += it }

            return result
        }
}