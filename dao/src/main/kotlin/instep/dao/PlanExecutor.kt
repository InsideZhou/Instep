package instep.dao

import java.time.temporal.Temporal

interface PlanExecutor<P : Plan<*>> {
    fun execute(plan: P)

    fun executeString(plan: P): String

    fun executeLong(plan: P): Long

    fun executeDouble(plan: P): Double

    fun <R : Temporal> executeTemporal(plan: P, cls: Class<R>): R?

    fun <T : Any> execute(plan: P, cls: Class<T>): List<T>
}