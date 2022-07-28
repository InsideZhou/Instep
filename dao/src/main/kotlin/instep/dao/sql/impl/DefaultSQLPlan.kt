package instep.dao.sql.impl

import instep.Instep
import instep.dao.Expression
import instep.dao.ExpressionFactory
import instep.dao.sql.SQLPlan
import instep.dao.sql.SubSQLPlan

class DefaultSQLPlan(txt: String) : SQLPlan<DefaultSQLPlan>, Expression<DefaultSQLPlan>, SubSQLPlan<DefaultSQLPlan>() {
    private val expressionFactory = Instep.make(ExpressionFactory::class.java)
    private val superExpression = expressionFactory.createInstance(txt)

    override val parameters: List<Any?>
        get() = superExpression.parameters

    override val statement: String
        get() {
            return superExpression.text
        }

    override val text: String
        get() {
            return superExpression.text
        }

    override fun addParameter(placeholderName: String, parameter: Any?): DefaultSQLPlan {
        superExpression.addParameter(placeholderName, parameter)
        return this
    }

    override fun addExpression(placeHolderName: String, expression: Expression<*>?): DefaultSQLPlan {
        superExpression.addExpression(placeHolderName, expression)
        return this
    }
}