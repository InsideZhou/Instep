package instep.dao.sql

import instep.dao.DaoException
import java.sql.SQLException


class SQLPlanExecutionException(e: SQLException) : DaoException(e.message ?: "", e)