package instep.orm.sql

import instep.orm.Plan
import instep.orm.PlanExecutor
import java.sql.Connection
import java.sql.ResultSet

interface SQLPlanExecutor : PlanExecutor {
    fun executeUpdate(plan: Plan): Long;
    fun executeResultSet(conn: Connection, plan: Plan): ResultSet;
}