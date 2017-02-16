package instep.dao.sql

import instep.dao.Expression
import instep.dao.impl.DefaultExpression

open class Condition protected constructor(txt: String) : DefaultExpression(txt) {
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

    open fun andEQ(left: String, right: Any): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = eq(left, right)
            condition.addParameters(right)
            conjunction = Conjunction(AND, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $AND $left = ?")
            condition.addParameters(*conj.condition.parameters.toTypedArray())
            condition.addParameters(right)

            conj.condition = condition
        }

        return this
    }

    open fun andNotEQ(left: String, right: Any): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = notEQ(left, right)
            condition.addParameters(right)
            conjunction = Conjunction(AND, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $AND $left <> ?")
            condition.addParameters(*conj.condition.parameters.toTypedArray())
            condition.addParameters(right)

            conj.condition = condition
        }

        return this
    }

    open fun andGT(left: String, right: Any): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = gt(left, right)
            condition.addParameters(right)
            conjunction = Conjunction(AND, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $AND $left > ?")
            condition.addParameters(*conj.condition.parameters.toTypedArray())
            condition.addParameters(right)

            conj.condition = condition
        }

        return this
    }

    open fun andGTE(left: String, right: Any): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = gte(left, right)
            condition.addParameters(right)
            conjunction = Conjunction(AND, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $AND $left >= ?")
            condition.addParameters(*conj.condition.parameters.toTypedArray())
            condition.addParameters(right)

            conj.condition = condition
        }

        return this
    }

    open fun andLT(left: String, right: Any): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = lt(left, right)
            condition.addParameters(right)
            conjunction = Conjunction(AND, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $AND $left < ?")
            condition.addParameters(*conj.condition.parameters.toTypedArray())
            condition.addParameters(right)

            conj.condition = condition
        }

        return this
    }

    open fun andLTE(left: String, right: Any): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = lte(left, right)
            condition.addParameters(right)
            conjunction = Conjunction(AND, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $AND $left <= ?")
            condition.addParameters(*conj.condition.parameters.toTypedArray())
            condition.addParameters(right)

            conj.condition = condition
        }

        return this
    }

    open fun andIsNull(left: String): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = isNull(left)
            conjunction = Conjunction(AND, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $AND $left IS NULL")
            condition.addParameters(*conj.condition.parameters.toTypedArray())
            conj.condition = condition
        }

        return this
    }

    open fun andIsNotNull(left: String): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = isNotNull(left)
            conjunction = Conjunction(AND, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $AND $left IS NOT NULL")
            condition.addParameters(*conj.condition.parameters.toTypedArray())
            conj.condition = condition
        }

        return this
    }

    open fun andContains(left: String, right: Any): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = contains(left, right)
            condition.addParameters(right)
            conjunction = Conjunction(AND, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $AND $left LIKE '%' || ? || '%'")
            condition.addParameters(*conj.condition.parameters.toTypedArray())
            condition.addParameters(right)

            conj.condition = condition
        }

        return this
    }

    open fun andStartsWith(left: String, right: Any): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = startsWith(left, right)
            condition.addParameters(right)
            conjunction = Conjunction(AND, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $AND $left LIKE ? || '%'")
            condition.addParameters(*conj.condition.parameters.toTypedArray())
            condition.addParameters(right)

            conj.condition = condition
        }

        return this
    }

    open fun andEndsWith(left: String, right: Any): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = endsWith(left, right)
            condition.addParameters(right)
            conjunction = Conjunction(AND, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $AND $left LIKE '%' || ?")
            condition.addParameters(*conj.condition.parameters.toTypedArray())
            condition.addParameters(right)

            conj.condition = condition
        }

        return this
    }

    open fun orEQ(left: String, right: Any): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = eq(left, right)
            condition.addParameters(right)
            conjunction = Conjunction(OR, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $OR $left = ?")
            condition.addParameters(*conj.condition.parameters.toTypedArray())
            condition.addParameters(right)

            conj.condition = condition
        }

        return this
    }

    open fun orNotEQ(left: String, right: Any): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = notEQ(left, right)
            condition.addParameters(right)
            conjunction = Conjunction(OR, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $OR $left <> ?")
            condition.addParameters(*conj.condition.parameters.toTypedArray())
            condition.addParameters(right)

            conj.condition = condition
        }

        return this
    }

    open fun orGT(left: String, right: Any): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = gt(left, right)
            condition.addParameters(right)
            conjunction = Conjunction(OR, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $OR $left > ?")
            condition.addParameters(*conj.condition.parameters.toTypedArray())
            condition.addParameters(right)

            conj.condition = condition
        }

        return this
    }

    open fun orGTE(left: String, right: Any): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = gte(left, right)
            condition.addParameters(right)
            conjunction = Conjunction(OR, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $OR $left >= ?")
            condition.addParameters(*conj.condition.parameters.toTypedArray())
            condition.addParameters(right)

            conj.condition = condition
        }

        return this
    }

    open fun orLT(left: String, right: Any): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = lt(left, right)
            condition.addParameters(right)
            conjunction = Conjunction(OR, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $OR $left < ?")
            condition.addParameters(*conj.condition.parameters.toTypedArray())
            condition.addParameters(right)

            conj.condition = condition
        }

        return this
    }

    open fun orLTE(left: String, right: Any): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = lte(left, right)
            condition.addParameters(right)
            conjunction = Conjunction(OR, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $OR $left <= ?")
            condition.addParameters(*conj.condition.parameters.toTypedArray())
            condition.addParameters(right)

            conj.condition = condition
        }

        return this
    }

    open fun orIsNull(left: String): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = isNull(left)
            conjunction = Conjunction(OR, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $OR $left IS NULL")
            condition.addParameters(*conj.condition.parameters.toTypedArray())
            conj.condition = condition
        }

        return this
    }

    open fun orIsNotNull(left: String): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = isNotNull(left)
            conjunction = Conjunction(OR, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $OR $left IS NOT NULL")
            condition.addParameters(*conj.condition.parameters.toTypedArray())
            conj.condition = condition
        }

        return this
    }

    open fun orContains(left: String, right: Any): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = contains(left, right)
            condition.addParameters(right)
            conjunction = Conjunction(OR, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $OR $left LIKE '%' || ? || '%'")
            condition.addParameters(*conj.condition.parameters.toTypedArray())
            condition.addParameters(right)

            conj.condition = condition
        }

        return this
    }

    open fun orStartsWith(left: String, right: Any): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = startsWith(left, right)
            condition.addParameters(right)
            conjunction = Conjunction(OR, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $OR $left LIKE ? || '%'")
            condition.addParameters(*conj.condition.parameters.toTypedArray())
            condition.addParameters(right)

            conj.condition = condition
        }

        return this
    }

    open fun orEndsWith(left: String, right: Any): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = endsWith(left, right)
            condition.addParameters(right)
            conjunction = Conjunction(OR, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $OR $left LIKE '%' || ?")
            condition.addParameters(*conj.condition.parameters.toTypedArray())
            condition.addParameters(right)

            conj.condition = condition
        }

        return this
    }

    open fun and(expression: Expression): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = Condition(expression.expression)
            condition.addParameters(*expression.parameters.toTypedArray())
            conjunction = Conjunction(AND, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $AND ${expression.expression}")
            condition.addParameters(*conj.condition.parameters.toTypedArray())
            condition.addParameters(*expression.parameters.toTypedArray())

            conj.condition = condition
        }

        return this
    }

    open fun or(expression: Expression): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = Condition(expression.expression)
            condition.addParameters(*expression.parameters.toTypedArray())
            conjunction = Conjunction(OR, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $OR ${expression.expression}")
            condition.addParameters(*conj.condition.parameters.toTypedArray())
            condition.addParameters(*expression.parameters.toTypedArray())

            conj.condition = condition
        }

        return this
    }

    open fun andGroup(expression: Expression): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = Condition("(${expression.expression})")
            condition.addParameters(*expression.parameters.toTypedArray())
            conjunction = Conjunction(AND, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $AND (${expression.expression})")
            condition.addParameters(*conj.condition.parameters.toTypedArray())
            condition.addParameters(*expression.parameters.toTypedArray())

            conj.condition = condition
        }

        return this
    }

    open fun orGroup(expression: Expression): Condition {
        val conj = conjunction

        if (null == conj) {
            val condition = Condition("(${expression.expression})")
            condition.addParameters(*expression.parameters.toTypedArray())
            conjunction = Conjunction(OR, condition)
        }
        else {
            val condition = Condition("${conj.condition.expression} $OR (${expression.expression})")
            condition.addParameters(*conj.condition.parameters.toTypedArray())
            condition.addParameters(*expression.parameters.toTypedArray())

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