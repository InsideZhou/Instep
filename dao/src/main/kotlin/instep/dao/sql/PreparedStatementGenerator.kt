package instep.dao.sql

import java.sql.Connection
import java.sql.PreparedStatement


interface PreparedStatementGenerator {
    fun generate(conn: Connection, dialect: Dialect, plan: instep.dao.Plan<*>): PreparedStatement
}