package instep.dao.sql

import com.alibaba.druid.pool.DruidDataSource
import instep.Instep
import instep.InstepLogger
import instep.InstepLoggerFactory
import instep.dao.sql.dialect.HSQLDialect
import instep.dao.sql.dialect.MySQLDialect
import instep.dao.sql.dialect.SQLServerDialect
import net.moznion.random.string.RandomStringGenerator
import org.testng.Assert
import org.testng.annotations.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object InstepSQLTest {
    val stringGenerator = RandomStringGenerator()
    val datasourceUrl: String = System.getProperty("instep.test.jdbc_url", "jdbc:hsqldb:mem:instep_orm")
    val dialect = Dialect.of(datasourceUrl)
    val datasource = DruidDataSource()

    object TransactionTable : Table("transaction_" + stringGenerator.generateByRegex("[a-z]{8}"), "transaction test", dialect) {
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

        Instep.bind(ConnectionProvider::class.java, TransactionContext.ConnectionProvider(datasource, dialect))
        Instep.bind(InstepLoggerFactory::class.java, object : InstepLoggerFactory {
            override fun getLogger(cls: Class<*>): InstepLogger {
                return object : InstepLogger {
                    var msg = ""
                    var context = mutableMapOf<String, Any>()
                    var t: Throwable? = null

                    override fun message(message: String): InstepLogger {
                        msg = message
                        return this
                    }

                    override fun exception(e: Throwable): InstepLogger {
                        t = e
                        return this
                    }

                    override fun context(key: String, value: Any): InstepLogger {
                        context[key] = value
                        return this
                    }

                    override fun context(key: String, lazy: () -> String): InstepLogger {
                        context[key] = lazy.invoke()
                        return this
                    }

                    override fun debug() {
                        println(msg)
                        println(context)
                    }

                    override fun info() {
                        println(msg)
                        println(context)
                    }

                    override fun warn() {
                        System.err.println(msg)
                        System.err.println(context)
                    }
                }
            }
        })
        TransactionTable.create().debug().execute()
    }

    @Test
    fun executeScalar() {
        val scalar = when (dialect) {
            is HSQLDialect -> InstepSQL.plan("""VALUES(to_char(current_timestamp, 'YYYY-MM-DD HH24\:MI\:SS'))""").executeScalar()
            is MySQLDialect -> InstepSQL.plan("""SELECT date_format(current_timestamp, '%Y-%m-%d %k\:%i\:%S')""").executeScalar()
            is SQLServerDialect -> InstepSQL.plan("""SELECT format(current_timestamp, 'yyyy-MM-dd HH:mm:ss')""").executeScalar()
            else -> InstepSQL.plan("""SELECT to_char(current_timestamp, 'YYYY-MM-DD HH24:MI:SS')""").executeScalar()
        }
        LocalDateTime.parse(scalar, DateTimeFormatter.ofPattern("""yyyy-MM-dd HH:mm:ss"""))

        Assert.assertNull(InstepSQL.plan("""SELECT NULL""").executeScalar(Int::class.java))
    }

    @Test
    fun transaction() {
        InstepSQL.transaction().committed {
            val row = TableRow()
            row[TransactionTable.name] = stringGenerator.generateByRegex("\\w{8,64}")
            TransactionTable[1] = row
        }
        assert(TransactionTable[1] != null)

        InstepSQL.transaction().serializable {
            val row = TableRow()
            row[TransactionTable.name] = stringGenerator.generateByRegex("\\w{8,64}")
            TransactionTable[2] = row
            abort()
        }
        assert(TransactionTable[2] == null)

        assert(TransactionTable.select(TransactionTable.id.count()).distinct().executeScalar().toInt() == 1)
    }
}