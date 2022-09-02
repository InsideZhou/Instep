package instep.dao.sql.impl

import instep.Instep
import instep.dao.DaoException
import instep.dao.sql.*
import instep.typeconversion.Converter
import instep.typeconversion.TypeConversion

open class DefaultTableUpdatePlan(val table: Table) : TableUpdatePlan, AbstractTablePlan<TableUpdatePlan>() {
    protected open val params = mutableMapOf<Column<*>, Any?>()

    override var where: Condition = Condition.empty

    private var pkValue: Any? = null
    private val typeConversion = Instep.make(TypeConversion::class.java)

    override fun step(column: NumberColumn<*>, value: Number): TableUpdatePlan {
        assertColumnBelongToMe(column)
        params[column] = StepValue(value)

        return this
    }

    private fun assertColumnBelongToMe(column: Column<*>) {
        if (column.table != table) throw DaoException("Column ${column.name} should belong to Table ${table.tableName}")
    }

    override fun set(column: Column<*>, value: Any?): TableUpdatePlan {
        assertColumnBelongToMe(column)

        when (value) {
            is Enum<*> -> params[column] = if (IntegerColumn::class.java == column.javaClass) value.ordinal else value.name
            else -> params[column] = value
        }

        return this
    }

    @Suppress("UNCHECKED_CAST")
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
                        val value = p.getter.invoke(obj)
                        if (null == value) {
                            params[col] = null
                            return@forEach
                        }

                        val getterType = p.getter.returnType
                        (typeConversion.getConverter(getterType, String::class.java, col.qualifiedName) as? Converter<Any, String>)?.let { converter ->
                            params[col] = converter.convert(value)
                            return@forEach
                        }

                        params[col] = value
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

    @Suppress("DuplicatedCode")
    override val statement: String
        get() {
            var txt = "UPDATE ${table.tableName} SET ${
                params.entries.joinToString(",") {
                    val column = it.key
                    val value = it.value

                    val standardSetClause = "${column.name}=${table.dialect.placeholderForParameter(column)}"

                    when (column) {
                        is NumberColumn -> when (value) {
                            is StepValue -> "${it.key.name}=(${it.key.name} + ?)"
                            else -> standardSetClause
                        }

                        is ArbitraryColumn -> "${it.key.name}=${it.value}"

                        else -> standardSetClause
                    }

                }
            }"

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
            val result = params
                .map {
                    val column = it.key
                    val value = it.value

                    when {
                        (column is StringColumn && null != value && value !is String) -> {
                            typeConversion.getConverter(value.javaClass, String::class.java)?.convert(value) ?: value.toString()
                        }

                        column is IntegerColumn && value is String -> when (column.type) {
                            IntegerColumnType.Tiny -> value.toByte()
                            IntegerColumnType.Small -> value.toShort()
                            IntegerColumnType.Int -> value.toInt()
                            IntegerColumnType.Long -> value.toLong()
                        }

                        column is FloatingColumn && value is String -> when (column.type) {
                            FloatingColumnType.Double -> value.toDouble()
                            FloatingColumnType.Float -> value.toFloat()
                            else -> value
                        }

                        column is BooleanColumn && value is String -> value.toBoolean()
                        column is ArbitraryColumn -> Unit
                        value is StepValue -> value.step

                        else -> value
                    }
                }
                .filterNot { it is Unit }
                .toMutableList()

            where.let { result.addAll(it.parameters) }
            pkValue?.let { result.add(it) }

            return result
        }
}