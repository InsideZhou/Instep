package instep.dao.sql

interface WhereClause<out T : Any> {
    var where: Condition?

    fun where(vararg conditions: Condition): T {
        if (null == where) {
            where = conditions.reduce(Condition::and)
        }
        else {
            val cond = where
            cond?.andGroup(conditions.reduce(Condition::and))
        }

        @Suppress("unchecked_cast") return this as T
    }
}