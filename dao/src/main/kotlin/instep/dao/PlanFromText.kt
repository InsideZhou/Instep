package instep.dao

import instep.dao.impl.DefaultPlanFromText

interface PlanFromText : Plan<PlanFromText>, Expression {
    override fun addParameters(vararg parameters: Any?): PlanFromText
    override fun addParameter(placeholderName: String, parameter: Any?): PlanFromText

    override fun addExpressions(vararg expressions: Expression): PlanFromText
    override fun addExpression(placeHolderName: String, expression: Expression?): PlanFromText

    companion object : PlanFromTextFactory {
        override fun createInstance(txt: String): PlanFromText {
            return DefaultPlanFromText(txt)
        }
    }
}

interface PlanFromTextFactory {
    fun createInstance(txt: String): PlanFromText
}