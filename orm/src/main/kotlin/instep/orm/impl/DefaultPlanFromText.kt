package instep.orm.impl

import instep.orm.Expression
import instep.orm.PlanFromText

open class DefaultPlanFromText private constructor(txt: String, paramsInitRequired: Boolean) : DefaultExpression(txt, paramsInitRequired), PlanFromText {
    constructor(txt: String) : this(txt, true)

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
        return DefaultPlanFromText(txt, false)
    }

    override val statement: String
        get() {
            return super.expression
        }

    companion object {
        private const val serialVersionUID = -9202019814173830690L
    }
}