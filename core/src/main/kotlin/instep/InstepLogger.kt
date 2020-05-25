package instep

import instep.servicecontainer.ServiceNotFoundException

/**
 * Instep log everything through this logger, if there is a corresponding service bound in [Instep.serviceContainer], none by default.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
interface InstepLogger {
    fun message(message: String): InstepLogger
    fun exception(e: Throwable): InstepLogger
    fun context(key: String, value: Any): InstepLogger
    fun context(key: String, lazy: () -> String): InstepLogger

    fun debug()
    fun info()
    fun warn()

    companion object {
        var factory = try {
            Instep.make(InstepLoggerFactory::class.java)
        }
        catch (e: ServiceNotFoundException) {
            null
        }

        var nullLogger = object : InstepLogger {
            override fun message(message: String): InstepLogger = this

            override fun exception(e: Throwable): InstepLogger = this

            override fun context(key: String, value: Any): InstepLogger = this

            override fun context(key: String, lazy: () -> String): InstepLogger = this

            override fun debug() {}

            override fun info() {}

            override fun warn() {}
        }

        fun getLogger(cls: Class<*>): InstepLogger {
            return this.factory?.let { getLogger(cls) } ?: nullLogger
        }
    }
}

interface InstepLoggerFactory {
    fun getLogger(cls: Class<*>): InstepLogger
}
