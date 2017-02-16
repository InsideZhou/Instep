package instep.dao.sql

import java.sql.Connection

interface ConnectionProvider {
    fun getConnection(): Connection
}