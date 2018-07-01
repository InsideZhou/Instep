package instep.dao

interface PlanExecutor {
    fun execute(plan: Plan<*>)
    fun executeScalar(plan: Plan<*>): String
    fun <T : Any> executeScalar(plan: Plan<*>, cls: Class<T>): T?
    fun <T : Any> execute(plan: Plan<*>, cls: Class<T>): List<T>
}