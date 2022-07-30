package instep.dao

import instep.InstepLogger

/**
 * Plan that is targeting relational database manipulated by SQL.
 */
interface Plan<T : Plan<T>> {
    val logger: InstepLogger

    val statement: String

    /**
     * Order of parameters need to be same as order of statement's placeholders.
     */
    val parameters: List<Any?>

    @Suppress("UNCHECKED_CAST")
    fun trace(): T {
        logger.message(statement).context("parameters") { parameterToLogFormat() }.trace()
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun debug(): T {
        logger.message(statement).context("parameters") { parameterToLogFormat() }.debug()
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun info(): T {
        logger.message(statement).context("parameters") { parameterToLogFormat() }.info()
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun warn(): T {
        logger.message(statement).context("parameters") { parameterToLogFormat() }.warn()
        return this as T
    }

    fun parameterToLogFormat(): String = parameters.joinToString("|", transform = Any?::toString)
}