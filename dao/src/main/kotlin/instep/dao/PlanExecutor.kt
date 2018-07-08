package instep.dao

interface PlanExecutor {
    @Throws(DaoException::class)
    fun execute(plan: Plan<*>)

    @Throws(DaoException::class)
    fun executeScalar(plan: Plan<*>): String

    @Throws(DaoException::class)
    fun <T : Any> executeScalar(plan: Plan<*>, cls: Class<T>): T?

    @Throws(DaoException::class)
    fun <T : Any> execute(plan: Plan<*>, cls: Class<T>): List<T>
}