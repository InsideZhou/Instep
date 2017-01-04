package instep.orm.sql

import instep.Instep
import instep.orm.Plan
import instep.orm.sql.dialect.H2Dialect
import instep.servicecontainer.ServiceNotFoundException

/**
 * Abstract DAO object.
 */
abstract class Table(val tableName: String) {
    fun char(name: String, length: Int): StringColumn {
        return StringColumn(name, StringColumnType.Char, length)
    }

    fun varchar(name: String, length: Int): StringColumn {
        return StringColumn(name, StringColumnType.Varchar, length)
    }

    fun text(name: String): StringColumn {
        return StringColumn(name, StringColumnType.Text)
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
            primary = true
            autoIncrement = true
        }
    }

    fun autoIncrementLong(name: String): IntegerColumn {
        return IntegerColumn(name, IntegerColumnType.Long).apply {
            primary = true
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

    fun lob(name: String): BinaryColumn {
        return BinaryColumn(name, BinaryColumnType.BLOB)
    }

    fun columns(): List<Column<*>> {
        val mirror = Instep.reflect(this)
        return mirror.getters
            .filter { Column::class.java.isAssignableFrom(it.returnType) }
            .map { it.invoke(this) as Column<*> }
    }

    fun create(): Plan {
        val dialect = Instep.make(Dialect::class.java)
        return dialect.createTable(tableName, columns())
    }

    fun select(): TableSelectPlan {
        throw NotImplementedError()
    }

    companion object {
        init {
            try {
                Instep.make(Dialect::class.java)
            }
            catch(e: ServiceNotFoundException) {
                Instep.bind(Dialect::class.java, H2Dialect())
            }
        }
    }
}
