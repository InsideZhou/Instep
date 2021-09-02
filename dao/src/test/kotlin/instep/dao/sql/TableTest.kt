package instep.dao.sql

import instep.Instep
import instep.dao.sql.dialect.MySQLDialect
import instep.dao.sql.dialect.PostgreSQLDialect
import instep.dao.sql.dialect.SQLServerDialect
import org.testng.Assert
import java.time.*
import java.util.*

@Suppress("unused")
object TableTest {
    val stringGenerator = net.moznion.random.string.RandomStringGenerator()
    val datasource = InstepSQLTest.datasource

    val birthDate = LocalDate.of(1993, 6, 6)
    val birthTime = LocalTime.of(6, 6)
    val birthday = OffsetDateTime.of(birthDate, birthTime, ZoneOffset.UTC)

    @Suppress("UNUSED_PARAMETER")
    enum class AccountType(code: String, desc: String) {
        Admin("admin", "管理员"), User("user", "用户");
    }

    class Account {
        var id = UUID.randomUUID().toString()
        var type = AccountType.User
        var name = ""
        var balance: java.math.BigDecimal = java.math.BigDecimal.ZERO
        var createdAt: Instant = Instant.now()
        var birthDate: LocalDate? = null
        var birthTime: LocalTime? = null
        var avatar = byteArrayOf()
        var remark: String? = null
        var preferences = emptyMap<String, Any?>()
    }

    abstract class AbstractTable(tableName: String, tableComment: String, dialect: Dialect) : Table(tableName, tableComment, dialect) {
        constructor(tableName: String, tableComment: String) : this(tableName, tableComment, Instep.make(Dialect::class.java))
        constructor(tableName: String) : this(tableName, "")

        val id = uuid("id").primary()
    }

    object AccountTable : AbstractTable("account_" + stringGenerator.generateByRegex("[a-z]{8}"), "账号表") {
        val type = varchar("type", 16).notnull().comment("账号类型")
        val code = varchar("code", 16).unique()
        val name = varchar("name", 256).notnull()
        val balance = when (dialect) {
            is MySQLDialect -> numeric("balance", 65, 2).notnull()
            is PostgreSQLDialect -> numeric("balance", 1000, 2).notnull()
            is SQLServerDialect -> numeric("balance", 38, 2).notnull()
            else -> numeric("balance", Int.MAX_VALUE, 2).notnull()
        }
        val createdAt = instant("created_at").notnull()
        var birthDate = date("birth_date")
        var birthTime = time("birth_time")
        var birthday = if (dialect.offsetDateTimeSupported) {
            offsetDateTime("birthday")
        }
        else {
            datetime("birthday")
        }
        val avatar = lob("avatar")
        val remark = varchar("remark", 512)
        val preferences = json("preferences").default("'{}'::jsonb")
    }

    @org.testng.annotations.Test
    fun createAccountTable() {
        AccountTable.create().debug().execute()
    }

    @org.testng.annotations.Test(dependsOnMethods = arrayOf("createAccountTable"), priority = 1)
    fun addColumn() {
        AccountTable.addColumn(AccountTable.boolean("verified").default("false")).debug().execute()
    }

    @org.testng.annotations.Test(dependsOnMethods = arrayOf("createAccountTable"))
    fun insertAccounts() {
        val random = Random()
        val total = random.ints(10, 100).findAny().orElse(100)

        for (index in 0..total) {
            AccountTable.insert()
                .addValue(AccountTable.id, UUID.randomUUID().toString())
                .addValue(AccountTable.type, AccountType.Admin)
                .addValue(AccountTable.name, stringGenerator.generateByRegex("\\w{1,256}"))
                .addValue(AccountTable.code, stringGenerator.generateByRegex("\\w{6}"))
                .addValue(AccountTable.balance, random.nextDouble())
                .addValue(AccountTable.createdAt, Instant.now())
                .addValue(AccountTable.birthDate, birthDate)
                .addValue(AccountTable.birthTime, birthTime)
                .addValue(AccountTable.birthday, birthday)
                .addValue(AccountTable.preferences, """{"a":1,"b":2}""")
                .returning()
                .debug()
                .execute()
        }
    }

    @org.testng.annotations.Test(dependsOnMethods = arrayOf("insertAccounts"))
    fun maxAccountId() {
        val latest = AccountTable.select(AccountTable.createdAt.max()).executeScalar(Instant::class.java)
        AccountTable.select(AccountTable.id).where(AccountTable.createdAt eq latest!!).executeScalar()
    }

