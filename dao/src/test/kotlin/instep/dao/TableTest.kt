package instep.dao

import instep.dao.sql.*
import net.moznion.random.string.RandomStringGenerator
import org.testng.annotations.Test
import java.time.*
import java.util.*

object TableTest {
    val stringGenerator = RandomStringGenerator()
    val datasource = InstepSQLTest.datasource

    val birthDate = LocalDate.of(1993, 6, 6)
    val birthTime = LocalTime.of(6, 6)
    val birthday = OffsetDateTime.of(birthDate, birthTime, ZonedDateTime.now().offset)

    object AccountTable : Table("account_" + stringGenerator.generateByRegex("[a-z]{8}")) {
        val id = autoIncrementLong("id").primary()
        val name = varchar("name", 256).notnull()
        val balance = numeric("balance", Int.MAX_VALUE, 2).notnull()
        val createdAt = datetime("created_at").notnull()
        var birthDate = date("birth_date")
        var birthTime = time("birth_time")
        var birthday = offsetDateTime("birthday")
        val avatar = lob("avatar")
    }

    @Test
    fun createAccountTable() {
        AccountTable.create().execute()
    }

    @Test(dependsOnMethods = arrayOf("createAccountTable"), priority = 1)
    fun addColumn() {
        AccountTable.addColumn(AccountTable.boolean("verified").default("FALSE")).execute()
    }

    @Test(dependsOnMethods = arrayOf("createAccountTable"))
    fun insertAccounts() {
        val random = Random()
        val total = random.ints(10, 100).findAny().orElse(100)

        for (index in 0..total) {
            val name = stringGenerator.generateByRegex("\\w{1,256}")
            AccountTable.insert().addValues(
                null,
                name,
                random.nextDouble(),
                LocalDateTime.now(),
                birthDate,
                birthTime,
                birthday,
                null
            ).execute()
        }

        for (index in 0..total) {
            val name = stringGenerator.generateByRegex("\\w{1,256}")
            AccountTable.insert()
                .addValue(AccountTable.name, name)
                .addValue(AccountTable.balance, random.nextDouble())
                .addValue(AccountTable.createdAt, LocalDateTime.now())
                .addValue(AccountTable.birthDate, birthDate)
                .addValue(AccountTable.birthTime, birthTime)
                .addValue(AccountTable.birthday, birthday)
                .execute()
        }
    }

    @Test(dependsOnMethods = arrayOf("insertAccounts"))
    fun maxAccountId() {
        AccountTable.select(AccountTable.id.max()).executeScalar().toInt()
    }

    @Test(dependsOnMethods = arrayOf("maxAccountId"))
    fun updateAccounts() {
        val random = Random()
        val max = AccountTable.select(AccountTable.id.max()).executeScalar().toInt()
        val id = random.ints(1, max).findAny().orElse(max)

        AccountTable.update()
            .set(AccountTable.name, "laozi")
            .set(AccountTable.balance, 3.33)
            .where(id)
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

    @Test(dependsOnMethods = arrayOf("maxAccountId"))
    fun deleteAccounts() {
        val random = Random()
        val max = AccountTable.select(AccountTable.id.max()).executeScalar().toInt()
        val id = random.ints(1, max).findAny().orElse(max)

        AccountTable.delete().where(AccountTable.id eq id).executeUpdate()
        assert(null == AccountTable[id])
    }

    @Test(dependsOnMethods = arrayOf("insertAccounts"))
    fun datetime() {
        val random = Random()
        val max = AccountTable.select(AccountTable.id.max()).executeScalar().toInt()
        val id = random.ints(1, max).findAny().orElse(max)

        val account = AccountTable.select().where(AccountTable.id eq id).execute().single()

        assert(account[AccountTable.birthDate] == OffsetDateTime.of(birthDate, LocalTime.MIDNIGHT, ZonedDateTime.now().offset))
        assert(account[AccountTable.birthTime] == OffsetDateTime.of(LocalDate.ofEpochDay(0), birthTime, ZonedDateTime.now().offset))
        assert(account[AccountTable.birthday] == OffsetDateTime.of(birthDate, birthTime, ZonedDateTime.now().offset))

        assert(account.getLocalDateTime(AccountTable.birthDate) == LocalDateTime.of(birthDate, LocalTime.MIDNIGHT))
        assert(account.getLocalDateTime(AccountTable.birthTime) == LocalDateTime.of(LocalDate.ofEpochDay(0), birthTime))
    }
}
