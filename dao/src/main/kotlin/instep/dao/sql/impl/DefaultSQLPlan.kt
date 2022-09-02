package instep.dao.sql.impl

import instep.InstepLogger
import instep.dao.AbstractExpression
import instep.dao.Expression
import instep.dao.sql.SQLPlan


abstract class AbstractSQLPlan<T : SQLPlan<T>>(txt: String) : AbstractExpression<T>(txt), SQLPlan<T> {
    override val logger: InstepLogger = InstepLogger.getLogger(SQLPlan::class.java)

    override val subPlans: MutableList<SQLPlan<*>> = mutableListOf()

    override fun addSubPlan(plan: SQLPlan<*>): SQLPlan<*> {
        subPlans.add(plan)

        return this
    }

    override fun toString(): String {
        return """${statement}\n${parameterToLogFormat()}"""
    }
}

abstract class AbstractTablePlan<T : SQLPlan<T>>() : AbstractSQLPlan<T>("") {
    override val text: String
        get() {
            return statement
        }
}

open class DefaultSQLPlan(txt: String) : AbstractSQLPlan<DefaultSQLPlan>(txt), Expression<DefaultSQLPlan> {
    override val statement: String
        get() {
            return text
        }
}