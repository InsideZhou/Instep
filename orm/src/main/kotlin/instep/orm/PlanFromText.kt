package instep.orm

import instep.orm.planbuild.DefaultPlanFromText

interface PlanFromText : Plan<PlanFromText>, Expression {
    override fun addParameters(vararg parameters: Any?): PlanFromText
    override fun addParameter(placeholderName: String, parameter: Any?): PlanFromText

    override fun addExpressions(vararg expressions: Expression): PlanFromText
    override fun addExpression(placeHolderName: String, expression: Expression?): PlanFromText

    override fun clone(): PlanFromText

    companion object {
        fun createInstance(txt: String): PlanFromText {
            return DefaultPlanFromText(txt)
        }
    }
}