package instep

import instep.servicecontainer.ServiceNotFoundException

/**
 * Instep log everything through this logger, if there is a corresponding service bound in [Instep.ServiceContainer], none by default.
 */
interface InstepLogger {
    val defaultLogger: String

    val enableDebug: Boolean
    val enableInfo: Boolean
    val enableWarning: Boolean

    fun debug(log: String, logger: String = "")
    fun info(log: String, logger: String = "")
    fun warning(log: String, logger: String = "")

    companion object {
        var root = try {
            Instep.make(InstepLogger::class.java)
        }
        catch (e: ServiceNotFoundException) {
            null
        }

        fun debug(lazy: () -> String, logger: String = "") {
            root?.let {
                if (it.enableDebug) {
                    it.debug(lazy(), logger)
                }
            }
        }

        fun info(lazy: () -> String, logger: String = "") {
            root?.let {
                if (it.enableInfo) {
                    it.info(lazy(), logger)
                }
            }
        }

        fun warning(lazy: () -> String, logger: String = "") {
            root?.let {
                if (it.enableWarning) {
                    it.warning(lazy(), logger)
                }
            }
        }
    }
}