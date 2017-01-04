package instep.orm

interface PlanExecutor {
    fun execute(plan: Plan);
    fun executeScalar(plan: Plan): String;
    fun <T : Any> execute(plan: Plan, cls: Class<T>): List<T>;
}