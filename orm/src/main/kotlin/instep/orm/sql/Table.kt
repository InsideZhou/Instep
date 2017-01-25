package instep.orm.sql

import instep.Instep
import instep.orm.OrmException
import instep.orm.Plan
import instep.orm.sql.dialect.H2Dialect
import instep.servicecontainer.ServiceNotFoundException

/**
 * Abstract DAO object.
 */
abstract class Table(val tableName: String) {
    fun char(name: String, length: Int): StringColumn {
        return StringColumn(this, name, StringColumnType.Char, length)
    }

    fun varchar(name: String, length: Int): StringColumn {
        return StringColumn(this, name, StringColumnType.Varchar, length)
    }

    fun text(name: String): StringColumn {
        return StringColumn(this, name, StringColumnType.Text)
    }

    fun tinyInt(name: String): IntegerColumn {
        return IntegerColumn(this, name, IntegerColumnType.Tiny)
    }

    fun smallInt(name: String): IntegerColumn {
        return IntegerColumn(this, name, IntegerColumnType.Small)
    }

    fun int(name: String): IntegerColumn {
        return IntegerColumn(this, name, IntegerColumnType.Int)
    }

    fun long(name: String): IntegerColumn {
        return IntegerColumn(this, name, IntegerColumnType.Long)
    }

    fun autoIncrement(name: String): IntegerColumn {
        return IntegerColumn(this, name, IntegerColumnType.Int).apply {
            primary = true
            autoIncrement = true
        }
    }

    fun autoIncrementLong(name: String): IntegerColumn {
        return IntegerColumn(this, name, IntegerColumnType.Long).apply {
            primary = true
            autoIncrement = true
        }
    }

    fun float(name: String): FloatingColumn {
        return FloatingColumn(this, name, FloatingColumnType.Float)
    }

    fun double(name: String): FloatingColumn {
        return FloatingColumn(this, name, FloatingColumnType.Double)
    }

    fun numeric(name: String, precision: Int, scale: Int): FloatingColumn {
        return FloatingColumn(this, name, FloatingColumnType.Numeric, precision, scale)
    }

    fun date(name: String): DateTimeColumn {
        return DateTimeColumn(this, name, DateTimeColumnType.Date)
    }

    fun time(name: String): DateTimeColumn {
        return DateTimeColumn(this, name, DateTimeColumnType.Time)
    }

    fun datetime(name: String): DateTimeColumn {
        return DateTimeColumn(this, name, DateTimeColumnType.DateTime)
    }

    fun offsetDateTime(name: String): DateTimeColumn {
        return DateTimeColumn(this, name, DateTimeColumnType.OffsetDateTime)
    }

    fun bytes(name: String, length: Int): BinaryColumn {
        return BinaryColumn(this, name, BinaryColumnType.Varying, length)
    }

    fun lob(name: String): BinaryColumn {
        return BinaryColumn(this, name, BinaryColumnType.BLOB)
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

    fun select(vararg columnOrAggregates: Any): TableSelectPlan {
        val factory = Instep.make(TableSelectPlan.Companion::class.java)
        return factory.createInstance(this).select(*columnOrAggregates)
    }

    operator fun get(key: Number): TableRow? {
        if (null == primaryKey) throw OrmException("Table $tableName should has primary key")

        val pk = primaryKey as IntegerColumn
        return select().where(pk eq key).execute().singleOrNull()
    }

    operator fun get(key: String): TableRow? {
        if (null == primaryKey) throw OrmException("Table $tableName should has primary key")

        val pk = primaryKey as StringColumn
        return select().where(pk eq key).execute().singleOrNull()
    }

    fun insert(): TableInsertPlan {
        val factory = Instep.make(TableInsertPlan.Companion::class.java)
        return factory.createInstance(this)
    }

    fun update(): TableUpdatePlan {
        val factory = Instep.make(TableUpdatePlan.Companion::class.java)
        return factory.createInstance(this)
    }
    //TODO use set operator to insert&update

    companion object {
        init {
            try {
                Instep.make(Dialect::class.java)
            }
            catch(e: ServiceNotFoundException) {
                Instep.bind(Dialect::class.java, H2Dialect())
            }

            try {
                Instep.make(TableRow.Companion::class.java)
            }
            catch(e: ServiceNotFoundException) {
                Instep.bind(TableRow.Companion::class.java, TableRow.Companion)
            }

            try {
                Instep.make(TableSelectPlan.Companion::class.java)
            }
            catch(e: ServiceNotFoundException) {
                Instep.bind(TableSelectPlan.Companion::class.java, TableSelectPlan.Companion)
            }

            try {
                Instep.make(TableInsertPlan.Companion::class.java)
            }
            catch(e: ServiceNotFoundException) {
                Instep.bind(TableInsertPlan.Companion::class.java, TableInsertPlan.Companion)
            }

            try {
                Instep.make(TableUpdatePlan.Companion::class.java)
            }
            catch(e: ServiceNotFoundException) {
                Instep.bind(TableUpdatePlan.Companion::class.java, TableUpdatePlan.Companion)
            }
        }
    }
}
