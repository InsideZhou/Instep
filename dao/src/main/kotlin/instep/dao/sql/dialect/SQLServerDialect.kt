package instep.dao.sql.dialect

import instep.InstepLogger
import instep.dao.sql.*
import instep.dao.sql.impl.DefaultSQLPlan
import instep.dao.sql.impl.DefaultTableSelectPlan
import microsoft.sql.DateTimeOffset
import java.time.OffsetDateTime

open class SQLServerDialect : SeparateCommentDialect() {
    override val returningClauseForInsert = ""

    private val logger = InstepLogger.getLogger(SQLServerDialect::class.java)

    class ResultSet(private val rs: java.sql.ResultSet) : AbstractDialect.ResultSet(rs) {
        override fun getOffsetDateTime(index: Int): OffsetDateTime? {
            return (rs.getObject(index) as? DateTimeOffset)?.offsetDateTime
        }

        override fun getOffsetDateTime(label: String): OffsetDateTime? {
            return (rs.getObject(label) as? DateTimeOffset)?.offsetDateTime
        }
    }

    class SelectPlan(from: Table) : DefaultTableSelectPlan(from) {
        private val noOrderByButRowsLimited get() = orderBy.isEmpty() && limit > 0

        override val selectWords get() = if (noOrderByButRowsLimited) "${super.selectWords} TOP $limit" else super.selectWords

        override val statement: String
            get() {
                val sql = baseSql + whereTxt + groupByTxt + havingTxt + orderByTxt
                return if (noOrderByButRowsLimited) sql else from.dialect.pagination.statement(sql, limit, offset)
            }

        override val parameters: List<Any?>
            get() {
                val params = where.parameters + having.parameters
                return if (noOrderByButRowsLimited) params else from.dialect.pagination.parameters(params, limit, offset)
            }
    }

    class Pagination : StandardPagination() {
        override fun statement(statement: String, limit: Int, offset: Int): String {
            return if (limit <= 0) {
                if (offset > 0) "$statement\n OFFSET ? ROWS" else statement
            }
            else {
                if (offset > 0) "$statement\nOFFSET ? ROWS\nFETCH NEXT ? ROWS ONLY" else "$statement\nOFFSET 0 ROWS\nFETCH NEXT ? ROWS ONLY"
            }
        }
    }

    override val pagination: instep.dao.sql.Pagination
        get() = Pagination()

    override fun addColumn(column: Column<*>): SQLPlan<*> {
        val columnDefinition = definitionForColumns(column)
        return InstepSQL.plan("ALTER TABLE ${column.table.tableName} ADD $columnDefinition")
    }

    override fun dropColumn(column: Column<*>): SQLPlan<*> {
        return InstepSQL.plan("ALTER TABLE ${column.table.tableName} DROP ${column.name}")
    }

    override fun definitionForBooleanColumn(column: BooleanColumn): String = "BIT"

    override fun definitionForAutoIncrementColumn(column: IntegerColumn): String = when (column.type) {
        IntegerColumnType.Long -> "BIGINT IDENTITY"
        IntegerColumnType.Int -> "INT IDENTITY"
        IntegerColumnType.Small -> "SMALLINT IDENTITY"
        IntegerColumnType.Tiny -> "TINYINT IDENTITY"
    }

    override fun definitionForDateTimeColumn(column: DateTimeColumn): String = when (column.type) {
        DateTimeColumnType.DateTime -> "DATETIME2"
        DateTimeColumnType.Instant -> "DATETIME2"
        DateTimeColumnType.OffsetDateTime -> "DATETIMEOFFSET"
        else -> super.definitionForDateTimeColumn(column)
    }

    override fun definitionForBinaryColumn(column: BinaryColumn): String = when (column.type) {
        BinaryColumnType.BLOB -> "VARBINARY(MAX)"
        else -> super.definitionForBinaryColumn(column)
    }

    override fun definitionForUUIDColumn(column: StringColumn): String = "UNIQUEIDENTIFIER"

    override fun createTableIfNotExists(tableName: String, tableComment: String, columns: List<Column<*>>): SQLPlan<*> {
        val existsTableCheck = DefaultSQLPlan("SELECT name FROM sys.tables WHERE name=\${name}")
            .placeholderToParameter("name", tableName)

        val existsTableName = existsTableCheck.executeScalar()
        if (tableName == existsTableName) return existsTableCheck

        val ddl = "CREATE TABLE $tableName (\n"

        return createTable(tableName, tableComment, ddl, columns)
    }

    override fun createTable(tableName: String, tableComment: String, ddl: String, columns: List<Column<*>>): SQLPlan<*> {
        if (columns.isEmpty()) {
            logger.message("Table has no columns.").context("table", tableName).warn()
        }

        return InstepSQL.plan(ddl + definitionForColumns(*columns.toTypedArray()) + "\n)")
    }
}