package instep.dao.sql.dialect

import instep.dao.sql.Column


abstract class CommentInTableDefinitionDialect : AbstractDialect() {
    override val separatelyCommenting: Boolean = false

    override fun definitionForColumn(column: Column<*>): String {
        var txt = super.definitionForColumn(column)

        if (column.comment.isNotBlank()) {
            txt += " COMMENT '${column.comment}'"
        }

        return txt;
    }
}