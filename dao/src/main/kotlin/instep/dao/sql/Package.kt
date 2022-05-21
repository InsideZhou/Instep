package instep.dao.sql

import instep.Instep

internal object Package {
    val dialect = Instep.make(ConnectionProvider::class.java).dialect
}
