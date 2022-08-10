package instep.dao.sql

import instep.dao.AbstractExpression

open class Condition constructor(txt: String, vararg parameters: Any?) : AbstractExpression<Condition>(txt) {
    init {
        super.addParameters(*parameters)
    }

    open fun and(condition: Condition): Condition {
        return joinCondition(condition, AND, false)
    }

    open fun or(condition: Condition): Condition {
        return joinCondition(condition, OR, false)
    }

    open fun andGroup(condition: Condition): Condition {
        return joinCondition(condition, AND, true)
    }

    open fun orGroup(condition: Condition): Condition {
        return joinCondition(condition, OR, true)
    }

    private fun joinCondition(condition: Condition, word: String, grouping: Boolean): Condition {
        if (condition.text.isBlank()) return this
        if (this.text.isBlank()) return condition

        val newCondition = if (grouping) {
            Condition("${this.text} $word (${condition.text})")
        }
        else {
            Condition("${this.text} $word ${condition.text}")
        }

        newCondition.addParameters(*this.parameters.toTypedArray())
        newCondition.addParameters(*condition.parameters.toTypedArray())

        return newCondition
    }

    companion object {
        const val AND = "AND"
        const val OR = "OR"

        val empty = Condition("")
    }
}
