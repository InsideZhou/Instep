package instep.dao.sql

import instep.Instep
import instep.UnexpectedCodeError
import instep.dao.DaoException
import instep.dao.Plan
import instep.dao.sql.dialect.H2Dialect
import instep.dao.sql.dialect.HSQLDialect
import instep.servicecontainer.ServiceNotFoundException

/**
 * Abstract DAO object.
 */
abstract class Table(val tableName: String) {
    fun boolean(name: String): BooleanColumn {
        return BooleanColumn(name)
    }

    fun char(name: String, length: Int): StringColumn {
        return StringColumn(name, StringColumnType.Char, length)
    }

    fun varchar(name: String, length: Int): StringColumn {
        return StringColumn(name, StringColumnType.Varchar, length)
    }

    fun text(name: String, length: Int = 0): StringColumn {
        return StringColumn(name, StringColumnType.Text, length)
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

    fun long(name: String): IntegerColumn {
        return IntegerColumn(name, IntegerColumnType.Long)
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

    fun double(name: String): FloatingColumn {
        return FloatingColumn(name, FloatingColumnType.Double)
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

    fun lob(name: String, length: Int = 0): BinaryColumn {
        return BinaryColumn(name, BinaryColumnType.BLOB, length)
    }

    val columns: List<Column<*>>
        get() {
            val mirror = Instep.reflect(this)
            return mirror.getters
                .filter { Column::class.java.isAssignableFrom(it.returnType) }
                .map { it.invoke(this) as Column<*> }
        }

    val primaryKey: Column<*>?
        get() = columns.singleOrNull { it.primary }

    fun create(): Plan<*> {
        val dialect = Instep.make(Dialect::class.java)
        return dialect.createTable(tableName, columns)
    }

    fun addColumn(column: Column<*>): Plan<*> {
        val dialect = Instep.make(Dialect::class.java)
        return dialect.addColumn(tableName, column)
    }

    fun insert(): TableInsertPlan {
        val factory = Instep.make(TableInsertPlanFactory::class.java)
        return factory.createInstance(this)
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

    operator fun get(key: Number): TableRow? {
        if (null == primaryKey) throw DaoException("Table $tableName should has primary key")

        val pk = primaryKey as IntegerColumn
        return select().where(pk eq key).execute().singleOrNull()
    }

    operator fun get(key: String): TableRow? {
        if (null == primaryKey) throw DaoException("Table $tableName should has primary key")

        val pk = primaryKey as StringColumn
        return select().where(pk eq key).execute().singleOrNull()
    }

    operator fun set(key: Number, row: TableRow) {
        insertOrUpdate(key, row)
    }

    operator fun set(key: String, row: TableRow) {
        insertOrUpdate(key, row)
    }

    private fun insertOrUpdate(key: Any, row: TableRow) {
        val pk = primaryKey
        if (null == pk) throw DaoException("Table $tableName should has primary key")

        when (key) {
            is Number -> {
                val existsRow = this[key]
                if (null != existsRow) {
                    val plan = update()
                    columns.forEach {
                        plan.set(it, row[it])
                    }
                    plan.where(pk as NumberColumn eq key).execute()
                    return
                }
            }
            is String -> {
                val existsRow = this[key]
                if (null != existsRow) {
                    val plan = update()
                    columns.forEach {
                        plan.set(it, row[it])
                    }
                    plan.where(pk as StringColumn eq key).execute()
                    return
                }
            }
            else -> throw UnexpectedCodeError()
        }

        val plan = insert()
        columns.forEach {
            plan.addValue(it, row[it])
        }

        if (null == row[pk]) {
            plan.addValue(pk, key)
        }

        plan.execute()
    }

    companion object {
        init {
            try {
                Instep.make(TableSelectPlanFactory::class.java)
            }
            catch(e: ServiceNotFoundException) {
                Instep.bind(TableSelectPlanFactory::class.java, TableSelectPlan.Companion)
            }

            try {
                Instep.make(TableInsertPlanFactory::class.java)
            }
            catch(e: ServiceNotFoundException) {
                Instep.bind(TableInsertPlanFactory::class.java, TableInsertPlan.Companion)
            }

            try {
                Instep.make(TableUpdatePlanFactory::class.java)
            }
            catch(e: ServiceNotFoundException) {
                Instep.bind(TableUpdatePlanFactory::class.java, TableUpdatePlan.Companion)
            }

            try {
                Instep.make(TableDeletePlanFactory::class.java)
            }
            catch(e: ServiceNotFoundException) {
                Instep.bind(TableDeletePlanFactory::class.java, TableDeletePlan.Companion)
            }
        }

        /**
         * Infer dialect from datasource url and set it up.
         */
        fun setupDialect(url: String) {
            try {
                Instep.make(Dialect::class.java)
            }
            catch(e: ServiceNotFoundException) {
                if (url.contains("hsqldb", true)) {
                    Instep.bind(Dialect::class.java, HSQLDialect())
                }
                else if (url.contains("h2", true)) {
                    Instep.bind(Dialect::class.java, H2Dialect())
                }
                else {
                    throw DaoException("cannot infer dialect for datasource $url")
                }
            }
        }
    }
}
