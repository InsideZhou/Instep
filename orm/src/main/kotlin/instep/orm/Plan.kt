package instep.orm

import instep.InstepLogger
import java.io.Serializable

/**
 * Plan that is targeting relational database manipulated by SQL.
 */
interface Plan<T : Plan<T>> : Serializable, Cloneable {
    val statement: String
    /**
     * Order of parameters need to be same as order of statement's placeholders.
     */
    val parameters: List<Any?>

    @Suppress("UNCHECKED_CAST")
    fun debug(): T {
        InstepLogger.debug({ statement }, this.javaClass.name)
        InstepLogger.debug({ parameters.map { it.toString() }.joinToString("|") }, this.javaClass.name)
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun log(runner: () -> Unit): T {
        runner()
        return this as T
    }
}