package instep.dao

interface Expression<T : Expression<T>> {
    val text: String
    val parameters: List<Any?>

    fun placeholderToParameter(placeholderName: String, parameter: Any?): T
    fun placeholderToExpression(placeHolderName: String, expression: Expression<*>?): T
}

interface Alias<out T> {
    var alias: String

    fun alias(alias: String): T {
        this.alias = alias
        @Suppress("unchecked_cast") return this as T
    }
}

class PlaceHolder(val index: Int, val name: String, var ignore: Boolean = false) {
    companion object {

        /**
         * Will take matched group 1 as placeholder name. group size must be 2.
         * Normalizer must be changed together while changing placeholder.
         */
        var rule = Rule(Regex("""(?<!\\)\$\{(\w+)\}"""), Regex("""\\$"""), "$")
        var parameter = "?"
    }

    data class Rule(val placeholder: Regex, val normalizer: Regex, val replacement: String) {
        fun normalize(txt: String): String {
            return normalizer.replace(txt, replacement)
        }
    }
}
