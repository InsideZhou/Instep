package instep.dao

import instep.Instep
import instep.dao.impl.AbstractExpression
import instep.dao.sql.Condition
import instep.servicecontainer.ServiceNotFoundException
import org.testng.Assert
import org.testng.annotations.Test

object ExpressionTest {
    @Test
    fun placeholder() {
        val factory = Instep.make(ExpressionFactory::class.java)
        val expression = factory.createInstance("name = :name AND age >= :age AND :condition")
        assert(expression.parameters.all { it is PlaceHolder })

        expression.addParameter("name", "ZhangFei")

        if (expression is AbstractExpression) {
            Assert.assertThrows(PlaceHolderRemainingException::class.java, { expression.addParameters(18) })
        }

        val condition = Condition.isNotNull("army").and(Condition.gte("elite", 100))
        expression.addExpression("condition", condition)
        assert(expression.parameters.size == 3)

        assert(expression.expression == "name = ? AND age >= ? AND army IS NOT NULL AND elite >= ?")
    }
}