package instep.dao.impl

import instep.dao.Expression
import instep.dao.PlanFromText

open class DefaultPlanFromText(txt: String) : AbstractPlan<PlanFromText>(), PlanFromText {
    protected val superExpression = DefaultExpression(txt)

    override val parameters: List<Any?>
        get() = superExpression.parameters

    override val statement: String
        get() {
            return superExpression.expression
        }

    override val expression: String
        get() {
            return superExpression.expression
        }

    override fun addParameter(placeholderName: String, parameter: Any?): PlanFromText {
        superExpression.addParameter(placeholderName, parameter)
        return this
    }

    override fun addExpression(placeHolderName: String, expression: Expression<*>?): PlanFromText {
        superExpression.addExpression(placeHolderName, expression)
        return this
    }

    companion object {
        private const val serialVersionUID = -9202019814173830690L
    }
}