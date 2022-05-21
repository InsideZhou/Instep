package instep.dao.sql.impl

import instep.Instep
import instep.dao.sql.ConnectionProvider

internal object Package {
    val threadLocalTransactionContext = object : ThreadLocal<DefaultTransactionContext>() {}
    val dialect = Instep.make(ConnectionProvider::class.java).dialect
}