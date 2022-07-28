package instep.dao.sql

import org.testng.Assert
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import java.time.Instant
import java.util.*


object PaginationTest {
    object PaginationTable : Table("pagination_" + ConditionTest.stringGenerator.generateByRegex("[a-z]{8}")) {
        val id = this.uuid("id").primary()
        val index = int("index").notnull()
        val name = PaginationTable.varchar("name", 256).notnull()
        val createdAt = instant("created_at")
    }

    init {
        InstepSQLTest
    }

    @BeforeClass
    fun init() {
        PaginationTable.create().debug().execute()

        val random = Random()
        val total = random.ints(10, 100).findAny().orElse(100)

        for (index in 0..total) {
            PaginationTable.insert()
                .addValue(PaginationTable.id, UUID.randomUUID().toString())
                .addValue(PaginationTable.index, index)
                .addValue(PaginationTable.name, TableTest.stringGenerator.generateByRegex("\\w{1,256}"))
                .addValue(PaginationTable.createdAt, Instant.now())
                .returning()
                .debug()
                .execute()
        }
    }

    @AfterClass()
    fun cleanUp() {
        PaginationTable.drop().execute()
    }

    @org.testng.annotations.Test
    fun createAccountTable() {
        val planDesc = PaginationTable.select().orderBy(PaginationTable.index.desc())
        var limit = 3
        val offset = 1
        var rows = planDesc.limit(limit).debug().execute()
        Assert.assertEquals(rows.size, limit)

        val lastRow = rows[0]

        Assert.assertEquals(
            planDesc.limit(limit).offset(offset).debug().execute()[0][PaginationTable.index],
            lastRow[PaginationTable.index] - 1
        )

        val planAsc = PaginationTable.select().orderBy(PaginationTable.index.asc())
        limit = 5
        rows = planAsc.limit(limit).debug().execute()
        Assert.assertEquals(rows.size, limit)

        val firstRow = rows[0]

        Assert.assertEquals(
            planAsc.limit(limit).offset(offset).debug().execute()[0][PaginationTable.index],
            firstRow[PaginationTable.index] + offset
        )
    }
}