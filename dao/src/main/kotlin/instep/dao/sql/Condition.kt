package instep.dao.sql

import instep.dao.PlaceHolder
import instep.dao.impl.AbstractExpression

open class Condition constructor(txt: String, vararg parameters: Any?) : AbstractExpression<Condition>(txt) {
    init {
        super.addParameters(parameters)
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
//
//        fun plain(txt: String = ""): Condition {
//            return Condition(txt)
//        }
//
//        fun eq(left: String, right: Any): Condition {
//            val condition = Condition("$left = ${PlaceHolder.parameter}")
//            condition.addParameters(right)
//            return condition
//        }
//
//        fun notEQ(left: String, right: Any, valueFormat: String = VALUE_FORMAT): Condition {
//            val condition = Condition("$left <> $valueFormat")
//            condition.addParameters(right)
//            return condition
//        }
//
//        fun gt(left: String, right: Any, valueFormat: String = VALUE_FORMAT): Condition {
//            val condition = Condition("$left > $valueFormat")
//            condition.addParameters(right)
//            return condition
//        }
//
//        fun gte(left: String, right: Any, valueFormat: String = VALUE_FORMAT): Condition {
//            val condition = Condition("$left >= $valueFormat")
//            condition.addParameters(right)
//            return condition
//        }
//
//        fun lt(left: String, right: Any, valueFormat: String = VALUE_FORMAT): Condition {
//            val condition = Condition("$left < $valueFormat")
//            condition.addParameters(right)
//            return condition
//        }
//
//        fun lte(left: String, right: Any, valueFormat: String = VALUE_FORMAT): Condition {
//            val condition = Condition("$left <= $valueFormat")
//            condition.addParameters(right)
//            return condition
//        }
//
//        fun isNull(left: String): Condition {
//            return Condition("$left IS NULL")
//        }
//
//        fun isNotNull(left: String): Condition {
//            return Condition("$left IS NOT NULL")
//        }
//
//        fun contains(left: String, right: Any, valueFormat: String = VALUE_FORMAT): Condition {
//            val condition = Condition("$left LIKE $valueFormat")
//            condition.addParameters("%$right%")
//            return condition
//        }
//
//        fun startsWith(left: String, right: Any, valueFormat: String = VALUE_FORMAT): Condition {
//            val condition = Condition("$left LIKE $valueFormat")
//            condition.addParameters("$right%")
//            return condition
//        }
//
//        fun endsWith(left: String, right: Any, valueFormat: String = VALUE_FORMAT): Condition {
//            val condition = Condition("$left LIKE $valueFormat")
//            condition.addParameters("%$right")
//            return condition
//        }
//
//        fun inArray(left: String, right: Array<*>, valueFormat: String = VALUE_FORMAT): Condition {
//            val builder = StringBuilder("$left IN (")
//
//            right.forEach {
//                builder.append("$valueFormat,")
//            }
//
//            builder.deleteCharAt(builder.length - 1)
//            builder.append(")")
//
//            val condition = Condition(builder.toString())
//            condition.addParameters(*right)
//            return condition
//        }
    }
}
