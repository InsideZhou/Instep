package instep.dao.sql

import instep.dao.AbstractExpression

open class Condition constructor(txt: String, vararg parameters: Any?) : AbstractExpression<Condition>(txt) {
    var grouping = false

    init {
        super.addParameters(*parameters)
    }

    open fun grouped(): Condition {
        this.grouping = true
        return this
    }

    override val text: String
        get() {
            return if (grouping) {
                return "(${super.text})"
            }
            else {
                super.text
            }
        }

    open fun and(condition: Condition): Condition {
        return joinCondition(condition, AND)
    }

    open fun or(condition: Condition): Condition {
        return joinCondition(condition, OR)
    }

    private fun joinCondition(condition: Condition, word: String): Condition {
        if (condition.text.isBlank()) return this
        if (this.text.isBlank()) return condition

        val newCondition = Condition("${this.text} $word ${condition.text}")

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
