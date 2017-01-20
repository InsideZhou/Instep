package instep.orm

import instep.orm.sql.Table
import instep.orm.sql.execute
import net.moznion.random.string.RandomStringGenerator
import org.testng.annotations.Test
import java.time.LocalDateTime
import java.util.*


object AccountTable : Table("account") {
    val id = autoIncrementLong("id").primary()
    val name = varchar("name", 256).nullable(false)
    val balance = numeric("balance", Int.MAX_VALUE, 2).nullable(false)
    val createdAt = datetime("created_at").nullable(false)
    val avatar = lob("avatar")
}

val stringGenerator = RandomStringGenerator()

object TableTest {
    @Test
    fun createAccountTable() {
        AccountTable.create().log().execute()
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
                null
            ).execute()
        }

        for (index in 0..total) {
            val name = stringGenerator.generateByRegex("\\w{1,256}")
            AccountTable.insert()
                .addValue(AccountTable.name, name)
                .addValue(AccountTable.balance, random.nextDouble())
                .addValue(AccountTable.createdAt, LocalDateTime.now())
                .log()
                .execute()
        }
    }
}
