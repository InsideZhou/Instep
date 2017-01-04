package instep.orm.planbuild

import instep.orm.Expression
import instep.orm.PlaceHolder
import instep.orm.PlanFromText

open class DefaultPlanFromText protected constructor(
    txt: String,
    placeholder: Regex,
    params: MutableList<Any?>
) : DefaultExpression(txt, placeholder, params), PlanFromText {
    constructor(txt: String) : this(txt, PlaceHolder.regex, mutableListOf())

    override fun addExpression(placeHolderName: String, expression: Expression?): PlanFromText {
        super.addExpression(placeHolderName, expression)
        return this
    }

    override fun addExpressions(vararg expressions: Expression): PlanFromText {
        super.addExpressions(*expressions)
        return this
    }

    override fun addParameter(placeholderName: String, parameter: Any?): PlanFromText {
        super.addParameter(placeholderName, parameter)
        return this
    }

    override fun addParameters(vararg parameters: Any?): PlanFromText {
        super.addParameters(*parameters)
        return this
    }

    override fun clone(): PlanFromText {
        val c = DefaultPlanFromText(txt, placeholder, params)
        return c
    }

    override val statement: String
        get() {
            return super.expression
        }

    companion object {
        private const val serialVersionUID = -9202019814173830690L
    }
}