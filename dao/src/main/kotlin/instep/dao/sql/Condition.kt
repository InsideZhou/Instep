package instep.dao.sql

import instep.dao.impl.AbstractExpression

open class Condition constructor(txt: String, vararg parameters: Any?) : AbstractExpression<Condition>(txt) {
    init {
        super.addParameters(*parameters)
    }

    open fun and(condition: Condition): Condition {
        return joinCondition(condition, AND)
    }

    open fun or(condition: Condition): Condition {
        return joinCondition(condition, OR)
    }

    open fun andGroup(condition: Condition): Condition {
        return joinCondition(condition, AND, true)
    }

    open fun orGroup(condition: Condition): Condition {
        return joinCondition(condition, OR, true)
    }

    private fun joinCondition(condition: Condition, word: String, grouping: Boolean = false): Condition {
        if (condition.expression.isBlank()) return this

        val newCondition = if (grouping) {
            Condition("${this.expression} $word (${condition.expression})")
        }
        else {
            Condition("${this.expression} $word ${condition.expression}")
        }

        newCondition.addParameters(*this.parameters.toTypedArray())
        newCondition.addParameters(*condition.parameters.toTypedArray())

        return newCondition
    }

    companion object {
        const val AND = "AND"
        const val OR = "OR"
    }
}
