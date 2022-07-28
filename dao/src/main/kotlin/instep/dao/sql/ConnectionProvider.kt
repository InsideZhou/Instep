package instep.dao.sql

import java.sql.Connection

interface ConnectionProvider {
    val dialect: Dialect
    val transactionRunner: TransactionRunner

    fun getConnection(): Connection

    fun releaseConnection(conn: Connection) {
        conn.close()
    }
}