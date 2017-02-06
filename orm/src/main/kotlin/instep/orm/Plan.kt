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
    fun log(): T {
        InstepLogger.info({ statement }, this.javaClass.name)
        InstepLogger.info({ parameters.map { it.toString() }.joinToString("|") }, this.javaClass.name)
        return this as T
    }

    override fun clone(): T
}