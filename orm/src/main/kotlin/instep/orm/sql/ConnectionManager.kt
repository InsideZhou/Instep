package instep.orm.sql

import java.sql.Connection

interface ConnectionManager {
    fun getConnection(): Connection
}