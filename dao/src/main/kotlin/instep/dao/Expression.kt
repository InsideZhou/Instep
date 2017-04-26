package instep.dao

import instep.dao.impl.DefaultExpression
import java.io.Serializable

interface Expression<T : Expression<T>> : Serializable, Cloneable {
    val expression: String
    /**
     * Order of parameters need to be same as order of placeholders.
     */
    val parameters: List<Any?>

    fun addParameter(placeholderName: String, parameter: Any?): T

    /**
     * @throws PlaceHolderRemainingException Cannot add positional parameter while named parameter with placeholder remaining.
     */
    @Throws(PlaceHolderRemainingException::class)
    fun addParameters(vararg parameters: Any?): T

    fun addExpression(placeHolderName: String, expression: Expression<*>?): T

    /**
     * @throws PlaceHolderRemainingException Cannot add positional expression while named expression with placeholder remaining.
     */
    @Throws(PlaceHolderRemainingException::class)
    fun addExpressions(vararg expressions: Expression<*>): T

    companion object : ExpressionFactory {
        override fun createInstance(txt: String): Expression<*> {
            return DefaultExpression(txt)
        }
    }
}

interface ExpressionFactory {
    fun createInstance(txt: String): Expression<*>
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
