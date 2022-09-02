package instep.dao.sql.impl

import instep.Instep
import instep.collection.AssocArray
import instep.dao.DaoException
import instep.dao.sql.*
import instep.typeconversion.TypeConversion

@Suppress("MemberVisibilityCanBePrivate")
open class DefaultTableInsertPlan(override val table: Table) : TableInsertPlan, AbstractTablePlan<TableInsertPlan>() {
    protected val params = mutableMapOf<Column<*>, Any?>()

    private val typeConversion = Instep.make(TypeConversion::class.java)

    private var returningRequired = false
    private val returning: AssocArray = AssocArray()


    override fun returning(vararg columnOrAggregates: Any): TableInsertPlan {
        this.returningRequired = true
        this.returning.add(*columnOrAggregates)
        return this
    }

    override fun addValue(column: Column<*>, value: Any?): TableInsertPlan {
        if (column.table != table) throw DaoException("Column ${column.name} should belong to Table ${table.tableName}")

        setValue(column, value)

        return this
    }

    override fun set(obj: Any): TableInsertPlan {
        val tableMirror = Instep.reflect(table)

        val map = (obj as? Map<*, *>)?.apply {
            this.mapKeys { it.key?.toString() }
                .filterKeys { !it.isNullOrBlank() }
                .forEach { (k, v) ->
                    tableMirror.getPropertiesUntil(Table::class.java)
                        .find {
                            k == it.field.name && Column::class.java.isAssignableFrom(it.field.type)
                        }?.let {
                            val col = if (null != it.getter) {
                                it.getter!!.invoke(table)
                            }
                            else {
                                it.field.get(table)
                            }

                            setValue(col as Column<*>, v)
                        }
                }
        }

        if (null == map) {
            Instep.reflect(obj).readableProperties.forEach { p ->
                tableMirror.getPropertiesUntil(Table::class.java)
                    .filter { Column::class.java.isAssignableFrom(it.field.type) }
                    .find { p.field.name == it.field.name }
                    ?.let {
                        val col = if (null != it.getter) {
                            it.getter!!.invoke(table)
                        }
                        else {
                            it.field.get(table)
                        }

                        if (col is IntegerColumn && col.primary && col.autoIncrement) {
                            setValue(col as Column<*>, Table.DefaultInsertValue)
                        }
                        else {
                            setValue(col as Column<*>, p.getter.invoke(obj))
                        }
                    }
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

            txt += columns.joinToString(",", "(", ")") { it.key.name }

            txt += "\nVALUES (${
                columns.map {
                    val col = it.key

                    if (it.value == Table.DefaultInsertValue) {
                        return@map table.dialect.defaultValueForInsert
                    }

                    when (col) {
                        is ArbitraryColumn -> it.value?.toString()
                        else -> table.dialect.placeholderForParameter(col)
                    }
                }.joinToString(",")
            })"

            val returningColumns = returning.filterNotNull()
            if (returningRequired && table.dialect.returningClauseForInsert.isNotEmpty()) {
                txt += if (returningColumns.isEmpty()) {
                    " " + table.dialect.returningClauseForInsert
                }
                else {
                    " RETURNING " + returningColumns.joinToString(",") {
                        when (it) {
                            is Column<*> -> it.name
                            is String -> it
                            else -> throw DaoException("Expression for RETURNING must be Column or Aggregate, now got ${it.javaClass.name}.")
                        }
                    }
                }
            }

            return txt
        }

    override val parameters: List<Any?>
        get() = params.filterNot { it.value == Table.DefaultInsertValue }.map {
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

                else -> value
            }
        }.filterNot { it is Unit }
}
