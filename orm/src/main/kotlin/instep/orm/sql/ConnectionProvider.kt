package instep.orm.sql

import java.sql.Connection

interface ConnectionProvider {
    fun getConnection(): Connection
}