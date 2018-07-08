package instep.dao.sql.impl

import instep.Instep
import instep.dao.Expression
import instep.dao.ExpressionFactory
import instep.dao.sql.SQLPlan
import instep.servicecontainer.ServiceNotFoundException

class DefaultSQLPlan(txt: String) : SQLPlan<DefaultSQLPlan>, Expression<DefaultSQLPlan> {
    private val expressionFactory = Instep.make(ExpressionFactory::class.java)
    private val superExpression = expressionFactory.createInstance(txt)

    override fun clone(): DefaultSQLPlan {
        return super<SQLPlan>.clone()
    }

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

    override fun addParameter(placeholderName: String, parameter: Any?): DefaultSQLPlan {
        superExpression.addParameter(placeholderName, parameter)
        return this
    }

    override fun addExpression(placeHolderName: String, expression: Expression<*>?): DefaultSQLPlan {
        superExpression.addExpression(placeHolderName, expression)
        return this
    }

    companion object {
        private const val serialVersionUID = -9202019814173830690L

        init {
            try {
                Instep.make(ExpressionFactory::class.java)
            }
            catch (e: ServiceNotFoundException) {
                Instep.bind(ExpressionFactory::class.java, ExpressionFactory.Companion)
            }
        }
    }
}