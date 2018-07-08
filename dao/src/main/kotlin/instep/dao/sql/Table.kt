package instep.dao.sql

import instep.Instep
import instep.UnexpectedCodeError
import instep.dao.DaoException
import instep.dao.Plan
import instep.servicecontainer.ServiceNotFoundException
import java.lang.reflect.Modifier

/**
 * Abstract DAO object.
 */
@Suppress("unused", "FoldInitializerAndIfToElvis")
abstract class Table(val tableName: String, val dialect: Dialect) {
    constructor(tableName: String) : this(tableName, Instep.make(Dialect::class.java))

    /**
     * for java interop.
     */
    fun bool(name: String): BooleanColumn {
        return boolean(name)
    }

    fun boolean(name: String): BooleanColumn {
        return BooleanColumn(name)
    }

    fun char(name: String, length: Int): StringColumn {
        return StringColumn(name, StringColumnType.Char, length)
    }

    /**
     * for java interop.
     */
    fun charColumn(name: String, length: Int): StringColumn {
        return char(name, length)
    }

    fun varchar(name: String, length: Int): StringColumn {
        return StringColumn(name, StringColumnType.Varchar, length)
    }

    @JvmOverloads
    fun text(name: String, length: Int = 0): StringColumn {
        return StringColumn(name, StringColumnType.Text, length)
    }

    fun uuid(name: String): StringColumn {
        return StringColumn(name, StringColumnType.UUID)
    }

    fun json(name: String): StringColumn {
        return StringColumn(name, StringColumnType.JSON)
    }

    fun tinyInt(name: String): IntegerColumn {
        return IntegerColumn(name, IntegerColumnType.Tiny)
    }

    fun smallInt(name: String): IntegerColumn {
        return IntegerColumn(name, IntegerColumnType.Small)
    }

    fun int(name: String): IntegerColumn {
        return IntegerColumn(name, IntegerColumnType.Int)
    }

    /**
     * for java interop.
     */
    fun integer(name: String): IntegerColumn {
        return int(name)
    }

    fun long(name: String): IntegerColumn {
        return IntegerColumn(name, IntegerColumnType.Long)
    }

    /**
     * for java interop.
     */
    fun longColumn(name: String): IntegerColumn {
        return long(name)
    }

    fun autoIncrement(name: String): IntegerColumn {
        return IntegerColumn(name, IntegerColumnType.Int).apply {
            autoIncrement = true
        }
    }

    fun autoIncrementLong(name: String): IntegerColumn {
        return IntegerColumn(name, IntegerColumnType.Long).apply {
            autoIncrement = true
        }
    }

    fun float(name: String): FloatingColumn {
        return FloatingColumn(name, FloatingColumnType.Float)
    }

    /**
     * for java interop.
     */
    fun floatColumn(name: String): FloatingColumn {
        return float(name)
    }

    fun double(name: String): FloatingColumn {
        return FloatingColumn(name, FloatingColumnType.Double)
    }

    /**
     * for java interop.
     */
    fun doubleColumn(name: String): FloatingColumn {
        return double(name)
    }

    fun numeric(name: String, precision: Int, scale: Int): FloatingColumn {
        return FloatingColumn(name, FloatingColumnType.Numeric, precision, scale)
    }

    fun date(name: String): DateTimeColumn {
        return DateTimeColumn(name, DateTimeColumnType.Date)
    }

    fun time(name: String): DateTimeColumn {
        return DateTimeColumn(name, DateTimeColumnType.Time)
    }

    fun datetime(name: String): DateTimeColumn {
        return DateTimeColumn(name, DateTimeColumnType.DateTime)
    }

    fun offsetDateTime(name: String): DateTimeColumn {
        return DateTimeColumn(name, DateTimeColumnType.OffsetDateTime)
    }

    fun bytes(name: String, length: Int): BinaryColumn {
        return BinaryColumn(name, BinaryColumnType.Varying, length)
    }

    @JvmOverloads
    fun lob(name: String, length: Int = 0): BinaryColumn {
        return BinaryColumn(name, BinaryColumnType.BLOB, length)
    }

