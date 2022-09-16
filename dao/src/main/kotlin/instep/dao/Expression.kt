package instep.dao

import instep.collection.AssocArray

interface Expression<T : Expression<T>> {
    val text: String
    val parameters: List<Any?>

    fun placeholderToParameter(placeholderName: String, parameter: Any?): T
    fun placeholderToExpression(placeHolderName: String, expression: Expression<*>?): T
    fun placeholderToExpression(placeHolderName: String, expression: String): T {
        return placeholderToExpression(placeHolderName, DefaultExpression(expression))
    }

    fun parameterToLogFormat(): String = parameters.joinToString("|", transform = Any?::toString)
}

interface Alias<out T> {
    var alias: String

    fun alias(alias: String): T {
        this.alias = alias
        @Suppress("unchecked_cast") return this as T
    }
}

@Suppress("RegExpRedundantEscape")
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

@Suppress("UNCHECKED_CAST")
abstract class AbstractExpression<T : Expression<T>>(private val txt: String) : Expression<T> {
    protected open val parameterPlaceHolder = PlaceHolder.parameter

    private val params = AssocArray()

    init {
        PlaceHolder.rule.placeholder.findAll(txt).forEachIndexed { i, matchResult ->
            val paramName = matchResult.groupValues[1]

            params.add(PlaceHolder(i, paramName))
        }
    }

    override val text: String
        get() {
            var index = 0

            return PlaceHolder.rule.normalize(PlaceHolder.rule.placeholder.replace(txt) {
                when (val param = params[index++]) {
                    is Expression<*> -> param.text
                    is PlaceHolder -> if (param.ignore) "" else parameterPlaceHolder
                    else -> parameterPlaceHolder
                }
            })
        }

    override val parameters: List<Any?>
        get() {
            return params.filterNot { it is Expression<*> }
        }

    override fun placeholderToParameter(placeholderName: String, parameter: Any?): T {
        params.mapIndexed { i, item -> Pair(i, item) }
            .filter { pair ->
                val item = pair.second
                item is PlaceHolder && item.name == placeholderName
            }
            .forEach { pair -> params[pair.first] = parameter }

        return this as T
    }

    override fun placeholderToExpression(placeHolderName: String, expression: Expression<*>?): T {
        params.mapIndexed { i, item -> Pair(i, item) }
            .filter {
                val p = it.second
                return@filter p is PlaceHolder && p.name == placeHolderName
            }
            .forEach {
                if (null == expression) {
                    (params[it.first] as PlaceHolder).ignore = true
                }
                else {
                    params[it.first] = expression
                }
            }

        return this as T
    }

    open fun addParameters(vararg parameters: Any?): T {
        params.add(*parameters)

        return this as T
    }

    override fun toString(): String {
        return """${text}\n${parameterToLogFormat()}"""
    }
}

open class DefaultExpression(txt: String) : AbstractExpression<DefaultExpression>(txt)