    @org.testng.annotations.Test(dependsOnMethods = arrayOf("maxAccountId"))
    fun updateAccounts() {
        val latest = AccountTable.select(AccountTable.createdAt.max()).executeScalar(Instant::class.java)
        val id = AccountTable.select(AccountTable.id).where(AccountTable.createdAt eq latest!!).executeScalar()

        AccountTable.update()
            .set(AccountTable.name, "laozi")
            .set(AccountTable.balance, 3.33)
            .whereKey(id)
            .debug()
            .executeUpdate()

        var laozi = AccountTable[id]!!
        assert(laozi[AccountTable.name] == "laozi")
        assert(laozi[AccountTable.balance] == 3.33)


        AccountTable.update()
            .set(AccountTable.name, "dao de jing")
            .set(AccountTable.balance, 6.66)
            .where(AccountTable.name eq "laozi", AccountTable.balance lte 3.33)
            .executeUpdate()

        laozi = AccountTable.select().where(AccountTable.id eq id).execute().single()
        assert(laozi[AccountTable.name] == "dao de jing")
        assert(laozi[AccountTable.balance] == 6.66)

        AccountTable.update()
            .step(AccountTable.balance, 2.22)
            .whereKey(id)
            .executeUpdate()

        laozi = AccountTable.select().where(AccountTable.id eq id).execute().single()
        assert(laozi[AccountTable.name] == "dao de jing")
        assert(laozi[AccountTable.balance] == 8.88)

        AccountTable.update()
            .step(AccountTable.balance, -1)
            .whereKey(id)
            .executeUpdate()

        laozi = AccountTable.select().where(AccountTable.id eq id).execute().single()
        assert(laozi[AccountTable.balance] == 7.88)
    }

    @org.testng.annotations.Test(dependsOnMethods = arrayOf("maxAccountId"))
    fun deleteAccounts() {
        val latest = AccountTable.select(AccountTable.createdAt.max()).executeScalar(Instant::class.java)
        val id = AccountTable.select(AccountTable.id).where(AccountTable.createdAt eq latest!!).executeScalar()

        AccountTable.delete().where(AccountTable.id eq id).executeUpdate()
        assert(null == AccountTable[id])
    }

    @org.testng.annotations.Test(dependsOnMethods = arrayOf("insertAccounts"))
    fun datetime() {
        val latest = AccountTable.select(AccountTable.createdAt.max()).executeScalar(Instant::class.java)
        val id = AccountTable.select(AccountTable.id).where(AccountTable.createdAt eq latest!!).executeScalar()

        val account = AccountTable.select().where(AccountTable.id eq id).execute().single()

        Assert.assertThrows(UnsupportedOperationException::class.java, { account[AccountTable.birthDate] })
        Assert.assertThrows(UnsupportedOperationException::class.java, { account[AccountTable.birthTime] })

        assert(account.getLocalDate(AccountTable.birthDate) == birthDate)
        assert(account.getLocalTime(AccountTable.birthTime) == birthTime)

        if (dialect.offsetDateTimeSupported) {
            assert(account.getOffsetDateTime(AccountTable.birthday) == OffsetDateTime.of(birthDate, birthTime, ZoneOffset.UTC))
        }
        else {
            assert(account.getOffsetDateTime(AccountTable.birthday) == OffsetDateTime.of(birthDate, birthTime, OffsetDateTime.now().offset))
        }

        Assert.assertThrows(UnsupportedOperationException::class.java, { account.getLocalDateTime(AccountTable.birthDate) })
        Assert.assertThrows(UnsupportedOperationException::class.java, { account.getLocalDateTime(AccountTable.birthTime) })
    }

    @org.testng.annotations.Test(dependsOnMethods = arrayOf("insertAccounts"))
    fun rowToInstance() {
        val latest = AccountTable.select(AccountTable.createdAt.max()).executeScalar(Instant::class.java)
        val id = AccountTable.select(AccountTable.id).where(AccountTable.createdAt eq latest!!).executeScalar()

        val account = AccountTable.select().where(AccountTable.id eq id).execute(Account::class.java).single()
        assert(account.id == id)
        assert(account.birthDate == birthDate)
        assert(account.birthTime == birthTime)
        assert(account.remark == null)
    }

    @org.testng.annotations.Test(dependsOnMethods = arrayOf("insertAccounts"))
    fun randomRow() {
        val latest = AccountTable.select(AccountTable.createdAt.max()).executeScalar(Instant::class.java)
        val idMax = AccountTable.select(AccountTable.id).where(AccountTable.createdAt eq latest!!).executeScalar()

        val oldest = AccountTable.select(AccountTable.createdAt.max()).executeScalar(Instant::class.java)
        val idMin = AccountTable.select(AccountTable.id).where(AccountTable.createdAt eq oldest!!).executeScalar()

        val idArray = setOf(idMax, idMin).toTypedArray()

        val plan = AccountTable.select(AccountTable.id.count()).where(AccountTable.id inArray idArray)

        assert(plan.executeScalar() == idArray.size.toString())
    }
}
