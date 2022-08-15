package instep.dao.sql

import org.testng.Assert
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test


@Suppress("MemberVisibilityCanBePrivate")
object TableSelectPlanTest {
    init {
        InstepSQLTest
    }

    val stringGenerator = net.moznion.random.string.RandomStringGenerator()

    object AccountTable : Table("account", "账号表") {
        val id = long("id").primary().autoIncrement()
        val name = varchar("name", 256).notnull()
    }

    object RoleTable : Table("role", "角色表") {
        val id = long("id").primary().autoIncrement()
        val name = varchar("name", 256).notnull()
    }

    object AccountRoleTable : Table("account_role", "用户角色关系表") {
        val accountId = long("account_id").notnull()
        val roleId = long("role_id").notnull()
        val remark = varchar("remark", 256).notnull()
    }

    class Account(var id: Long = 0, var name: String = "")
    class AccountWithRoleId(var id: Long = 0, var name: String = "", var roleId: Long = 0)

    class AccountWithRole(var id: Long = 0, var name: String = "", var roleId: Long = 0, var roleName: String = "")

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
            .execute(DataRow::class.java).first()

        roleA = RoleTable.insert()
            .addValue(RoleTable.name, stringGenerator.generateByRegex("[a-zA-Z0-9]{8,16}"))
            .returning()
            .debug()
            .execute(DataRow::class.java).first()

        roleB = RoleTable.insert()
            .addValue(RoleTable.name, stringGenerator.generateByRegex("[a-zA-Z0-9]{8,16}"))
            .returning()
            .debug()
            .execute(DataRow::class.java).first()

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
        val rolePrefix = "${RoleTable.tableName}_"

        var plan = AccountTable
            .selectExcept()
            .join(AccountRoleTable.accountId)
            .join(AccountRoleTable.roleId, RoleTable.id)
            .selectExpression(RoleTable.id.alias())
            .where(AccountRoleTable.accountId gt 0)
            .debug()

        val dataRows = plan.execute(DataRow::class.java)
        Assert.assertEquals(dataRows.size, 2)

        val accounts = plan.execute(Account::class.java)
        Assert.assertEquals(accounts.size, 2)

        val accountWithRoleIdList = plan.execute(AccountWithRoleId::class.java)
        Assert.assertEquals(accountWithRoleIdList[0].id, accountWithRoleIdList[1].id)
        Assert.assertEquals(accountWithRoleIdList[0].name, accountWithRoleIdList[1].name)
        Assert.assertEquals(accountWithRoleIdList[0].roleId, 1)
        Assert.assertEquals(accountWithRoleIdList[1].roleId, 2)

        plan = AccountTable
            .selectExcept()
            .join(AccountRoleTable.accountId)
            .join(AccountRoleTable.roleId, RoleTable.id)
            .select(RoleTable, rolePrefix)
            .where(AccountRoleTable.accountId gt 0)
            .orderBy(RoleTable.id.asc())
            .limit(1)
            .offset(1)
            .debug()

        val accountWithRoles = plan.execute(DataRow::class.java).map { row ->
            val instance = row.fillUp(AccountWithRole::class.java)
            row.fillUp(instance, rolePrefix)
            instance
        }

        Assert.assertEquals(accountWithRoles.size, 1)
        Assert.assertEquals(accountWithRoles[0].roleId, 2)
        Assert.assertTrue(accountWithRoles[0].roleName.isNotBlank())
    }

    @Test
    fun aggregateAccountWithRoles() {
        val plan = AccountTable
            .selectExpression(AccountRoleTable.accountId.count())
            .join(AccountRoleTable.accountId).selectExpression(RoleTable.id.alias())
            .join(AccountRoleTable.roleId, RoleTable.id)
            .groupBy(RoleTable.id)
            .having(Condition("${AccountRoleTable.accountId.count().text} > ?", 0))
            .debug()

        val dataRows = plan.execute(DataRow::class.java)
        Assert.assertEquals(dataRows.size, 2)
    }
}
