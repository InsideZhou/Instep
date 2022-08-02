@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package instep.dao.sql

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import instep.Instep
import instep.dao.sql.dialect.MySQLDialect
import instep.dao.sql.dialect.PostgreSQLDialect
import instep.dao.sql.dialect.SQLServerDialect
import instep.typeconversion.Converter
import instep.typeconversion.ConverterEligible
import instep.typeconversion.TypeConversion
import instep.util.path
import org.postgresql.jdbc.PgArray
import org.postgresql.util.PGobject
import org.testng.Assert
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.time.*
import java.util.*
import kotlin.reflect.jvm.javaField

@Suppress("UNCHECKED_CAST")
object TableTest {
    val stringGenerator = net.moznion.random.string.RandomStringGenerator()
    val birthDate: LocalDate = LocalDate.of(1993, 6, 6)
    val birthTime: LocalTime = LocalTime.of(6, 6)
    val birthday: OffsetDateTime = OffsetDateTime.of(birthDate, birthTime, ZoneOffset.UTC)
    val objectMapper = ObjectMapper()

    init {
        InstepSQLTest
        objectMapper.registerModule(JavaTimeModule())

        kotlin.runCatching { Instep.make(TypeConversion::class.java) }.onSuccess {
            it.register(object : Converter<UUID, String> {
                override val from = UUID::class.java
                override val to = String::class.java

                override fun <T : UUID> convert(instance: T): String {
                    return instance.toString()
                }
            })

            it.register(object : Converter<PGobject, Map<String, Any?>> {
                override val from = PGobject::class.java
                override val to = Map::class.java as Class<Map<String, Any?>>

                override fun <T : PGobject> convert(instance: T): Map<String, Any?> {
                    return objectMapper.readValue(instance.value, object : TypeReference<Map<String, Any?>>() {})
                }
            }, Account::class.java.path(Account::preferences.javaField!!))

            it.register(object : Converter<Map<String, Any?>, String> {
                override val from = Map::class.java as Class<Map<String, Any?>>
                override val to = String::class.java

                override fun <T : Map<String, Any?>> convert(instance: T): String {
                    return objectMapper.writeValueAsString(instance)
                }
            }, AccountTable.preferences.qualifiedName)

            it.register(object : Converter<PgArray, List<String>> {
                override val from = PgArray::class.java
                override val to = List::class.java as Class<List<String>>

                override fun <T : PgArray> convert(instance: T): List<String> {
                    return (instance.array as? Array<String>)?.let { array ->
                        arrayListOf(*array)
                    } ?: arrayListOf()
                }
            }, Account::class.java.path(Account::tags.javaField!!))

            it.register(object : Converter<List<String>, String> {
                override val from = List::class.java as Class<List<String>>
                override val to = String::class.java

                override fun <T : List<String>> convert(instance: T): String {
                    return instance.joinToString(separator = ",", prefix = "'{", postfix = "}'::text[]") { txt ->
                        "\"" + txt + "\""
                    }
                }
            }, AccountTable.tags.qualifiedName)

            it.register(object : Converter<PGobject, List<AccountLog>> {
                override val from = PGobject::class.java
                override val to = List::class.java as Class<List<AccountLog>>

                override fun <T : PGobject> convert(instance: T): List<AccountLog> {
                    return objectMapper.readValue(instance.value, object : TypeReference<List<AccountLog>>() {})
                }
            }, Account::class.java.path(Account::logs.javaField!!))

            it.register(object : Converter<List<AccountLog>, String> {
                override val from = List::class.java as Class<List<AccountLog>>
                override val to = String::class.java

                override fun <T : List<AccountLog>> convert(instance: T): String {
                    return "'${objectMapper.writeValueAsString(instance)}'::jsonb"
                }
            }, AccountTable.logs.qualifiedName)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    enum class AccountType(code: String, desc: String) {
        Admin("admin", "管理员"), User("user", "用户");
    }

    abstract class Model {
        var id = UUID.randomUUID().toString()
    }

    class Account : Model() {
        var type = AccountType.User
        var name = ""
        var balance: java.math.BigDecimal = java.math.BigDecimal.ZERO
        var createdAt: Instant = Instant.now()
        var birthDate: LocalDate? = null
        var birthTime: LocalTime? = null
        var avatar = byteArrayOf()
        var remark: String? = null

        var preferences = emptyMap<String, Any?>()
            @ConverterEligible(String::class)
            get
            @ConverterEligible(PGobject::class)
            set

        var tags = emptyList<String>()
            @ConverterEligible(String::class)
            get
            @ConverterEligible(PgArray::class)
            set

        var logs = emptyList<AccountLog>()
            @ConverterEligible(String::class)
            get
            @ConverterEligible(PGobject::class)
            set
    }

    data class AccountLog(var recordTime: Instant, var description: String) {
        constructor() : this(Instant.now(), "")
    }

    abstract class AbstractTable(tableName: String, tableComment: String, dialect: Dialect) : Table(tableName, tableComment, dialect) {
        constructor(tableName: String, tableComment: String) : this(tableName, tableComment, Instep.make(ConnectionProvider::class.java).dialect)
        constructor(tableName: String) : this(tableName, "")

        val id = this.uuid("id").primary()
    }

    object AccountTable : AbstractTable("account_" + stringGenerator.generateByRegex("[a-z]{8}"), "账号表") {
        val type = varchar("type", 16).notnull().comment("账号类型")
        val code = varchar("code", 16).unique()
        val name = varchar("name", 256).notnull()
        val balance = when (dialect) {
            is MySQLDialect -> numeric("balance", 65, 2).notnull()
            is PostgreSQLDialect -> numeric("balance", 1000, 2).notnull()
            is SQLServerDialect -> numeric("balance", 38, 2).notnull()
            else -> numeric("balance", Int.MAX_VALUE, 2).notnull()
        }
        val createdAt = instant("created_at").notnull()
        var birthDate = date("birth_date")
        var birthTime = time("birth_time")
        var birthday = if (dialect.offsetDateTimeSupported) {
            offsetDateTime("birthday")
        }
        else {
            datetime("birthday")
        }
        val avatar = lob("avatar")
        val remark = varchar("remark", 512)

        val preferences = json("preferences").defaultValue("'{}'::jsonb")
        val tags = arbitrary("tags", "text[]").defaultValue("'{}'::text[]")
        val logs = arbitrary("logs", "jsonb").defaultValue("'[]'::jsonb")
    }

    @BeforeClass
    fun init() {
        AccountTable.create().debug().execute()
    }

    @AfterClass()
    fun cleanUp() {
        AccountTable.drop().execute()
    }

    @Test
    fun addColumn() {
        AccountTable.addColumn(AccountTable.boolean("verified").defaultValue("false")).debug().execute()
    }

    @Test
    fun insertAccounts() {
        val random = Random()
        val total = random.ints(10, 100).findAny().orElse(100)

        for (index in 0..total) {
            AccountTable.insert()
                .addValue(AccountTable.id, UUID.randomUUID().toString())
                .addValue(AccountTable.type, AccountType.Admin)
                .addValue(AccountTable.name, stringGenerator.generateByRegex("\\w{1,256}"))
                .addValue(AccountTable.code, stringGenerator.generateByRegex("\\w{6}"))
                .addValue(AccountTable.balance, random.nextDouble())
                .addValue(AccountTable.createdAt, Instant.now())
                .addValue(AccountTable.birthDate, birthDate)
                .addValue(AccountTable.birthTime, birthTime)
                .addValue(AccountTable.birthday, birthday)
                .addValue(AccountTable.preferences, """{"a":1,"b":2}""")
                .addValue(AccountTable.tags, """'{"a","b","c"}'::text[]""")
                .addValue(AccountTable.logs, """'[{"recordTime":"${Instant.now()}","description":"你根本不是司机"}]'::jsonb""")
                .returning()
                .debug()
                .execute()
        }
    }

    @Test(dependsOnMethods = ["insertAccounts"])
    fun maxAccountId() {
        val latest = AccountTable.selectExpression(AccountTable.createdAt.max()).executeScalar(Instant::class.java)
        AccountTable.select(AccountTable.id).where(AccountTable.createdAt eq latest!!).executeScalar()
    }

    @Test(dependsOnMethods = ["insertAccounts"])
    fun updateAccounts() {
        val latest = AccountTable.selectExpression(AccountTable.createdAt.max()).executeScalar(Instant::class.java)
        val id = AccountTable.select(AccountTable.id).where(AccountTable.createdAt eq latest!!).executeScalar()

        AccountTable.update()
            .set(AccountTable.name, "laozi")
            .set(AccountTable.balance, 3.33)
            .whereKey(id)
            .debug()
            .executeUpdate()

        var laozi = AccountTable[id]!!
        assert(laozi[AccountTable.name] == "laozi")
        assert(laozi[AccountTable.balance] == 3.33)

        AccountTable.update()
            .set(AccountTable.name, "dao de jing")
            .set(AccountTable.balance, 6.66)
            .where(AccountTable.name eq "laozi", AccountTable.balance lte 3.33)
            .executeUpdate()

        laozi = AccountTable.select().where(AccountTable.id eq id).execute().single()
        assert(laozi[AccountTable.name] == "dao de jing")
        assert(laozi[AccountTable.balance] == 6.66)

        AccountTable.update()
            .step(AccountTable.balance, 2.22)
            .whereKey(id)
            .executeUpdate()

        laozi = AccountTable.select().where(AccountTable.id eq id).execute().single()
        assert(laozi[AccountTable.name] == "dao de jing")
        assert(laozi[AccountTable.balance] == 8.88)

        AccountTable.update()
            .step(AccountTable.balance, -1)
            .whereKey(id)
            .executeUpdate()

        laozi = AccountTable.select().where(AccountTable.id eq id).execute().single()
        assert(laozi[AccountTable.balance] == 7.88)

        val zhuangzi = AccountTable.select().where(AccountTable.id eq id).execute(Account::class.java).single()
        zhuangzi.name = "zhuangzi"
        AccountTable.update().set(zhuangzi).debug().executeUpdate()

        laozi = AccountTable.select().where(AccountTable.id eq id).execute().single()
        assert(laozi[AccountTable.name] == zhuangzi.name)
    }

    @Test(dependsOnMethods = ["insertAccounts"])
    fun deleteAccounts() {
        val latest = AccountTable.selectExpression(AccountTable.createdAt.max()).executeScalar(Instant::class.java)
        val id = AccountTable.select(AccountTable.id).where(AccountTable.createdAt eq latest!!).executeScalar()

        AccountTable.delete().where(AccountTable.id eq id).debug().executeUpdate()
        assert(null == AccountTable[id])
    }

    @Test(dependsOnMethods = ["insertAccounts"])
    fun datetime() {
        val latest = AccountTable.selectExpression(AccountTable.createdAt.max()).executeScalar(Instant::class.java)
        val id = AccountTable.select(AccountTable.id).where(AccountTable.createdAt eq latest!!).executeScalar()

        val account = AccountTable.select().where(AccountTable.id eq id).execute().single()

        Assert.assertThrows(UnsupportedOperationException::class.java) { account[AccountTable.birthDate] }
        Assert.assertThrows(UnsupportedOperationException::class.java) { account[AccountTable.birthTime] }

        assert(account.getLocalDate(AccountTable.birthDate) == birthDate)
        assert(account.getLocalTime(AccountTable.birthTime) == birthTime)

        if (AccountTable.dialect.offsetDateTimeSupported) {
            assert(account.getOffsetDateTime(AccountTable.birthday) == OffsetDateTime.of(birthDate, birthTime, ZoneOffset.UTC))
        }
        else {
            assert(account.getOffsetDateTime(AccountTable.birthday) == OffsetDateTime.of(birthDate, birthTime, OffsetDateTime.now().offset))
        }

        Assert.assertThrows(UnsupportedOperationException::class.java) { account.getLocalDateTime(AccountTable.birthDate) }
        Assert.assertThrows(UnsupportedOperationException::class.java) { account.getLocalDateTime(AccountTable.birthTime) }
    }

    @Test(dependsOnMethods = ["insertAccounts"])
    fun rowToInstance() {
        val latest = AccountTable.selectExpression(AccountTable.createdAt.max()).executeScalar(Instant::class.java)
        val id = AccountTable.select(AccountTable.id).where(AccountTable.createdAt eq latest!!).executeScalar()

        val account = AccountTable.select().where(AccountTable.id eq id).execute(Account::class.java).single()
        assert(account.id == id)
        Assert.assertEquals(account.birthDate, birthDate)
        assert(account.birthTime == birthTime)
        assert(account.remark == null)
    }

    @Test(dependsOnMethods = ["insertAccounts"])
    fun randomRow() {
        val latest = AccountTable.selectExpression(AccountTable.createdAt.max()).executeScalar(Instant::class.java)
        val idMax = AccountTable.select(AccountTable.id).where(AccountTable.createdAt eq latest!!).executeScalar()

        val oldest = AccountTable.selectExpression(AccountTable.createdAt.max()).executeScalar(Instant::class.java)
        val idMin = AccountTable.select(AccountTable.id).where(AccountTable.createdAt eq oldest!!).executeScalar()

        val idArray = setOf(idMax, idMin).toTypedArray()

        val plan = AccountTable.selectExpression(AccountTable.id.count()).where(AccountTable.id inArray idArray)

        assert(plan.executeScalar() == idArray.size.toString())
    }
}
