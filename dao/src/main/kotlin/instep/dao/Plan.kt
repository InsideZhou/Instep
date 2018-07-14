package instep.dao

import instep.InstepLogger

/**
 * Plan that is targeting relational database manipulated by SQL.
 */
interface Plan<T : Plan<T>> {
    val statement: String
    /**
     * Order of parameters need to be same as order of statement's placeholders.
     */
    val parameters: List<Any?>

    @Suppress("UNCHECKED_CAST")
    fun debug(): T {
        InstepLogger.debug({ "$statement\n${parameterToLogFormat()}" }, this.javaClass.name)
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun info(): T {
        InstepLogger.info({ "$statement\n${parameterToLogFormat()}" }, this.javaClass.name)
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun log(runner: (T) -> Unit): T {
        runner(this as T)
        return this
    }

    fun parameterToLogFormat(): String = parameters.map(Any?::toString).joinToString("|")
}