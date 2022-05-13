package instep.dao.sql.dialect

import instep.dao.PlaceHolder
import instep.dao.sql.*
import java.sql.Blob
import javax.sql.rowset.serial.SerialBlob


open class PostgreSQLDialect : SeparateCommentDialect() {
    class ResultSet(private val rs: java.sql.ResultSet) : AbstractDialect.ResultSet(rs) {
        override fun getBlob(columnIndex: Int): Blob? {
            val stream = rs.getBinaryStream(columnIndex) ?: return null
            return SerialBlob(stream.readBytes())
        }
    }

    override val returningClauseForInsert = "RETURNING *"
    override val offsetDateTimeSupported: Boolean = false

    override fun definitionForAutoIncrementColumn(column: IntegerColumn): String = when (column.type) {
        IntegerColumnType.Long -> "BIGSERIAL"
        else -> "SERIAL"
    }

    override val parameterForJSONType: String = "?::JSONB"
    override val parameterForUUIDType: String = "?::UUID"

    override val defaultValueForInsert = "DEFAULT"

    override fun definitionForUUIDColumn(column: StringColumn): String = "UUID"

    override fun definitionForJSONColumn(column: StringColumn): String = "JSONB"

    override fun definitionForBinaryColumn(column: BinaryColumn): String = "BYTEA"
}