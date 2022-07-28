package instep.dao.impl

import instep.collection.AssocArray
import instep.dao.Expression
import instep.dao.PlaceHolder
import instep.dao.PlaceHolderRemainingException

@Suppress("UNCHECKED_CAST", "MemberVisibilityCanBePrivate")
abstract class AbstractExpression<T : Expression<T>>(val txt: String) : Expression<T> {
    @Suppress("RemoveRedundantQualifierName")
    val rule = PlaceHolder.rule.copy()
    val parameterPlaceHolder = PlaceHolder.parameter

    private val params = AssocArray()

    init {
        rule.placeholder.findAll(txt).forEachIndexed { i, matchResult ->
            val paramName = matchResult.groupValues[1]

            params.add(PlaceHolder(i, paramName))
        }
    }

    override val text: String
        get() {
            var index = 0

            return rule.normalize(rule.placeholder.replace(txt) {
                when (val param = params[index++]) {
                    is Expression<*> -> param.text
                    is PlaceHolder -> if (param.ignore) "" else parameterPlaceHolder
                    else -> parameterPlaceHolder
                }
            })
        }

    override val parameters: List<Any?>
        get() {
            return params.flatMap { item ->
                when (item) {
                    is Expression<*> -> item.parameters
                    else -> listOf(item)
                }
            }
        }

    override fun addParameter(placeholderName: String, parameter: Any?): T {
        params.filter { item -> item is PlaceHolder && item.name == placeholderName }
            .mapIndexed { i, _ -> i }
            .forEach { i -> params[i] = parameter }

        return this as T
    }

    fun addParameters(vararg parameters: Any?): T {
        if (parameters.isNotEmpty()) {
            val remainingPlaceHolderCount = params.count { p -> p is PlaceHolder }

            if (remainingPlaceHolderCount > 0) {
                throw PlaceHolderRemainingException("$remainingPlaceHolderCount placeholders remaining, cannot add positional parameters.")
            }

            params.add(*parameters)
        }

        return this as T
    }

    override fun addExpression(placeHolderName: String, expression: Expression<*>?): T {
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
}