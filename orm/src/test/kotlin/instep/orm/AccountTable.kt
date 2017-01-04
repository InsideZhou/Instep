package instep.orm

import instep.orm.sql.*

object AccountTable : Table("account") {
    val id = IntegerColumn("id", IntegerColumnType.Long).primary().autoIncrement()
    val name = StringColumn("name", StringColumnType.Varchar).nullable(false)
}