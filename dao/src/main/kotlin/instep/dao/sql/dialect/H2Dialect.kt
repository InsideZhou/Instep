package instep.dao.sql.dialect

import instep.InstepLogger
import instep.UnexpectedCodeError
import instep.dao.DaoException
import instep.dao.Plan
import instep.dao.sql.*

open class H2Dialect : Dialect {
    override fun createTable(tableName: String, columns: List<Column<*>>): Plan<*> {
        val ddl = "CREATE TABLE IF NOT EXISTS $tableName (\n"

        if (columns.isEmpty()) {
            InstepLogger.warning({ "Table $tableName has no columns." }, this.javaClass.name)
        }

        return InstepSQL.plan(ddl + definitionForColumns(*columns.toTypedArray()) + "\n)")
    }

    override fun addColumn(tableName: String, column: Column<*>): Plan<*> {
        val columnDefinition = definitionForColumns(column)
        return InstepSQL.plan("ALTER TABLE $tableName ADD COLUMN$columnDefinition")
    }

    override val pagination: Pagination = StandardPagination()

    protected open fun definitionForBooleanColumn(column: BooleanColumn): String = "BOOLEAN"

    protected open fun definitionForAutoIncrementColumn(column: IntegerColumn): String = "IDENTITY"

    protected open fun definitionForIntegerColumn(column: IntegerColumn): String {
        return when (column.type) {
            IntegerColumnType.Tiny -> "TINYINT"
            IntegerColumnType.Small -> "SMALLINT"
            IntegerColumnType.Int -> "INT"
            IntegerColumnType.Long -> "BIGINT"
        }
    }

    protected open fun definitionForStringColumn(column: StringColumn): String {
        return when (column.type) {
            StringColumnType.Char -> "CHAR(${column.length})"
            StringColumnType.Varchar -> "VARCHAR(${column.length})"
            StringColumnType.Text -> if (column.length > 0) "TEXT(${column.length})" else "TEXT"
        }
    }

    protected open fun definitionForFloatingColumn(column: FloatingColumn): String {
        return when (column.type) {
            FloatingColumnType.Float -> "REAL"
            FloatingColumnType.Double -> "DOUBLE"
            FloatingColumnType.Numeric -> "NUMERIC(${column.precision},${column.scale})"
        }
    }

    protected open fun definitionForDateTimeColumn(column: DateTimeColumn): String {
        return when (column.type) {
            DateTimeColumnType.Date -> "DATE"
            DateTimeColumnType.Time -> "TIME"
            DateTimeColumnType.DateTime -> "TIMESTAMP"
            DateTimeColumnType.OffsetDateTime -> throw DaoException("DateTimeColumn.OffsetDateTime is not support")
        }
    }

    protected open fun definitionForBinaryColumn(column: BinaryColumn): String {
        return when (column.type) {
            BinaryColumnType.Varying -> if (column.length > 0) "BINARY(${column.length})" else "BINARY"
            BinaryColumnType.BLOB -> if (column.length > 0) "BLOB(${column.length})" else "BLOB"
        }
    }

    private fun definitionForColumns(vararg columns: Column<*>): String {
        return columns.map {
            var txt = "\t${it.name}"

            txt += " " + when (it) {
                is BooleanColumn -> definitionForBooleanColumn(it)
                is StringColumn -> definitionForStringColumn(it)
                is IntegerColumn ->
                    if (it.autoIncrement) {
                        definitionForAutoIncrementColumn(it)
                    }
                    else {
                        definitionForIntegerColumn(it)
                    }
                is FloatingColumn -> definitionForFloatingColumn(it)
                is DateTimeColumn -> definitionForDateTimeColumn(it)
                is BinaryColumn -> definitionForBinaryColumn(it)
                else -> throw UnexpectedCodeError()
            }

            if (!it.nullable) {
                txt += " NOT NULL"
            }

            if (it.default.isNotBlank()) {
                txt += " DEFAULT ${it.default}"
            }

            if (it.primary) {
                txt += " PRIMARY KEY"
            }

            return@map txt
        }.joinToString(",\n")
    }
}