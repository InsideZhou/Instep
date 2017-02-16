package instep.dao.sql

interface OrderBy {
    val column: Column<*>
    val descending: Boolean
    val nullFirst: Boolean
}