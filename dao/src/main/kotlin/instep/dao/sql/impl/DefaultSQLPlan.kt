package instep.dao.sql.impl

import instep.dao.Expression
import instep.dao.impl.DefaultExpression
import instep.dao.sql.SQLPlan
import instep.dao.sql.SubSQLPlan

class DefaultSQLPlan(txt: String) : SQLPlan<DefaultSQLPlan>, Expression<DefaultSQLPlan>, SubSQLPlan<DefaultSQLPlan>() {
    private val superExpression = DefaultExpression(txt)

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

    override fun placeholderToParameter(placeholderName: String, parameter: Any?): DefaultSQLPlan {
        superExpression.placeholderToParameter(placeholderName, parameter)
        return this
    }

    override fun placeholderToExpression(placeHolderName: String, expression: Expression<*>?): DefaultSQLPlan {
        superExpression.placeholderToExpression(placeHolderName, expression)
        return this
    }
}