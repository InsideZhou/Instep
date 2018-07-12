package instep.dao.sql

import instep.ImpossibleBranch
import instep.dao.Expression
import instep.dao.impl.AbstractExpression

@Suppress("unused")
open class Condition protected constructor(txt: String) : AbstractExpression<Condition>(txt) {
    var conjunction: Conjunction? = null
        private set

    override val expression: String
        get() {
            val conj = conjunction
            val baseExpression = super.expression

            if (null == conj || conj.condition.expression.isBlank()) return baseExpression
            if (baseExpression.isBlank()) return conj.condition.expression

            return "$baseExpression ${conj.conjunction} ${conj.condition.expression}"
        }

    override val parameters: List<Any?>
        get() {
            val conj = conjunction
            if (null == conj) return super.parameters

            return super.parameters + conj.condition.parameters
        }

    open fun andEQ(left: String, right: Any, valueFormat: String = VALUE_FORMAT): Condition {
        val condition = eq(left, right, valueFormat)
        joinCondition(condition, AND)
        return this
    }

    open fun andNotEQ(left: String, right: Any, valueFormat: String = VALUE_FORMAT): Condition {
        val condition = notEQ(left, right, valueFormat)
        joinCondition(condition, AND)
        return this
    }

    open fun andGT(left: String, right: Any, valueFormat: String = VALUE_FORMAT): Condition {
        val condition = gt(left, right, valueFormat)
        joinCondition(condition, AND)
        return this
    }

    open fun andGTE(left: String, right: Any, valueFormat: String = VALUE_FORMAT): Condition {
        val condition = gte(left, right, valueFormat)
        joinCondition(condition, AND)
        return this
    }

    open fun andLT(left: String, right: Any, valueFormat: String = VALUE_FORMAT): Condition {
        val condition = lt(left, right, valueFormat)
        joinCondition(condition, AND)
        return this
    }

    open fun andLTE(left: String, right: Any, valueFormat: String = VALUE_FORMAT): Condition {
        val condition = lte(left, right, valueFormat)
        joinCondition(condition, AND)
        return this
    }

    open fun andIsNull(left: String): Condition {
        val condition = isNull(left)
        joinCondition(condition, AND)
        return this
    }

    open fun andIsNotNull(left: String): Condition {
        val condition = isNotNull(left)
        joinCondition(condition, AND)
        return this
    }

    open fun andContains(left: String, right: Any, valueFormat: String = VALUE_FORMAT): Condition {
        val condition = contains(left, right, valueFormat)
        joinCondition(condition, AND)
        return this
    }

    open fun andStartsWith(left: String, right: Any, valueFormat: String = VALUE_FORMAT): Condition {
        val condition = startsWith(left, right, valueFormat)
        joinCondition(condition, AND)
        return this
    }

    open fun andEndsWith(left: String, right: Any, valueFormat: String = VALUE_FORMAT): Condition {
        val condition = endsWith(left, right, valueFormat)
        joinCondition(condition, AND)
        return this
    }

    open fun andInArray(left: String, right: Array<*>, valueFormat: String = VALUE_FORMAT): Condition {
        val condition = inArray(left, right, valueFormat)
        joinCondition(condition, AND)
        return this
    }

    open fun orEQ(left: String, right: Any, valueFormat: String = VALUE_FORMAT): Condition {
        val condition = eq(left, right, valueFormat)
        joinCondition(condition, OR)
        return this
    }

    open fun orNotEQ(left: String, right: Any, valueFormat: String = VALUE_FORMAT): Condition {
        val condition = notEQ(left, right, valueFormat)
        joinCondition(condition, OR)
        return this
    }

    open fun orGT(left: String, right: Any, valueFormat: String = VALUE_FORMAT): Condition {
        val condition = gt(left, right, valueFormat)
        joinCondition(condition, OR)
        return this
    }

    open fun orGTE(left: String, right: Any, valueFormat: String = VALUE_FORMAT): Condition {
        val condition = gte(left, right)
        joinCondition(condition, OR)
        return this
    }

    open fun orLT(left: String, right: Any, valueFormat: String = VALUE_FORMAT): Condition {
        val condition = lt(left, right, valueFormat)
        joinCondition(condition, OR)
        return this
    }

    open fun orLTE(left: String, right: Any, valueFormat: String = VALUE_FORMAT): Condition {
        val condition = lte(left, right, valueFormat)
        joinCondition(condition, OR)
        return this
    }

    open fun orIsNull(left: String): Condition {
        val condition = isNull(left)
        joinCondition(condition, OR)
        return this
    }

    open fun orIsNotNull(left: String): Condition {
        val condition = isNotNull(left)
        joinCondition(condition, OR)
        return this
    }

    open fun orContains(left: String, right: Any, valueFormat: String = VALUE_FORMAT): Condition {
        val condition = contains(left, right, valueFormat)
        joinCondition(condition, OR)
        return this
    }

    open fun orStartsWith(left: String, right: Any, valueFormat: String = VALUE_FORMAT): Condition {
        val condition = startsWith(left, right, valueFormat)
        joinCondition(condition, OR)
        return this
    }

    open fun orEndsWith(left: String, right: Any, valueFormat: String = VALUE_FORMAT): Condition {
        val condition = endsWith(left, right, valueFormat)
        joinCondition(condition, OR)
        return this
    }

