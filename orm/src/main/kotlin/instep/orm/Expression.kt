package instep.orm

import java.io.Serializable

interface Expression : Serializable, Cloneable {
    val expression: String
    /**
     * Order of parameters need to be same as order of placeholders.
     */
    val parameters: List<Any?>

    fun addParameter(placeholderName: String, parameter: Any?): Expression

    /**
     * Cannot add positional parameter while named parameter with placeholder remaining.
     *
     * @throws PlaceHolderRemainingException
     */
    @Throws(PlaceHolderRemainingException::class)
    fun addParameters(vararg parameters: Any?): Expression

    fun addExpression(placeHolderName: String, expression: Expression?): Expression

    /**
     * Cannot add positional parameter while named parameter with placeholder remaining.
     *
     * @throws PlaceHolderRemainingException
     */
    @Throws(PlaceHolderRemainingException::class)
    fun addExpressions(vararg expressions: Expression): Expression

    override public fun clone(): Expression
}

class PlaceHolder(val index: Int, val name: String, var ignore: Boolean = false) {
    companion object {

        /**
         * Will take matched group 1 as placeholder name. group size must be 2.
         * Normalizer must be changed together while changing placeholder.
         */
        var rule = Rule(Regex("""(?<!\\):(\w+)"""), Regex("""\\:"""), ":")
    }

    data class Rule(val placeholder: Regex, val normalizer: Regex, val replacement: String) {
        fun normalize(txt: String): String {
            return normalizer.replace(txt, replacement)
        }
    }
}
