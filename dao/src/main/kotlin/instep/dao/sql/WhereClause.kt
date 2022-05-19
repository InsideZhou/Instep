package instep.dao.sql

interface WhereClause<out T> {
    var where: Condition?

    fun where(vararg conditions: Condition): T {
        where = if (null == where) {
            conditions.reduce(Condition::and)
        }
        else {
            where!!.andGroup(conditions.reduce(Condition::and))
        }

        @Suppress("unchecked_cast") return this as T
    }
}