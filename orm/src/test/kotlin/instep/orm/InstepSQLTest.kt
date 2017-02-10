package instep.orm

import com.alibaba.druid.pool.DruidDataSource
import instep.Instep
import instep.InstepLogger
import instep.orm.sql.*
import instep.orm.sql.impl.DefaultConnectionProvider
import net.moznion.random.string.RandomStringGenerator
import org.testng.annotations.Test
import java.sql.Connection
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object InstepSQLTest {
    val stringGenerator = RandomStringGenerator()
    val datasource = DruidDataSource()

    object TransactionTable : Table("transaction_" + stringGenerator.generateByRegex("[a-z]{8}")) {
        val id = autoIncrementLong("id").primary()
        val name = varchar("name", 256).notnull()
    }

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

        Instep.bind(ConnectionProvider::class.java, DefaultConnectionProvider(datasource))
        Instep.bind(InstepLogger::class.java, object : InstepLogger {
            override val enableDebug: Boolean = true
            override val enableInfo: Boolean = true
            override val enableWarning: Boolean = true

            override val defaultLogger: String = this.javaClass.name

            override fun debug(log: String, logger: String) {
                println(log)
            }

            override fun info(log: String, logger: String) {
                println(log)
            }

            override fun warning(log: String, logger: String) {
                System.err.println(log)
            }
        })

        TransactionTable.create().debug().execute()
    }

    @Test
    fun executeScalar() {
        val scalar = InstepSQL.executeScalar("""SELECT to_char(current_timestamp, 'YYYY-MM-DD HH24\:MI\:SS.FF3')""")
        LocalDateTime.parse(scalar, DateTimeFormatter.ofPattern("""yyyy-MM-dd HH:mm:ss.SSS"""))
    }

    @Test
    fun transaction() {
        InstepSQL.transaction(Connection.TRANSACTION_SERIALIZABLE) {
            val row = TableRow()
            row[TransactionTable.name] = stringGenerator.generateByRegex("\\w{8,64}")
            TransactionTable[1] = row
        }
        assert(TransactionTable[1] != null)

        InstepSQL.transaction(Connection.TRANSACTION_SERIALIZABLE) {
            val row = TableRow()
            row[TransactionTable.name] = stringGenerator.generateByRegex("\\w{8,64}")
            TransactionTable[2] = row
            abort()
        }
        assert(TransactionTable[2] == null)

        assert(TransactionTable.select(TransactionTable.id.count()).distinct().debug().executeScalar().toInt() == 1)
    }
}