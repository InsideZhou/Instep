package instep.orm

import instep.orm.sql.Table

object AccountTable : Table("account") {
    val id = autoIncrementLong("id").primary()
    val name = varchar("name", 256).nullable(false)
    val balance = numeric("balance", Int.MAX_VALUE, 2).nullable(false)
    val createdAt = datetime("created_at").nullable(false)
    val avatar = lob("avatar")
}
