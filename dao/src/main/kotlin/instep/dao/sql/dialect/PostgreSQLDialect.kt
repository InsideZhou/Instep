package instep.dao.sql.dialect

import instep.dao.sql.BinaryColumn
import instep.dao.sql.IntegerColumn
import instep.dao.sql.IntegerColumnType
import instep.dao.sql.StringColumn


open class PostgreSQLDialect : SeparateCommentDialect() {
    override val returningClauseForInsert: Boolean = true
    override val offsetDateTimeSupported: Boolean = false

    override fun definitionForAutoIncrementColumn(column: IntegerColumn): String = when (column.type) {
        IntegerColumnType.Long -> "BIGSERIAL"
        else -> "SERIAL"
    }

    override val placeholderForJSONType: String = "?::JSONB"
    override val placeholderForUUIDType: String = "?::UUID"

    override val defaultValueForInsert = "DEFAULT"

    override fun definitionForUUIDColumn(column: StringColumn): String = "UUID"

    override fun definitionForJSONColumn(column: StringColumn): String = "JSONB"

    override fun definitionForBinaryColumn(column: BinaryColumn): String = "BYTEA"
}