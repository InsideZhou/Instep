package instep.orm

import com.alibaba.druid.pool.DruidDataSource
import instep.Instep
import instep.InstepLogger
import instep.orm.sql.ConnectionManager
import instep.orm.sql.InstepSQL
import instep.orm.sql.impl.DefaultConnectionManager
import org.testng.annotations.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object InstepSQLTest {
    val datasource = DruidDataSource()

    init {
        datasource.url = "jdbc:h2:mem:instep_orm;DB_CLOSE_DELAY=-1"
        datasource.initialSize = 1
        datasource.minIdle = 1
        datasource.maxActive = 2
        datasource.timeBetweenEvictionRunsMillis = 60000
        datasource.minEvictableIdleTimeMillis = 300000
        datasource.isTestWhileIdle = true
        datasource.maxPoolPreparedStatementPerConnectionSize = 16
        datasource.validationQuery = "select current_timestamp"

        Instep.bind(ConnectionManager::class.java, DefaultConnectionManager(datasource))
        Instep.bind(InstepLogger::class.java, object : InstepLogger {
            override fun debug(log: String) {
                println(log)
            }

            override fun info(log: String) {
                println(log)
            }

            override fun warn(log: String) {
                System.err.println(log)
            }
        })
    }

    @Test
    fun executeScalar() {
        val scalar = InstepSQL.executeScalar("""SELECT to_char(current_timestamp, 'YYYY-MM-DD HH24\:MI\:SS.FF3')""")
        LocalDateTime.parse(scalar, DateTimeFormatter.ofPattern("""yyyy-MM-dd HH:mm:ss.SSS"""))
    }
}