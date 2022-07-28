package instep.dao.sql

import instep.ImpossibleBranch
import instep.Instep
import instep.dao.DaoException

/**
 * Abstract DAO object.
 */
@Suppress("unused", "FoldInitializerAndIfToElvis", "MemberVisibilityCanBePrivate")
abstract class Table(val tableName: String, val tableComment: String, val dialect: Dialect) {
    constructor(tableName: String, tableComment: String) : this(tableName, tableComment, Instep.make(ConnectionProvider::class.java).dialect)
    constructor(tableName: String) : this(tableName, "")

    open fun path(column: Column<*>): String = "${tableName}.${column.name}"

    /**
     * for java interop.
     */
    open fun bool(name: String): BooleanColumn {
        return boolean(name)
    }

    open fun boolean(name: String): BooleanColumn {
        return BooleanColumn(name, this)
    }

    open fun char(name: String, length: Int): StringColumn {
        return StringColumn(name, this, StringColumnType.Char, length)
    }

    /**
     * for java interop.
     */
    open fun charColumn(name: String, length: Int): StringColumn {
        return char(name, length)
    }

    open fun varchar(name: String, length: Int): StringColumn {
        return StringColumn(name, this, StringColumnType.Varchar, length)
    }

    @JvmOverloads
    open fun text(name: String, length: Int = 0): StringColumn {
        return StringColumn(name, this, StringColumnType.Text, length)
    }

    open fun uuid(name: String): StringColumn {
        return StringColumn(name, this, StringColumnType.UUID)
    }

    open fun json(name: String): StringColumn {
        return StringColumn(name, this, StringColumnType.JSON)
    }

    open fun tinyInt(name: String): IntegerColumn {
        return IntegerColumn(name, this, IntegerColumnType.Tiny)
    }

    open fun smallInt(name: String): IntegerColumn {
        return IntegerColumn(name, this, IntegerColumnType.Small)
    }

    open fun int(name: String): IntegerColumn {
        return IntegerColumn(name, this, IntegerColumnType.Int)
    }

    /**
     * for java interop.
     */
    open fun integer(name: String): IntegerColumn {
        return int(name)
    }

    open fun long(name: String): IntegerColumn {
        return IntegerColumn(name, this, IntegerColumnType.Long)
    }

    /**
     * for java interop.
     */
    open fun longColumn(name: String): IntegerColumn {
        return long(name)
    }

    open fun autoIncrement(name: String): IntegerColumn {
        return IntegerColumn(name, this, IntegerColumnType.Int).apply {
            autoIncrement = true
        }
    }

    open fun autoIncrementLong(name: String): IntegerColumn {
        return IntegerColumn(name, this, IntegerColumnType.Long).apply {
            autoIncrement = true
        }
    }

    open fun float(name: String): FloatingColumn {
        return FloatingColumn(name, this, FloatingColumnType.Float)
    }

    /**
     * for java interop.
     */
    open fun floatColumn(name: String): FloatingColumn {
        return float(name)
    }

    open fun double(name: String): FloatingColumn {
        return FloatingColumn(name, this, FloatingColumnType.Double)
    }

    /**
     * for java interop.
     */
    open fun doubleColumn(name: String): FloatingColumn {
        return double(name)
    }

    open fun numeric(name: String, precision: Int, scale: Int): FloatingColumn {
        return FloatingColumn(name, this, FloatingColumnType.Numeric, precision, scale)
    }

    open fun date(name: String): DateTimeColumn {
        return DateTimeColumn(name, this, DateTimeColumnType.Date)
    }

    open fun time(name: String): DateTimeColumn {
        return DateTimeColumn(name, this, DateTimeColumnType.Time)
    }

    open fun datetime(name: String): DateTimeColumn {
        return DateTimeColumn(name, this, DateTimeColumnType.DateTime)
    }

    open fun offsetDateTime(name: String): DateTimeColumn {
        return DateTimeColumn(name, this, DateTimeColumnType.OffsetDateTime)
    }

    open fun instant(name: String): DateTimeColumn {
        return DateTimeColumn(name, this, DateTimeColumnType.Instant)
    }

    open fun bytes(name: String, length: Int): BinaryColumn {
        return BinaryColumn(name, this, BinaryColumnType.Varying, length)
    }

    @JvmOverloads
    open fun lob(name: String, length: Int = 0): BinaryColumn {
        return BinaryColumn(name, this, BinaryColumnType.BLOB, length)
    }

    open fun arbitrary(name: String, definition: String): ArbitraryColumn {
        return ArbitraryColumn(name, this, definition)
    }

    val columns: List<Column<*>>
        get() {
            val mirror = Instep.reflect(this)

            return mirror.getPropertiesUntil(Table::class.java).filter { Column::class.java.isAssignableFrom(it.field.type) }.map {
                if (null != it.getter) {
                    it.getter!!.invoke(this)
                }
                else {
                    it.field.get(this)
                } as Column<*>
            }
        }

    val primaryKey: Column<*>?
        get() = columns.singleOrNull { it.primary }

