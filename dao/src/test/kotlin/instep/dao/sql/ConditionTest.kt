package instep.dao.sql

import instep.Instep


object ConditionTest {
    val stringGenerator = net.moznion.random.string.RandomStringGenerator()
    val datasource = InstepSQLTest.datasource

    class Account {
        var id = 0L
        var name = ""
    }

    object AccountTable : Table("account_" + stringGenerator.generateByRegex("[a-z]{8}")) {
        val id = AccountTable.autoIncrementLong("id").primary()
        val name = AccountTable.varchar("name", 256).notnull()
    }

    init {
        AccountTable.create().execute()

        insertAccounts()
    }

    private fun insertAccounts() {
        AccountTable.insert()
            .addValue(AccountTable.name, "abc")
            .execute()

        AccountTable.insert()
            .addValue(AccountTable.name, "bcd")
            .execute()


        AccountTable.insert()
            .addValue(AccountTable.name, "cde")
            .execute()

        AccountTable.insert()
            .addValue(AccountTable.name, "def")
            .execute()

        AccountTable.insert()
            .addValue(AccountTable.name, "efg")
            .execute()

        AccountTable.insert()
            .addValue(AccountTable.name, "fgh")
            .execute()

        AccountTable.insert()
            .addValue(AccountTable.name, "ghi")
            .execute()

        AccountTable.insert()
            .addValue(AccountTable.name, "hij")
            .execute()

        AccountTable.insert()
            .addValue(AccountTable.name, "ijk")
            .execute()

        AccountTable.insert()
            .addValue(AccountTable.name, "jkl")
            .execute()

        AccountTable.insert()
            .addValue(AccountTable.name, "lmn")
            .execute()
    }

    @org.testng.annotations.Test
    fun containsTest() {
        val a = AccountTable.select(AccountTable.id.count()).where(AccountTable.name contains "a").executeScalar()
        val i = AccountTable.select(AccountTable.id.count()).where(AccountTable.name contains "i").executeScalar()
        val de = AccountTable.select(AccountTable.id.count()).where(AccountTable.name contains "de").executeScalar()
        val mn = AccountTable.select(AccountTable.id.count()).where(AccountTable.name contains "mn").executeScalar()

        assert(a == "1")
        assert(i == "3")
        assert(de == "2")
        assert(mn == "1")
    }

    @org.testng.annotations.Test
    fun startsWithTest() {
        val a = AccountTable.select(AccountTable.id.count()).where(AccountTable.name startsWith  "a").executeScalar()
        val i = AccountTable.select(AccountTable.id.count()).where(AccountTable.name startsWith  "i").executeScalar()
        val de = AccountTable.select(AccountTable.id.count()).where(AccountTable.name startsWith  "de").executeScalar()
        val mn = AccountTable.select(AccountTable.id.count()).where(AccountTable.name startsWith  "mn").executeScalar()

        assert(a == "1")
        assert(i == "1")
        assert(de == "1")
        assert(mn == "0")
    }

    @org.testng.annotations.Test
    fun endsWithTest() {
        val a = AccountTable.select(AccountTable.id.count()).where(AccountTable.name endsWith   "a").executeScalar()
        val i = AccountTable.select(AccountTable.id.count()).where(AccountTable.name endsWith   "i").executeScalar()
        val de = AccountTable.select(AccountTable.id.count()).where(AccountTable.name endsWith   "de").executeScalar()
        val mn = AccountTable.select(AccountTable.id.count()).where(AccountTable.name endsWith   "mn").executeScalar()

        assert(a == "0")
        assert(i == "1")
        assert(de == "1")
        assert(mn == "1")
    }
}