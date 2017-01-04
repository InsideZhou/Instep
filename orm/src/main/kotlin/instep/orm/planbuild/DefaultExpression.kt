package instep.orm.planbuild

import instep.orm.Expression
import instep.orm.OrmException
import instep.orm.PlaceHolder

open class DefaultExpression protected constructor(
    val txt: String,
    val placeholder: Regex,
    protected val params: MutableList<Any?>
) : Expression {
    constructor(txt: String) : this(txt, PlaceHolder.regex, mutableListOf())

    init {
        if (params.isEmpty()) {
            placeholder.findAll(txt).forEachIndexed { i, matchResult ->
                val paramName = matchResult.groupValues[1]

                params.add(PlaceHolder(i, paramName))
            }
        }
    }

    override val expression: String
        get() {
            var index = 0
            return placeholder.replace(txt, { matchResult ->
                val param = params[index++]
                when (param) {
                    is Expression -> param.expression
                    is PlaceHolder -> if (param.ignore) "" else "?"
                    else -> "?"
                }
            })
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
            throw OrmException("$remainingPlaceHolderCount placeholders remaining, cannot add positional expressions.")
        }

        params.addAll(parameters)

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
            throw OrmException("$remainingPlaceHolderCount placeholders remaining, cannot add positional expressions.")
        }

        params.addAll(expressions)

        return this
    }

    override fun clone(): Expression {
        val c = DefaultExpression(txt, placeholder, params)
        return c
    }

    companion object {
        private const val serialVersionUID = -6502434497734704298L
    }
}
