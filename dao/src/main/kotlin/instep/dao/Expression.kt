package instep.dao

import instep.dao.impl.DefaultExpression
import java.io.Serializable

interface Expression<T : Expression<T>> : Serializable, Cloneable {
    val expression: String
    val parameters: List<Any?>

    fun addParameter(placeholderName: String, parameter: Any?): T
    fun addExpression(placeHolderName: String, expression: Expression<*>?): T

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
        var rule = Rule(Regex("""(?<!\\)\$\{(\w+)\}"""), Regex("""\\$"""), "$")
    }

    data class Rule(val placeholder: Regex, val normalizer: Regex, val replacement: String) {
        fun normalize(txt: String): String {
            return normalizer.replace(txt, replacement)
        }
    }
}
