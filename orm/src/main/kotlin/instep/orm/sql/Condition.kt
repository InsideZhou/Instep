package instep.orm.sql

import instep.orm.Expression
import instep.orm.planbuild.DefaultExpression

class Condition protected constructor(txt: String) : DefaultExpression(txt) {
    var conjunction: Conjunction? = null
        private set

    override val expression: String
        get() {
            val conj = conjunction
            if (null == conj) return super.expression

            return "${super.expression} ${conj.conjunction} ${conj.condition.expression}"
        }

    override val parameters: List<Any?>
        get() {
            val conj = conjunction
            if (null == conj) return super.parameters

            return super.parameters + conj.condition.parameters
        }

    fun andEQ(left: String, right: Any): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = eq(left, right)
            condition.addParameters(right)
            conjunction = Conjunction(AND, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $AND $left = ?")
            condition.addParameters(conj.condition.parameters)
            condition.addParameters(right)

            conj.condition = condition
        }

        return this
    }

    fun andNotEQ(left: String, right: Any): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = notEQ(left, right)
            condition.addParameters(right)
            conjunction = Conjunction(AND, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $AND $left <> ?")
            condition.addParameters(conj.condition.parameters)
            condition.addParameters(right)

            conj.condition = condition
        }

        return this
    }

    fun andGT(left: String, right: Any): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = gt(left, right)
            condition.addParameters(right)
            conjunction = Conjunction(AND, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $AND $left > ?")
            condition.addParameters(conj.condition.parameters)
            condition.addParameters(right)

            conj.condition = condition
        }

        return this
    }

    fun andGTE(left: String, right: Any): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = gte(left, right)
            condition.addParameters(right)
            conjunction = Conjunction(AND, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $AND $left >= ?")
            condition.addParameters(conj.condition.parameters)
            condition.addParameters(right)

            conj.condition = condition
        }

        return this
    }

    fun andLT(left: String, right: Any): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = lt(left, right)
            condition.addParameters(right)
            conjunction = Conjunction(AND, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $AND $left < ?")
            condition.addParameters(conj.condition.parameters)
            condition.addParameters(right)

            conj.condition = condition
        }

        return this
    }

    fun andLTE(left: String, right: Any): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = lte(left, right)
            condition.addParameters(right)
            conjunction = Conjunction(AND, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $AND $left <= ?")
            condition.addParameters(conj.condition.parameters)
            condition.addParameters(right)

            conj.condition = condition
        }

        return this
    }

    fun andIsNull(left: String): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = isNull(left)
            conjunction = Conjunction(AND, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $AND $left IS NULL")
            condition.addParameters(conj.condition.parameters)
            conj.condition = condition
        }

        return this
    }

    fun andIsNotNull(left: String): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = isNotNull(left)
            conjunction = Conjunction(AND, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $AND $left IS NOT NULL")
            condition.addParameters(conj.condition.parameters)
            conj.condition = condition
        }

        return this
    }

    fun andContains(left: String, right: Any): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = contains(left, right)
            condition.addParameters(right)
            conjunction = Conjunction(AND, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $AND $left LIKE '%' || ? || '%'")
            condition.addParameters(conj.condition.parameters)
            condition.addParameters(right)

            conj.condition = condition
        }

        return this
    }

    fun andStartsWith(left: String, right: Any): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = startsWith(left, right)
            condition.addParameters(right)
            conjunction = Conjunction(AND, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $AND $left LIKE ? || '%'")
            condition.addParameters(conj.condition.parameters)
            condition.addParameters(right)

            conj.condition = condition
        }

        return this
    }

    fun andEndsWith(left: String, right: Any): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = endsWith(left, right)
            condition.addParameters(right)
            conjunction = Conjunction(AND, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $AND $left LIKE '%' || ?")
            condition.addParameters(conj.condition.parameters)
            condition.addParameters(right)

            conj.condition = condition
        }

        return this
    }

    fun orEQ(left: String, right: Any): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = eq(left, right)
            condition.addParameters(right)
            conjunction = Conjunction(OR, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $OR $left = ?")
            condition.addParameters(conj.condition.parameters)
            condition.addParameters(right)

            conj.condition = condition
        }

        return this
    }

    fun orNotEQ(left: String, right: Any): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = notEQ(left, right)
            condition.addParameters(right)
            conjunction = Conjunction(OR, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $OR $left <> ?")
            condition.addParameters(conj.condition.parameters)
            condition.addParameters(right)

            conj.condition = condition
        }

        return this
    }

    fun orGT(left: String, right: Any): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = gt(left, right)
            condition.addParameters(right)
            conjunction = Conjunction(OR, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $OR $left > ?")
            condition.addParameters(conj.condition.parameters)
            condition.addParameters(right)

            conj.condition = condition
        }

        return this
    }

    fun orGTE(left: String, right: Any): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = gte(left, right)
            condition.addParameters(right)
            conjunction = Conjunction(OR, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $OR $left >= ?")
            condition.addParameters(conj.condition.parameters)
            condition.addParameters(right)

            conj.condition = condition
        }

        return this
    }

    fun orLT(left: String, right: Any): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = lt(left, right)
            condition.addParameters(right)
            conjunction = Conjunction(OR, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $OR $left < ?")
            condition.addParameters(conj.condition.parameters)
            condition.addParameters(right)

            conj.condition = condition
        }

        return this
    }

    fun orLTE(left: String, right: Any): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = lte(left, right)
            condition.addParameters(right)
            conjunction = Conjunction(OR, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $OR $left <= ?")
            condition.addParameters(conj.condition.parameters)
            condition.addParameters(right)

            conj.condition = condition
        }

        return this
    }

    fun orIsNull(left: String): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = isNull(left)
            conjunction = Conjunction(OR, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $OR $left IS NULL")
            condition.addParameters(conj.condition.parameters)
            conj.condition = condition
        }

        return this
    }

    fun orIsNotNull(left: String): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = isNotNull(left)
            conjunction = Conjunction(OR, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $OR $left IS NOT NULL")
            condition.addParameters(conj.condition.parameters)
            conj.condition = condition
        }

        return this
    }

    fun orContains(left: String, right: Any): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = contains(left, right)
            condition.addParameters(right)
            conjunction = Conjunction(OR, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $OR $left LIKE '%' || ? || '%'")
            condition.addParameters(conj.condition.parameters)
            condition.addParameters(right)

            conj.condition = condition
        }

        return this
    }

    fun orStartsWith(left: String, right: Any): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = startsWith(left, right)
            condition.addParameters(right)
            conjunction = Conjunction(OR, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $OR $left LIKE ? || '%'")
            condition.addParameters(conj.condition.parameters)
            condition.addParameters(right)

            conj.condition = condition
        }

        return this
    }

    fun orEndsWith(left: String, right: Any): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = endsWith(left, right)
            condition.addParameters(right)
            conjunction = Conjunction(OR, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $OR $left LIKE '%' || ?")
            condition.addParameters(conj.condition.parameters)
            condition.addParameters(right)

            conj.condition = condition
        }

        return this
    }

    fun and(expression: Expression): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = Condition(expression.expression)
            condition.addParameters(expression.parameters)
            conjunction = Conjunction(AND, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $AND ${expression.expression}")
            condition.addParameters(conj.condition.parameters)
            condition.addParameters(expression.parameters)

            conj.condition = condition
        }

        return this
    }

    fun or(expression: Expression): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = Condition(expression.expression)
            condition.addParameters(expression.parameters)
            conjunction = Conjunction(OR, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $OR ${expression.expression}")
            condition.addParameters(conj.condition.parameters)
            condition.addParameters(expression.parameters)

            conj.condition = condition
        }

        return this
    }

    fun andGroup(expression: Expression): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = Condition("(${expression.expression})")
            condition.addParameters(expression.parameters)
            conjunction = Conjunction(AND, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $AND (${expression.expression})")
            condition.addParameters(conj.condition.parameters)
            condition.addParameters(expression.parameters)

            conj.condition = condition
        }

        return this
    }

    fun orGroup(expression: Expression): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = Condition("(${expression.expression})")
            condition.addParameters(expression.parameters)
            conjunction = Conjunction(OR, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $OR (${expression.expression})")
            condition.addParameters(conj.condition.parameters)
            condition.addParameters(expression.parameters)

            conj.condition = condition
        }

        return this
    }

    companion object {
        private const val serialVersionUID = -1016375911659982537L

        var AND = "AND"
        var OR = "OR"

        fun eq(left: String, right: Any): Condition {
            val condition = Condition("$left = ?")
            condition.addParameters(right)
            return condition
        }

        fun notEQ(left: String, right: Any): Condition {
            val condition = Condition("$left <> ?")
            condition.addParameters(right)
            return condition
        }

        fun gt(left: String, right: Any): Condition {
            val condition = Condition("$left > ?")
            condition.addParameters(right)
            return condition
        }

        fun gte(left: String, right: Any): Condition {
            val condition = Condition("$left >= ?")
            condition.addParameters(right)
            return condition
        }

        fun lt(left: String, right: Any): Condition {
            val condition = Condition("$left < ?")
            condition.addParameters(right)
            return condition
        }

        fun lte(left: String, right: Any): Condition {
            val condition = Condition("$left <= ?")
            condition.addParameters(right)
            return condition
        }

        fun isNull(left: String): Condition {
            return Condition("$left IS NULL")
        }

        fun isNotNull(left: String): Condition {
            return Condition("$left IS NOT NULL")
        }

        fun contains(left: String, right: Any): Condition {
            val condition = Condition("$left LIKE '%' || ? || '%'")
            condition.addParameters(right)
            return condition
        }

        fun startsWith(left: String, right: Any): Condition {
            val condition = Condition("$left LIKE ? || '%'")
            condition.addParameters(right)
            return condition
        }

        fun endsWith(left: String, right: Any): Condition {
            val condition = Condition("$left LIKE '%' || ?")
            condition.addParameters(right)
            return condition
        }
    }
}

data class Conjunction(val conjunction: String, var condition: Condition)