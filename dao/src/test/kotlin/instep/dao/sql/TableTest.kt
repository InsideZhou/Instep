package instep.dao.sql

import instep.Instep
import instep.dao.sql.dialect.MySQLDialect
import instep.dao.sql.dialect.PostgreSQLDialect
import java.time.*
import java.util.*

@Suppress("unused")
object TableTest {
    val stringGenerator = net.moznion.random.string.RandomStringGenerator()
    val datasource = InstepSQLTest.datasource
    val dialect = Instep.make(Dialect::class.java)

    val birthDate = LocalDate.of(1993, 6, 6)
    val birthTime = LocalTime.of(6, 6)
    val birthday = OffsetDateTime.of(birthDate, birthTime, ZoneOffset.UTC)

    class Account {
        var id = UUID.randomUUID().toString()
        var name = ""
        var balance: java.math.BigDecimal = java.math.BigDecimal.ZERO
        var createdAt: Instant = Instant.now()
        var birthDate: LocalDate? = null
        var birthTime: LocalTime? = null
        var avatar = byteArrayOf()
        var preferences = emptyMap<String, Any?>()
    }

    object AccountTable : Table("account_" + stringGenerator.generateByRegex("[a-z]{8}")) {
        val id = AccountTable.uuid("id").primary()
        val name = AccountTable.varchar("name", 256).notnull()
        val balance = when (AccountTable.dialect) {
            is MySQLDialect -> AccountTable.numeric("balance", 65, 2).notnull()
            is PostgreSQLDialect -> AccountTable.numeric("balance", 1000, 2).notnull()
            else -> AccountTable.numeric("balance", Int.MAX_VALUE, 2).notnull()
        }
        val createdAt = AccountTable.instant("created_at").notnull()
        var birthDate = AccountTable.date("birth_date")
        var birthTime = AccountTable.time("birth_time")
        var birthday = if (AccountTable.dialect.isOffsetDateTimeSupported) {
            AccountTable.offsetDateTime("birthday")
        }
        else {
            AccountTable.datetime("birthday")
        }
        val avatar = AccountTable.lob("avatar")
        val preferences = AccountTable.json("preferences").default("'{}'::jsonb")
    }

    @org.testng.annotations.Test
    fun createAccountTable() {
        AccountTable.createIfNotExists().execute()
    }

    @org.testng.annotations.Test(dependsOnMethods = arrayOf("createAccountTable"), priority = 1)
    fun addColumn() {
        AccountTable.addColumn(AccountTable.boolean("verified").default("FALSE")).execute()
    }

    @org.testng.annotations.Test(dependsOnMethods = arrayOf("createAccountTable"))
    fun insertAccounts() {
        val random = java.util.Random()
        val total = random.ints(10, 100).findAny().orElse(100)

        for (index in 0..total) {
            val name = stringGenerator.generateByRegex("\\w{1,256}")
            AccountTable.insert()
                .addValue(AccountTable.id, UUID.randomUUID().toString())
                .addValue(AccountTable.name, name)
                .addValue(AccountTable.balance, random.nextDouble())
                .addValue(AccountTable.createdAt, Instant.now())
                .addValue(AccountTable.birthDate, birthDate)
                .addValue(AccountTable.birthTime, birthTime)
                .addValue(AccountTable.birthday, birthday)
                .addValue(AccountTable.preferences, """{"a":1,"b":2}""")
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
            .where(id)
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

        assert(account[AccountTable.birthDate] == OffsetDateTime.of(birthDate, LocalTime.MIDNIGHT, ZonedDateTime.now().offset))
        assert(account[AccountTable.birthTime] == OffsetDateTime.of(LocalDate.ofEpochDay(0), birthTime, ZonedDateTime.now().offset))

        if (dialect.isOffsetDateTimeSupported) {
            assert(account[AccountTable.birthday] == OffsetDateTime.of(birthDate, birthTime, ZoneOffset.UTC))
        }
        else {
            assert(account[AccountTable.birthday] == OffsetDateTime.of(birthDate, birthTime, OffsetDateTime.now().offset))
        }

        assert(account.getLocalDateTime(AccountTable.birthDate) == LocalDateTime.of(birthDate, LocalTime.MIDNIGHT))
        assert(account.getLocalDateTime(AccountTable.birthTime) == LocalDateTime.of(LocalDate.ofEpochDay(0), birthTime))
    }

    @org.testng.annotations.Test(dependsOnMethods = arrayOf("insertAccounts"))
    fun rowToInstance() {
        val latest = AccountTable.select(AccountTable.createdAt.max()).executeScalar(Instant::class.java)
        val id = AccountTable.select(AccountTable.id).where(AccountTable.createdAt eq latest!!).executeScalar()

        val account = AccountTable.select().where(AccountTable.id eq id).execute(Account::class.java).single()
        assert(account.id == id)
        assert(account.birthDate == birthDate)
        assert(account.birthTime == birthTime)
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
