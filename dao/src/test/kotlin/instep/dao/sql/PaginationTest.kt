package instep.dao.sql

import instep.Instep


object PaginationTest {
    val datasource = InstepSQLTest.datasource
    val dialect = Instep.make(Dialect::class.java)

    init {
        TableTest.createAccountTable()
        TableTest.insertAccounts()
    }


    @org.testng.annotations.Test
    fun createAccountTable() {
        TableTest.AccountTable.select().orderBy(TableTest.AccountTable.createdAt.desc()).limit(3)
    }
}