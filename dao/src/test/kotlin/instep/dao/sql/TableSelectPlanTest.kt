package instep.dao.sql

import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test


@Suppress("MemberVisibilityCanBePrivate")
object TableSelectPlanTest {
    init {
        InstepSQLTest
    }

    val stringGenerator = net.moznion.random.string.RandomStringGenerator()

    object AccountTable : Table("account_" + stringGenerator.generateByRegex("[a-z]{8}"), "账号表") {
        val id = long("id").primary().autoIncrement()
        val name = varchar("name", 256).notnull()
    }

    object RoleTable : Table("role_" + stringGenerator.generateByRegex("[a-z]{8}"), "角色表") {
        val id = long("id").primary().autoIncrement()
        val name = varchar("name", 256).notnull()
    }

    object AccountRoleTable : Table("role_" + stringGenerator.generateByRegex("[a-z]{8}"), "用户角色关系表") {
        val accountId = long("account_id").notnull()
        val roleId = long("role_id").notnull()
        val remark = varchar("remark", 256).notnull()
    }

    var account = DataRow()
    var roleA = DataRow()
    var roleB = DataRow()

    @BeforeClass
    fun init() {
        AccountTable.create().debug().execute()
        RoleTable.create().debug().execute()
        AccountRoleTable.create().debug().execute()

        account = AccountTable.insert()
            .addValue(AccountTable.name, stringGenerator.generateByRegex("[a-zA-Z0-9]{8,16}"))
            .returning()
            .debug()
            .executeDataRow().first()

        roleA = RoleTable.insert()
            .addValue(RoleTable.name, stringGenerator.generateByRegex("[a-zA-Z0-9]{8,16}"))
            .returning()
            .debug()
            .executeDataRow().first()

        roleB = RoleTable.insert()
            .addValue(RoleTable.name, stringGenerator.generateByRegex("[a-zA-Z0-9]{8,16}"))
            .returning()
            .debug()
            .executeDataRow().first()

        AccountRoleTable.insert()
            .addValue(AccountRoleTable.accountId, account[AccountTable.id])
            .addValue(AccountRoleTable.roleId, roleA[RoleTable.id])
            .addValue(AccountRoleTable.remark, "A")
            .debug()
            .execute()

        AccountRoleTable.insert()
            .addValue(AccountRoleTable.accountId, account[AccountTable.id])
            .addValue(AccountRoleTable.roleId, roleB[RoleTable.id])
            .addValue(AccountRoleTable.remark, "B")
            .debug()
            .execute()
    }

    @AfterClass
    fun cleanUp() {
        AccountTable.drop().execute()
        RoleTable.drop().execute()
        AccountRoleTable.drop().execute()
    }

    @Test
    fun getAccountWithRoles() {
        AccountTable
            .selectAll().select(RoleTable.id)
            .join(AccountRoleTable.accountId).join(AccountRoleTable.roleId, RoleTable.id)
            .debug()
    }
}
