package instep.dao.impl

import instep.collection.AssocArray
import instep.dao.Expression
import instep.dao.PlaceHolder
import instep.dao.PlaceHolderRemainingException

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
            return params.flatMap { item ->
                when (item) {
                    is Expression<*> -> item.parameters
                    else -> listOf(item)
                }
            }
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
        if (parameters.isNotEmpty()) {
            val remainingPlaceHolderCount = params.count { p -> p is PlaceHolder }

            if (remainingPlaceHolderCount > 0) {
                throw PlaceHolderRemainingException("$remainingPlaceHolderCount placeholders remaining, cannot add positional parameters.")
            }

            params.add(*parameters)
        }

        return this as T
    }
}

open class DefaultExpression(txt: String) : AbstractExpression<DefaultExpression>(txt)
