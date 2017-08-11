package instep.dao.sql

import com.alibaba.druid.pool.DruidDataSource
import instep.Instep
import instep.InstepLogger
import instep.dao.sql.dialect.HSQLDialect
import instep.dao.sql.dialect.MySQLDialect
import net.moznion.random.string.RandomStringGenerator
import org.testng.annotations.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object InstepSQLTest {
    val stringGenerator = RandomStringGenerator()
    val datasourceUrl: String = System.getProperty("instep.test.jdbc_url", "jdbc:hsqldb:mem:instep_orm")
    val dialect = Dialect.of(datasourceUrl)
    val datasource = DruidDataSource()

    object TransactionTable : Table("transaction_" + stringGenerator.generateByRegex("[a-z]{8}"), dialect) {
        val id = autoIncrementLong("id").primary()
        val name = varchar("name", 256).notnull()
    }

    init {
        datasource.url = datasourceUrl
        datasource.initialSize = 1
        datasource.minIdle = 1
        datasource.maxActive = 2
        datasource.timeBetweenEvictionRunsMillis = 60000
        datasource.minEvictableIdleTimeMillis = 300000
        datasource.isTestWhileIdle = true
        datasource.maxPoolPreparedStatementPerConnectionSize = 16
        datasource.validationQuery = "VALUES(current_timestamp)"

        Instep.bind(ConnectionProvider::class.java, TransactionContext.ConnectionProvider(datasource, dialect))
        Instep.bind(InstepLogger::class.java, object : InstepLogger {
            override val enableDebug: Boolean = true
            override val enableInfo: Boolean = true
            override val enableWarning: Boolean = true

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

        TransactionTable.create().execute()
    }

    @Test
    fun executeScalar() {
        val scalar = when (dialect) {
            is HSQLDialect -> InstepSQL.executeScalar("""VALUES(to_char(current_timestamp, 'YYYY-MM-DD HH24\:MI\:SS'))""")
            is MySQLDialect -> InstepSQL.executeScalar("""SELECT date_format(current_timestamp, '%Y-%m-%d %k\:%i\:%S')""")
            else -> InstepSQL.executeScalar("""SELECT to_char(current_timestamp, 'YYYY-MM-DD HH24\:MI\:SS')""")
        }
        LocalDateTime.parse(scalar, DateTimeFormatter.ofPattern("""yyyy-MM-dd HH:mm:ss"""))
    }

    @Test
    fun transaction() {
        InstepSQL.committedTransaction {
            val row = TableRow()
            row[TransactionTable.name] = stringGenerator.generateByRegex("\\w{8,64}")
            TransactionTable[1] = row
        }
        assert(TransactionTable[1] != null)

        InstepSQL.serializableTransaction {
            val row = TableRow()
            row[TransactionTable.name] = stringGenerator.generateByRegex("\\w{8,64}")
            TransactionTable[2] = row
            abort()
        }
        assert(TransactionTable[2] == null)

        assert(TransactionTable.select(TransactionTable.id.count()).distinct().executeScalar().toInt() == 1)
    }
}