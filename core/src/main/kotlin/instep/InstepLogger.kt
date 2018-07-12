package instep

import instep.servicecontainer.ServiceNotFoundException

/**
 * Instep log everything through this logger, if there is a corresponding service bound in [Instep.ServiceContainer], none by default.
 */
@Suppress("unused")
interface InstepLogger {
    val enableDebug: Boolean
    val enableInfo: Boolean
    val enableWarning: Boolean

    fun debug(log: String, logger: String = "")
    fun info(log: String, logger: String = "")
    fun warning(log: String, logger: String = "")

    fun debug(log: String, cls: Class<*>) = debug(log, cls.name)
    fun info(log: String, cls: Class<*>) = info(log, cls.name)
    fun warning(log: String, cls: Class<*>) = warning(log, cls.name)

    companion object {
        var logger = try {
            Instep.make(InstepLogger::class.java)
        }
        catch (e: ServiceNotFoundException) {
            null
        }

        fun debug(lazy: () -> String, logger: String = "") {
            this.logger?.apply {
                if (enableDebug) {
                    debug(lazy(), logger)
                }
            }
        }

        fun info(lazy: () -> String, logger: String = "") {
            this.logger?.apply {
                if (enableInfo) {
                    info(lazy(), logger)
                }
            }
        }

        fun warning(lazy: () -> String, logger: String = "") {
            this.logger?.apply {
                if (enableWarning) {
                    warning(lazy(), logger)
                }
            }
        }

        fun debug(lazy: () -> String, cls: Class<*>) = debug(lazy, cls.name)
        fun info(lazy: () -> String, cls: Class<*>) = info(lazy, cls.name)
        fun warning(lazy: () -> String, cls: Class<*>) = warning(lazy, cls.name)
    }
}
