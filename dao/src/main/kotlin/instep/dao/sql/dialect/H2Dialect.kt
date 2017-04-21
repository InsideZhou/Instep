package instep.dao.sql.dialect

import instep.dao.DaoException
import instep.dao.sql.DateTimeColumn
import instep.dao.sql.DateTimeColumnType
import instep.dao.sql.IntegerColumn

open class H2Dialect : AbstractDialect() {
    override val isOffsetDateTimeSupported: Boolean = false

    override fun definitionForAutoIncrementColumn(column: IntegerColumn): String = "IDENTITY"

    override fun definitionForDateTimeColumn(column: DateTimeColumn): String {
        if (column.type == DateTimeColumnType.OffsetDateTime) throw DaoException("DateTimeColumn.OffsetDateTime is not support")

        return super.definitionForDateTimeColumn(column);
    }
}