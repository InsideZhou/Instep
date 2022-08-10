package instep.dao

import instep.dao.sql.Condition
import instep.dao.sql.InstepSQL
import org.testng.Assert
import org.testng.annotations.Test

object ExpressionTest {
    init {
        InstepSQL
    }

    @Test
    fun placeholder() {
        val expression = DefaultExpression("name = \${name} AND age >= \${age} AND \${condition}")
        assert(expression.parameters.isNotEmpty())
        assert(expression.parameters.all { it is PlaceHolder })

        expression.placeholderToParameter("name", "ZhangFei")
        Assert.assertThrows(PlaceHolderRemainingException::class.java) { expression.addParameters(18) }

        val condition = Condition("army IS NOT NULL").and(Condition("elite >= ?", 100))
        expression.placeholderToExpression("condition", condition)
        assert(expression.parameters.size == 3)
        assert(expression.text == "name = ? AND age >= ? AND army IS NOT NULL AND elite >= ?")
    }
}