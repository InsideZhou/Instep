package instep.orm.impl

import instep.orm.Expression
import instep.orm.PlanFromText

open class DefaultPlanFromText(txt: String) : DefaultExpression(txt), PlanFromText {

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

    override val statement: String
        get() {
            return super.expression
        }

    companion object {
        private const val serialVersionUID = -9202019814173830690L
    }
}