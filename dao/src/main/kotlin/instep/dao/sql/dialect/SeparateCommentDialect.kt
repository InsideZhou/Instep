package instep.dao.sql.dialect

import instep.InstepLogger
import instep.dao.sql.Column
import instep.dao.sql.InstepSQL
import instep.dao.sql.SQLPlan


abstract class SeparateCommentDialect : AbstractDialect() {
    private val logger = InstepLogger.getLogger(SeparateCommentDialect::class.java)

    override val separatelyCommenting: Boolean = true

    override fun createTable(tableName: String, tableComment: String, ddl: String, columns: List<Column<*>>): SQLPlan<*> {
        if (columns.isEmpty()) {
            logger.message("Table has no columns.").context("table", tableName).warn()
        }

        val plan = InstepSQL.plan(ddl + definitionForColumns(*columns.toTypedArray()) + "\n)")

        if (tableComment.isNotBlank()) {
            plan.addSubPlan(InstepSQL.plan("\nCOMMENT ON TABLE $tableName IS '$tableComment'"))
        }

        columns
            .filter { it.comment.isNotBlank() }
            .forEach {
                plan.addSubPlan(InstepSQL.plan("\nCOMMENT ON COLUMN $tableName.${it.name} IS '${it.comment}'"))
            }

        return plan
    }
}