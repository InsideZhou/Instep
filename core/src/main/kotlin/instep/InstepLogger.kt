package instep

import instep.servicecontainer.ServiceNotFoundException

/**
 * Instep log everything through this logger, if there is a corresponding service bound in [Instep.ServiceContainer], none by default.
 */
interface InstepLogger {
    fun debug(log: String)
    fun info(log: String)
    fun warn(log: String)

    companion object {
        var logger = try {
            Instep.make(InstepLogger::class.java)
        }
        catch (e: ServiceNotFoundException) {
            null
        }

        fun debug(log: String) {
            logger?.debug(log)
        }

        fun info(log: String) {
            logger?.info(log)
        }

        fun warning(log: String) {
            logger?.warn(log)
        }
    }
}