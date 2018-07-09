package instep.dao

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
    public override fun clone(): T {
        return super.clone() as T
    }

    @Suppress("UNCHECKED_CAST")
    fun debug(): T {
        val self = this
        InstepLogger.debug({ statement }, self.javaClass.name)
        InstepLogger.debug({ parameters.map(Any?::toString).joinToString("|") }, self.javaClass.name)
        return self as T
    }

    @Suppress("UNCHECKED_CAST")
    fun log(runner: (T) -> Unit): T {
        runner(this as T)
        return this
    }
}