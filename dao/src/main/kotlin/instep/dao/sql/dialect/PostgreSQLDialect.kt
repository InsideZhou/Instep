package instep.dao.sql.dialect

import instep.Instep
import instep.dao.sql.*
import instep.typeconversion.TypeConversion
import org.postgresql.util.PGobject
import java.sql.Blob
import java.sql.PreparedStatement
import javax.sql.rowset.serial.SerialBlob


open class PostgreSQLDialect : SeparateCommentDialect() {
    val typeconvert = Instep.make(TypeConversion::class.java)

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

    override val defaultValueForInsert = "DEFAULT"

    override fun definitionForUUIDColumn(column: StringColumn): String = "UUID"

    override fun definitionForJSONColumn(column: StringColumn): String = "JSONB"

    override fun definitionForBinaryColumn(column: BinaryColumn): String = "BYTEA"

    override fun setParameterForPreparedStatement(stmt: PreparedStatement, index: Int, value: Any?) {
        value?.let {
            typeconvert.getConverter(value.javaClass, PGobject::class.java)?.let { converter ->
                stmt.setObject(index, converter.convert(value))
                return
            }
        }

        super.setParameterForPreparedStatement(stmt, index, value)
    }

    override fun placeholderForParameter(column: Column<*>): String {
        when (column) {
            is StringColumn -> {
                when (column.type) {
                    StringColumnType.JSON -> return "?::JSONB"
                    StringColumnType.UUID -> return "?::UUID"
                    else -> Unit
                }
            }
        }

        return super.placeholderForParameter(column)
    }
}