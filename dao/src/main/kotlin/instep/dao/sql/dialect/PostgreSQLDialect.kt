package instep.dao.sql.dialect

import instep.dao.sql.BinaryColumn
import instep.dao.sql.IntegerColumn
import instep.dao.sql.IntegerColumnType


open class PostgreSQLDialect : AbstractDialect() {
    override val isOffsetDateTimeSupported: Boolean = false

    override fun definitionForAutoIncrementColumn(column: IntegerColumn): String = when (column.type) {
        IntegerColumnType.Long -> "BIGSERIAL"
        else -> "SERIAL"
    }

    override fun definitionForBinaryColumn(column: BinaryColumn): String = "BYTEA"
}