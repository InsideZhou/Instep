@file:Suppress("unused")

package instep.dao.sql

import org.testng.Assert
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import java.time.Duration
import java.time.Instant


object ConditionTest {
    val stringGenerator = net.moznion.random.string.RandomStringGenerator()
    val datasource = InstepSQLTest.datasource

    class Account {
        var id = 0L
        var name = ""
        var createdAt: Instant? = Instant.EPOCH
    }

    object AccountTable : Table("account_" + stringGenerator.generateByRegex("[a-z]{8}")) {
        val id = AccountTable.autoIncrementLong("id").primary()
        val name = AccountTable.varchar("name", 256).notnull()
        val createdAt = instant("created_at")
    }

    @BeforeClass
    fun init() {
        AccountTable.create().execute()
        insertAccounts()
    }

    @AfterClass()
    fun cleanUp() {
        AccountTable.drop().execute()
    }

    private fun insertAccounts() {
        val now = Instant.now()

        AccountTable.insert()
            .addValue(AccountTable.name, "abc")
            .addValue(AccountTable.createdAt, now - Duration.ofMinutes(11))
            .execute()

        AccountTable.insert()
            .addValue(AccountTable.name, "bcd")
            .addValue(AccountTable.createdAt, now - Duration.ofMinutes(10))
            .execute()


        AccountTable.insert()
            .addValue(AccountTable.name, "cde")
            .addValue(AccountTable.createdAt, now - Duration.ofMinutes(9))
            .execute()

        AccountTable.insert()
            .addValue(AccountTable.name, "def")
            .addValue(AccountTable.createdAt, now - Duration.ofMinutes(8))
            .execute()

        AccountTable.insert()
            .addValue(AccountTable.name, "efg")
            .addValue(AccountTable.createdAt, now - Duration.ofMinutes(7))
            .execute()

        AccountTable.insert()
            .addValue(AccountTable.name, "fgh")
            .addValue(AccountTable.createdAt, now - Duration.ofMinutes(6))
            .execute()

        AccountTable.insert()
            .addValue(AccountTable.name, "ghi")
            .addValue(AccountTable.createdAt, now - Duration.ofMinutes(5))
            .execute()

        AccountTable.insert()
            .addValue(AccountTable.name, "hij")
            .addValue(AccountTable.createdAt, now - Duration.ofMinutes(4))
            .execute()

        AccountTable.insert()
            .addValue(AccountTable.name, "ijk")
            .addValue(AccountTable.createdAt, now - Duration.ofMinutes(3))
            .execute()

        AccountTable.insert()
            .addValue(AccountTable.name, "jkl")
            .addValue(AccountTable.createdAt, now - Duration.ofMinutes(2))
            .execute()

        AccountTable.insert()
            .addValue(AccountTable.name, "lmn")
            .addValue(AccountTable.createdAt, now - Duration.ofMinutes(1))
            .execute()
    }

    @org.testng.annotations.Test
    fun containsTest() {
        val a = AccountTable.selectExpression(AccountTable.id.count()).where(AccountTable.name contains "a").executeScalar()
        val i = AccountTable.selectExpression(AccountTable.id.count()).where(AccountTable.name contains "i").executeScalar()
        val de = AccountTable.selectExpression(AccountTable.id.count()).where(AccountTable.name contains "de").executeScalar()
        val mn = AccountTable.selectExpression(AccountTable.id.count()).where(AccountTable.name contains "mn").executeScalar()

        assert(a == "1")
        assert(i == "3")
        assert(de == "2")
        assert(mn == "1")
    }

    @org.testng.annotations.Test
    fun startsWithTest() {
        val a = AccountTable.selectExpression(AccountTable.id.count()).where(AccountTable.name startsWith "a").executeScalar()
        val i = AccountTable.selectExpression(AccountTable.id.count()).where(AccountTable.name startsWith "i").executeScalar()
        val de = AccountTable.selectExpression(AccountTable.id.count()).where(AccountTable.name startsWith "de").executeScalar()
        val mn = AccountTable.selectExpression(AccountTable.id.count()).where(AccountTable.name startsWith "mn").executeScalar()

        assert(a == "1")
        assert(i == "1")
        assert(de == "1")
        assert(mn == "0")
    }

    @org.testng.annotations.Test
    fun endsWithTest() {
        val a = AccountTable.selectExpression(AccountTable.id.count()).where(AccountTable.name endsWith "a").executeScalar()
        val i = AccountTable.selectExpression(AccountTable.id.count()).where(AccountTable.name endsWith "i").executeScalar()
        val de = AccountTable.selectExpression(AccountTable.id.count()).where(AccountTable.name endsWith "de").executeScalar()
        val mn = AccountTable.selectExpression(AccountTable.id.count()).where(AccountTable.name endsWith "mn").executeScalar()

        assert(a == "0")
        assert(i == "1")
        assert(de == "1")
        assert(mn == "1")
    }

    @org.testng.annotations.Test
    fun beforeNowTest() {
        val now = Instant.now()
        val count = AccountTable.selectExpression(AccountTable.id.count()).where(AccountTable.createdAt lte now).debug().executeScalar(Long::class.java)

        Assert.assertEquals(11L, count)
    }
}