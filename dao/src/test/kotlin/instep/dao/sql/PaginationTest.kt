package instep.dao.sql


object PaginationTest {
    init {
        InstepSQLTest

        TableTest.createAccountTable()
        TableTest.insertAccounts()
    }

    @org.testng.annotations.Test
    fun createAccountTable() {
        TableTest.AccountTable.select().orderBy(TableTest.AccountTable.createdAt.desc()).limit(3).debug().execute()
        TableTest.AccountTable.select().orderBy(TableTest.AccountTable.createdAt.desc()).limit(3).offset(1).debug().execute()
        TableTest.AccountTable.select().limit(3).offset(1).debug().execute()
        TableTest.AccountTable.select().limit(3).debug().execute()
    }
}