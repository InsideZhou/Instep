package instep.dao.sql

import instep.dao.Plan
import instep.dao.PlanExecutor
import java.sql.Connection
import java.sql.ResultSet

interface SQLPlanExecutor : PlanExecutor {
    fun executeUpdate(plan: Plan<*>): Long
    fun executeResultSet(conn: Connection, plan: Plan<*>): ResultSet
}