    open fun orInArray(left: String, right: Array<*>, valueFormat: String = VALUE_FORMAT): Condition {
        val condition = inArray(left, right, valueFormat)
        joinCondition(condition, OR)
        return this
    }

    private fun joinCondition(condition: Condition, word: String) {
        if (condition.expression.isBlank()) return

        val conj = conjunction

        if (null == conj) {
            conjunction = Conjunction(word, condition)
        }
        else {
            when (word) {
                AND -> conj.condition = conj.condition.and(condition)
                OR -> conj.condition = conj.condition.or(condition)
                else -> throw ImpossibleBranch()
            }
        }
    }

    private fun joinExpression(expression: Expression<*>, word: String): Condition {
        if (expression.expression.isBlank()) return this

        val conj = conjunction

        when {
            null == conj -> {
                val condition = Condition(expression.expression)
                condition.addParameters(*expression.parameters.toTypedArray())
                conjunction = Conjunction(word, condition)
            }
            conj.condition.expression.isBlank() -> {
                val condition = Condition(expression.expression)
                condition.addParameters(*expression.parameters.toTypedArray())

                conj.condition = condition
            }
            else -> {
                val condition = Condition("${conj.condition.expression} $word ${expression.expression}")
                condition.addParameters(*conj.condition.parameters.toTypedArray())
                condition.addParameters(*expression.parameters.toTypedArray())

                conj.condition = condition
            }
        }

        return this
    }

    private fun joinAndGroupExpression(expression: Expression<*>, word: String): Condition {
        if (expression.expression.isBlank()) return this

        val conj = conjunction

        when {
            null == conj -> {
                val condition = Condition("(${expression.expression})")
                condition.addParameters(*expression.parameters.toTypedArray())
                conjunction = Conjunction(word, condition)
            }
            conj.condition.expression.isBlank() -> {
                val condition = Condition("(${expression.expression})")
                condition.addParameters(*expression.parameters.toTypedArray())

                conj.condition = condition
            }
            else -> {
                val condition = Condition("${conj.condition.expression} $word (${expression.expression})")
                condition.addParameters(*conj.condition.parameters.toTypedArray())
                condition.addParameters(*expression.parameters.toTypedArray())

                conj.condition = condition
            }
        }

        return this
    }

    open fun and(expression: Expression<*>): Condition {
        return joinExpression(expression, AND)
    }

    open fun or(expression: Expression<*>): Condition {
        return joinExpression(expression, OR)
    }

    open fun andGroup(expression: Expression<*>): Condition {
        return joinAndGroupExpression(expression, AND)
    }

    open fun orGroup(expression: Expression<*>): Condition {
        return joinAndGroupExpression(expression, OR)
    }

    companion object {
        private const val serialVersionUID = -1016375911659982537L

        const val AND = "AND"
        const val OR = "OR"
        const val VALUE_FORMAT = "?"

        fun empty(): Condition {
            return Condition("")
        }

        fun eq(left: String, right: Any, valueFormat: String = VALUE_FORMAT): Condition {
            val condition = Condition("$left = $valueFormat")
            condition.addParameters(right)
            return condition
        }

        fun notEQ(left: String, right: Any, valueFormat: String = VALUE_FORMAT): Condition {
            val condition = Condition("$left <> $valueFormat")
            condition.addParameters(right)
            return condition
        }

        fun gt(left: String, right: Any, valueFormat: String = VALUE_FORMAT): Condition {
            val condition = Condition("$left > $valueFormat")
            condition.addParameters(right)
            return condition
        }

        fun gte(left: String, right: Any, valueFormat: String = VALUE_FORMAT): Condition {
            val condition = Condition("$left >= $valueFormat")
            condition.addParameters(right)
            return condition
        }

        fun lt(left: String, right: Any, valueFormat: String = VALUE_FORMAT): Condition {
            val condition = Condition("$left < $valueFormat")
            condition.addParameters(right)
            return condition
        }

        fun lte(left: String, right: Any, valueFormat: String = VALUE_FORMAT): Condition {
            val condition = Condition("$left <= $valueFormat")
            condition.addParameters(right)
            return condition
        }

        fun isNull(left: String): Condition {
            return Condition("$left IS NULL")
        }

        fun isNotNull(left: String): Condition {
            return Condition("$left IS NOT NULL")
        }

        fun contains(left: String, right: Any, valueFormat: String = VALUE_FORMAT): Condition {
            val condition = Condition("$left LIKE $valueFormat")
            condition.addParameters("%$right%")
            return condition
        }

        fun startsWith(left: String, right: Any, valueFormat: String = VALUE_FORMAT): Condition {
            val condition = Condition("$left LIKE $valueFormat")
            condition.addParameters("$right%")
            return condition
        }

        fun endsWith(left: String, right: Any, valueFormat: String = VALUE_FORMAT): Condition {
            val condition = Condition("$left LIKE $valueFormat")
            condition.addParameters("%$right")
            return condition
        }

        fun inArray(left: String, right: Array<*>, valueFormat: String = VALUE_FORMAT): Condition {
            val builder = StringBuilder("$left IN (")

            right.forEach {
                builder.append("$valueFormat,")
            }

            builder.deleteCharAt(builder.length - 1)
            builder.append(")")

            val condition = Condition(builder.toString())
            condition.addParameters(*right)
            return condition
        }
    }
}

data class Conjunction(val conjunction: String, var condition: Condition)