    val columns: List<Column<*>>
        get() {
            val mirror = Instep.reflect(this)
            val getterColumns = mirror.getters
                .filter { Column::class.java.isAssignableFrom(it.returnType) }
                .map { it.invoke(this) as Column<*> }
                .toSet()

            val fieldColumns = this.javaClass.fields
                .filter { Modifier.isPublic(it.modifiers) && Column::class.java.isAssignableFrom(it.type) }
                .map { it.get(this) as Column<*> }
                .toSet()

            return (getterColumns + fieldColumns).toList()
        }

    val primaryKey: Column<*>?
        get() = columns.singleOrNull { it.primary }

    fun create(): Plan<*> {
        return dialect.createTable(tableName, columns)
    }

    fun addColumn(column: Column<*>): Plan<*> {
        return dialect.addColumn(tableName, column)
    }

    fun insert(): TableInsertPlan {
        val factory = Instep.make(TableInsertPlanFactory::class.java)
        return factory.createInstance(this, dialect)
    }

    fun select(vararg columnOrAggregates: Any): TableSelectPlan {
        val factory = Instep.make(TableSelectPlanFactory::class.java)
        return factory.createInstance(this).select(*columnOrAggregates)
    }

    fun update(): TableUpdatePlan {
        val factory = Instep.make(TableUpdatePlanFactory::class.java)
        return factory.createInstance(this)
    }

    fun delete(): TableDeletePlan {
        val factory = Instep.make(TableDeletePlanFactory::class.java)
        return factory.createInstance(this)
    }

    @Throws(SQLPlanExecutionException::class)
    fun <T : Any> get(key: Number, cls: Class<T>): T? {
        if (null == primaryKey) throw DaoException("Table $tableName should has primary key")

        val pk = primaryKey as IntegerColumn
        return select().where(pk eq key).execute(cls).singleOrNull()
    }

    @Throws(SQLPlanExecutionException::class)
    fun <T : Any> get(key: String, cls: Class<T>): T? {
        if (null == primaryKey) throw DaoException("Table $tableName should has primary key")

        val pk = primaryKey as StringColumn
        return select().where(pk eq key).execute(cls).singleOrNull()
    }

    @Throws(SQLPlanExecutionException::class)
    operator fun get(key: Number): TableRow? {
        if (null == primaryKey) throw DaoException("Table $tableName should has primary key")

        val pk = primaryKey as IntegerColumn
        return select().where(pk eq key).execute().singleOrNull()
    }

    @Throws(SQLPlanExecutionException::class)
    operator fun get(key: String): TableRow? {
        if (null == primaryKey) throw DaoException("Table $tableName should has primary key")

        val pk = primaryKey as StringColumn
        return select().where(pk eq key).execute().singleOrNull()
    }

    @Throws(SQLPlanExecutionException::class)
    operator fun set(key: Number, obj: Any) {
        insertOrUpdate(key, obj)
    }

    @Throws(SQLPlanExecutionException::class)
    operator fun set(key: String, obj: Any) {
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
                        else -> plan.set(obj).where(key).execute()
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
                        else -> plan.set(obj).where(key).execute()
                    }

                    return
                }
            }
            else -> throw UnexpectedCodeError()
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

        init {
            try {
                Instep.make(TableRowFactory::class.java)
            }
            catch (e: ServiceNotFoundException) {
                Instep.bind(TableRowFactory::class.java, TableRowFactory.Companion)
            }

            try {
                Instep.make(TableSelectPlanFactory::class.java)
            }
            catch (e: ServiceNotFoundException) {
                Instep.bind(TableSelectPlanFactory::class.java, TableSelectPlanFactory.Companion)
            }

            try {
                Instep.make(TableInsertPlanFactory::class.java)
            }
            catch (e: ServiceNotFoundException) {
                Instep.bind(TableInsertPlanFactory::class.java, TableInsertPlanFactory.Companion)
            }

            try {
                Instep.make(TableUpdatePlanFactory::class.java)
            }
            catch (e: ServiceNotFoundException) {
                Instep.bind(TableUpdatePlanFactory::class.java, TableUpdatePlanFactory.Companion)
            }

            try {
                Instep.make(TableDeletePlanFactory::class.java)
            }
            catch (e: ServiceNotFoundException) {
                Instep.bind(TableDeletePlanFactory::class.java, TableDeletePlanFactory.Companion)
            }
        }
    }
}
