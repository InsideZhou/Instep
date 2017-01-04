package instep.orm.sql

import java.io.Serializable

interface Pagination : Serializable {
    fun parameters(parameters: List<Any?>, limit: Int, offset: Int): List<Any?>
    fun statement(statement: String, limit: Int, offset: Int): String
}
