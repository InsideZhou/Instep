package instep.orm

import java.io.Serializable

interface Expression : Serializable, Cloneable {
    val expression: String
    /**
     * Order of parameters need to be same as order of placeholders.
     */
    val parameters: List<Any?>

    fun addParameter(placeholderName: String, parameter: Any?): Expression
    fun addParameters(vararg parameters: Any?): Expression

    fun addExpression(placeHolderName: String, expression: Expression?): Expression
    fun addExpressions(vararg expressions: Expression): Expression

    override public fun clone(): Expression
}

class PlaceHolder(val index: Int, val name: String, var ignore: Boolean = false) {
    companion object {

        /**
         * Will take matched group 1 as placeholder name. group size must be 2.
         */
        var regex = Regex(""":(\w+)""")
    }
}

