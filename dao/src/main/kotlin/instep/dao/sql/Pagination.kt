package instep.dao.sql

interface Pagination {
    fun parameters(parameters: List<Any?>, limit: Int, offset: Int): List<Any?>
    fun statement(statement: String, limit: Int, offset: Int): String
}
