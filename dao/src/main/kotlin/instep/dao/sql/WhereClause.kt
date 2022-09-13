package instep.dao.sql

interface WhereClause<out T> {
    var where: Condition

    fun where(vararg conditions: Condition): T {
        where = if (where.text.isBlank()) {
            conditions.reduce(Condition::and)
        }
        else {
            where.and(conditions.reduce(Condition::and).grouped())
        }

        @Suppress("unchecked_cast") return this as T
    }
}