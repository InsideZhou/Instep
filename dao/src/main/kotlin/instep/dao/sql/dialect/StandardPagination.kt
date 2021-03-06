package instep.dao.sql.dialect

import instep.dao.sql.Pagination

open class StandardPagination : Pagination {
    override fun parameters(parameters: List<Any?>, limit: Int, offset: Int): List<Any?> {
        if (limit <= 0) {
            return if (offset > 0) parameters + arrayOf(offset) else parameters
        }
        else {
            return if (offset > 0) parameters + arrayOf(limit, offset) else parameters + arrayOf(limit)
        }
    }

    override fun statement(statement: String, limit: Int, offset: Int): String {
        if (limit <= 0) {
            return if (offset > 0) "$statement\n OFFSET ?" else statement
        }
        else {
            return if (offset > 0) "$statement\nLIMIT ? OFFSET ?" else "$statement\nLIMIT ?"
        }
    }
}