package instep

import instep.servicecontainer.ServiceNotFoundException

/**
 * Instep log everything through this logger, if there is a corresponding service bound in [Instep.ServiceContainer], none by default.
 */
interface InstepLogger {
    val defaultLogger: String

    fun debug(log: String, logger: String = "")
    fun info(log: String, logger: String = "")
    fun warn(log: String, logger: String = "")

    companion object {
        var root = try {
            Instep.make(InstepLogger::class.java)
        }
        catch (e: ServiceNotFoundException) {
            null
        }

        fun debug(log: String, logger: String = "") {
            root?.debug(log, logger)
        }

        fun info(log: String, logger: String = "") {
            root?.info(log, logger)
        }

        fun warning(log: String, logger: String = "") {
            root?.warn(log, logger)
        }
    }
}