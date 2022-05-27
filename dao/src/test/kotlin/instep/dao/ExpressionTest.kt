package instep.dao

import instep.Instep
import instep.dao.impl.AbstractExpression
import instep.dao.sql.ConnectionProvider
import instep.dao.sql.InstepSQL
import org.testng.Assert
import org.testng.annotations.Test

object ExpressionTest {
    init {
        InstepSQL
    }

    @Test
    fun placeholder() {
        val dialect = Instep.make(ConnectionProvider::class.java).dialect
        val factory = Instep.make(ExpressionFactory::class.java)
        val expression = factory.createInstance("name = \${name} AND age >= \${age} AND \${condition}")
        assert(expression.parameters.isNotEmpty())
        assert(expression.parameters.all { it is PlaceHolder })

        expression.addParameter("name", "ZhangFei")

        if (expression is AbstractExpression) {
            Assert.assertThrows(PlaceHolderRemainingException::class.java) { expression.addParameters(18) }
        }

        val condition = dialect.isNotNull("army").and(dialect.gte("elite", 100))
        expression.addExpression("condition", condition)
        assert(expression.parameters.size == 3)

        assert(expression.expression == "name = ? AND age >= ? AND army IS NOT NULL AND elite >= ?")
    }
}