package instep.dao.sql.impl

import instep.dao.Plan
import instep.dao.sql.Dialect
import instep.dao.sql.PreparedStatementGenerator
import instep.dao.sql.TableInsertPlan
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement


open class DefaultPreparedStatementGenerator : PreparedStatementGenerator {
    override fun generate(conn: Connection, dialect: Dialect, plan: Plan<*>): PreparedStatement {
        val stmt = when {
            plan is TableInsertPlan || plan.statement.startsWith("insert", true) -> conn.prepareStatement(plan.statement, Statement.RETURN_GENERATED_KEYS)
            else -> conn.prepareStatement(plan.statement)
        }

        plan.parameters.forEachIndexed { i, value ->
            val paramIndex = i + 1

            dialect.setParameterForPreparedStatement(stmt, paramIndex, value)
        }

        return stmt
    }
}