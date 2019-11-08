package instep.dao

interface PlanExecutor<P : Plan<*>> {
    fun execute(plan: P)

    fun executeScalar(plan: P): String

    fun <T : Any> executeScalar(plan: P, cls: Class<T>): T?

    fun <T : Any> execute(plan: P, cls: Class<T>): List<T>
}