package instep.dao

interface PlanExecutor<P : Plan<*>> {
    @Throws(DaoException::class)
    fun execute(plan: P)

    @Throws(DaoException::class)
    fun executeScalar(plan: P): String

    @Throws(DaoException::class)
    fun <T : Any> executeScalar(plan: P, cls: Class<T>): T?

    @Throws(DaoException::class)
    fun <T : Any> execute(plan: P, cls: Class<T>): List<T>
}