package instep.orm

import instep.InstepLogger
import java.io.Serializable

/**
 * Plan that is targeting relational database manipulated by SQL.
 */
interface Plan : Serializable, Cloneable {
    val statement: String
    /**
     * Order of parameters need to be same as order of statement's placeholders.
     */
    val parameters: List<Any?>

    fun log(): Plan {
        InstepLogger.info(statement)
        InstepLogger.info(parameters.map { it.toString() }.joinToString("|"))
        return this
    }

    override fun clone(): Plan
}