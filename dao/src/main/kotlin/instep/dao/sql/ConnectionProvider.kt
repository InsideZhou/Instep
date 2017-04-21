package instep.dao.sql

import java.sql.Connection

interface ConnectionProvider {
    val dialect: Dialect

    fun getConnection(): Connection
}