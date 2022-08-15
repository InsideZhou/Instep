package instep.util

import org.testng.Assert
import org.testng.annotations.Test


class StringExtensionsTest {
    @Test
    fun camelCaseToSnake() {
        Assert.assertEquals("SnakeCase".camelCaseToSnake(), "snake_case")
        Assert.assertEquals("SNAKECase".camelCaseToSnake(), "snake_case")
        Assert.assertEquals("SnakeCaseSNAKECase".camelCaseToSnake(), "snake_case_snake_case")
        Assert.assertEquals("_SnakeCase".camelCaseToSnake(), "_snake_case")
        Assert.assertEquals("SnakeCase_".camelCaseToSnake(), "snake_case_")
        Assert.assertEquals("_SNAKECase".camelCaseToSnake(), "_snake_case")
        Assert.assertEquals("snakeCASE".camelCaseToSnake(), "snake_case")
        Assert.assertEquals("_snakeCASE".camelCaseToSnake(), "_snake_case")
        Assert.assertEquals("snakeCASE_".camelCaseToSnake(), "snake_case_")

        Assert.assertEquals("Snake1Case".camelCaseToSnake(), "snake1_case")
        Assert.assertEquals("Sna1keCase".camelCaseToSnake(), "sna1ke_case")
        Assert.assertEquals("SNAKE1Case".camelCaseToSnake(), "snake1_case")
        Assert.assertEquals("SNA1KECase".camelCaseToSnake(), "sna1_ke_case")
        Assert.assertEquals("_snake1CASE".camelCaseToSnake(), "_snake1_case")
    }
}