package instep.orm

import com.alibaba.druid.pool.DruidDataSource
import instep.Instep
import instep.InstepLogger
import instep.collection.AssocArray
import instep.orm.sql.ConnectionManager
import instep.orm.sql.InstepSQL
import instep.orm.sql.execute
import instep.orm.sql.executeUpdate
import instep.orm.sql.impl.DefaultConnectionManager
import instep.typeconvert.TypeConvert
import net.moznion.random.string.RandomStringGenerator
import org.testng.annotations.Test
import java.sql.ResultSet
import java.util.*

object InstepSQLTest {
    val datasource = DruidDataSource()
    val stringGenerator = RandomStringGenerator()

    private val accountNames = mutableListOf<String>()

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
    fun requiredService() {
        Instep.make(ConnectionManager::class.java)
    }

    @Test
    fun optionalService() {
        val typeconvert = Instep.make(TypeConvert::class.java)
        assert(typeconvert.canConvert(ResultSet::class.java, AssocArray::class.java))
    }

    @Test
    fun createAccountTable() {
        val plan = AccountTable.create()
        InstepLogger.info(plan.statement)
        plan.execute()
    }

    @Test(dependsOnMethods = arrayOf("createAccountTable"))
    fun insertAccounts() {
        val random = Random()
        val total = random.ints(10, 100).findAny().orElse(100)

        accountNames.clear()

        for (index in 0..total) {
            val name = stringGenerator.generateByRegex("\\w{1,256}")
            InstepSQL.plan("""insert into account(name) values(?)""").addParameters(name).executeUpdate()
            accountNames.add(name)
        }
    }

    @Test
    fun rawSQLStatement() {
        val raw = InstepSQL.plan("""select * from account where name=:name""")
        assert("""select * from account where name=?""" == raw.statement)
    }

    @Test
    fun rawSQLParameters() {
        val raw = InstepSQL.plan("""select * from account where name=:name""").addParameter("name", "haha")
        assert(raw.parameters.all { item -> item !is PlaceHolder })
    }

    @Test(dependsOnMethods = arrayOf("insertAccounts", "optionalService"))
    fun selectAccounts() {
        val random = Random()
        val totalTaken = random.nextInt(accountNames.size)

        val accountIndexes = mutableListOf<Int>()
        for (index in 0..totalTaken) {
            accountIndexes.add(random.nextInt(accountNames.size))
        }

        accountIndexes.map { index -> accountNames[index] }.forEach { name ->
            val record = InstepSQL.plan("""select * from account where name=:name""").addParameter("name", name).execute(AssocArray::class.java).first()
            assert(record["name"] as String == name)
        }
    }
}