package instep.dao.sql

import instep.dao.DaoException
import java.sql.SQLException


class SQLPlanExecutionException(e: SQLException?, override val message: String = "") : DaoException(message, e) {
    constructor(e: SQLException) : this(e, e.message ?: "")
    constructor(message: String) : this(null, message)
}