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
        expression.addParameters(18)

        val condition = Condition("army IS NOT NULL").and(Condition("elite >= ?", 100))
        expression.placeholderToExpression("condition", condition)
        Assert.assertEquals(expression.text, "name = ? AND age >= ? AND army IS NOT NULL AND elite >= ?")

        Assert.assertEquals(expression.parameters.size, 2)
        Assert.assertEquals(expression.parameters[0], "ZhangFei")
        Assert.assertEquals(expression.parameters[1] as Int, 18)

        expression.addParameters(*condition.parameters.toTypedArray())
        Assert.assertEquals(expression.parameters.size, 3)
        Assert.assertEquals(expression.parameters[2] as Int, 100)
    }

    @Test
    fun conditionGrouping() {
        val condition = Condition("army IS NOT NULL").and(Condition("elite >= ?", 100).or(Condition("elite < ?", 0)).grouped()).grouped()
        Assert.assertEquals(condition.text, "(army IS NOT NULL AND (elite >= ? OR elite < ?))")
    }
}