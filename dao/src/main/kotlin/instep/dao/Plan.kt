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
        InstepLogger.getLogger(this.javaClass).message(statement).context("parameters") { parameterToLogFormat() }.debug()
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun info(): T {
        InstepLogger.getLogger(this.javaClass).message(statement).context("parameters") { parameterToLogFormat() }.info()
        return this as T
    }

    fun parameterToLogFormat(): String = parameters.joinToString("|", transform = Any?::toString)
}