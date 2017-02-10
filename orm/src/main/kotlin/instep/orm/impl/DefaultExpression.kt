package instep.orm.impl

import instep.collection.AssocArray
import instep.orm.Expression
import instep.orm.PlaceHolder
import instep.orm.PlaceHolderRemainingException

open class DefaultExpression private constructor(
    val txt: String,
    val rule: PlaceHolder.Rule,
    private val params: AssocArray,
    paramsInitRequired: Boolean
) : Expression {
    protected constructor(txt: String, paramsInitRequired: Boolean) : this(txt, PlaceHolder.rule.copy(), AssocArray(), paramsInitRequired)
    constructor(txt: String) : this(txt, true)

    init {
        if (paramsInitRequired) {
            rule.placeholder.findAll(txt).forEachIndexed { i, matchResult ->
                val paramName = matchResult.groupValues[1]

                params.add(PlaceHolder(i, paramName))
            }
        }
    }

    override val expression: String
        get() {
            var index = 0

            return rule.normalize(rule.placeholder.replace(txt, { matchResult ->
                val param = params[index++]
                when (param) {
                    is Expression -> param.expression
                    is PlaceHolder -> if (param.ignore) "" else "?"
                    else -> "?"
                }
            }))
        }

    override val parameters: List<Any?>
        get() {
            return params.flatMap { item ->
                when (item) {
                    is Expression -> item.parameters
                    else -> listOf(item)
                }
            }
        }

    override fun addParameter(placeholderName: String, parameter: Any?): Expression {
        params.filter { item -> item is PlaceHolder && item.name == placeholderName }
            .mapIndexed { i, item -> i }
            .forEach { i -> params[i] = parameter }

        return this
    }

    override fun addParameters(vararg parameters: Any?): Expression {
        val remainingPlaceHolderCount = params.count { p -> p is PlaceHolder }

        if (remainingPlaceHolderCount > 0) {
            throw PlaceHolderRemainingException("$remainingPlaceHolderCount placeholders remaining, cannot add positional parameters.")
        }

        params.add(*parameters)

        return this
    }

    override fun addExpression(placeHolderName: String, expression: Expression?): Expression {
        params.filter { item -> item is PlaceHolder && item.name == placeHolderName }
            .mapIndexed { i, item -> i }
            .forEach { i ->
                if (null == expression) {
                    (params[i] as PlaceHolder).ignore = true
                }
                else {
                    params[i] = expression
                }
            }

        return this
    }

    override fun addExpressions(vararg expressions: Expression): Expression {
        val remainingPlaceHolderCount = params.count { p -> p is PlaceHolder }

        if (remainingPlaceHolderCount > 0) {
            throw PlaceHolderRemainingException("$remainingPlaceHolderCount placeholders remaining, cannot add positional expressions.")
        }

        params.add(*expressions)

        return this
    }

    override fun clone(): Expression {
        return DefaultExpression(txt, rule, params, false)
    }

    companion object {
        private const val serialVersionUID = -6502434497734704298L
    }
}
