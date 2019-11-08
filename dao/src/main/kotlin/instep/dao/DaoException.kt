package instep.dao

open class DaoException(override val message: String, override val cause: Exception? = null) : RuntimeException(message, cause)