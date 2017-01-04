package instep.orm

interface PlanFromText : Plan, Expression {
    override fun addParameters(vararg parameters: Any?): PlanFromText
    override fun addParameter(placeholderName: String, parameter: Any?): PlanFromText

    override fun addExpressions(vararg expressions: Expression): PlanFromText
    override fun addExpression(placeHolderName: String, expression: Expression?): PlanFromText

    override fun clone(): PlanFromText
}