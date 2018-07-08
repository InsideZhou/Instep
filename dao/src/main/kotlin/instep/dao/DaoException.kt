package instep.dao

open class DaoException(override val message: String, override val cause: Exception? = null) : Exception(message, cause)