    @JvmOverloads
    open fun create(checkExists: Boolean = true): SQLPlan<*> {
        return if (checkExists) dialect.createTableIfNotExists(tableName, tableComment, columns) else dialect.createTable(tableName, tableComment, columns)
    }

    @JvmOverloads
    open fun drop(checkExists: Boolean = true): SQLPlan<*> {
        return if (checkExists) dialect.dropTableIfExists(tableName) else dialect.dropTable(tableName)
    }

    open fun rename(name: String): SQLPlan<*> {
        return dialect.renameTable(tableName, name)
    }

    open fun addColumn(column: Column<*>): SQLPlan<*> {
        return dialect.addColumn(column)
    }

    open fun dropColumn(column: Column<*>): SQLPlan<*> {
        return dialect.dropColumn(column)
    }

    open fun renameColumn(column: Column<*>, oldName: String): SQLPlan<*> {
        return dialect.renameColumn(column, oldName)
    }

    open fun alterColumnNotNull(column: Column<*>): SQLPlan<*> {
        return dialect.alterColumnNotNull(column)
    }

    open fun alterColumnDefault(column: Column<*>): SQLPlan<*> {
        return dialect.alterColumnDefault(column)
    }

    open fun insert(): TableInsertPlan {
        val factory = Instep.make(TableInsertPlanFactory::class.java)
        return factory.createInstance(this)
    }

    open fun select(vararg columns: Column<*>): TableSelectPlan {
        val factory = Instep.make(TableSelectPlanFactory::class.java)
        return factory.createInstance(this).select(*columns)
    }

    open fun selectExpression(vararg columnExpressions: ColumnExpression): TableSelectPlan {
        val factory = Instep.make(TableSelectPlanFactory::class.java)
        return factory.createInstance(this).selectExpression(*columnExpressions)
    }

    open fun update(): TableUpdatePlan {
        val factory = Instep.make(TableUpdatePlanFactory::class.java)
        return factory.createInstance(this)
    }

    open fun delete(): TableDeletePlan {
        val factory = Instep.make(TableDeletePlanFactory::class.java)
        return factory.createInstance(this)
    }

    @Throws(SQLPlanExecutionException::class)
    open fun <T : Any> get(key: Number, cls: Class<T>): T? {
        if (null == primaryKey) throw DaoException("Table $tableName should has primary key")

        val pk = primaryKey as IntegerColumn
        return select().where(pk eq key).execute(cls).singleOrNull()
    }

    @Throws(SQLPlanExecutionException::class)
    open fun <T : Any> get(key: String, cls: Class<T>): T? {
        if (null == primaryKey) throw DaoException("Table $tableName should has primary key")

        val pk = primaryKey as StringColumn
        return select().where(pk eq key).execute(cls).singleOrNull()
    }

    @Throws(SQLPlanExecutionException::class)
    open operator fun get(key: Number): TableRow? {
        if (null == primaryKey) throw DaoException("Table $tableName should has primary key")

        val pk = primaryKey as IntegerColumn
        return select().where(pk eq key).execute().singleOrNull()
    }

    @Throws(SQLPlanExecutionException::class)
    open operator fun get(key: String): TableRow? {
        if (null == primaryKey) throw DaoException("Table $tableName should has primary key")

        val pk = primaryKey as StringColumn
        return select().where(pk eq key).execute().singleOrNull()
    }

    @Throws(SQLPlanExecutionException::class)
    open operator fun set(key: Number, obj: Any) {
        insertOrUpdate(key, obj)
    }

    @Throws(SQLPlanExecutionException::class)
    open operator fun set(key: String, obj: Any) {
        insertOrUpdate(key, obj)
    }

    private fun insertOrUpdate(key: Any, obj: Any) {
        val pk = primaryKey
        if (null == pk) throw DaoException("Table $tableName should has primary key")

        when (key) {
            is Number -> {
                val existsRow = this[key]
                if (null != existsRow) {
                    val plan = update()

                    when (obj) {
                        is TableRow -> {
                            columns.filterNot { it == pk }.forEach {
                                plan.set(it, obj[it])
                            }
                            plan.where(pk as NumberColumn eq key).execute()
                        }

                        else -> plan.set(obj).whereKey(key).execute()
                    }

                    return
                }
            }

            is String -> {
                val existsRow = this[key]
                if (null != existsRow) {
                    val plan = update()

                    when (obj) {
                        is TableRow -> {
                            columns.filterNot { it == pk }.forEach {
                                plan.set(it, obj[it])
                            }
                            plan.where(pk as StringColumn eq key).execute()
                        }

                        else -> plan.set(obj).whereKey(key).execute()
                    }

                    return
                }
            }

            else -> throw ImpossibleBranch()
        }

        val plan = insert()

        when (obj) {
            is TableRow -> {
                columns.filterNot { it == pk }.forEach {
                    plan.addValue(it, obj[it])
                }

                plan.addValue(pk, key)

                plan.execute()
            }

            else -> plan.set(obj).execute()
        }
    }

    companion object {
        val DefaultInsertValue = object {}
    }
}
