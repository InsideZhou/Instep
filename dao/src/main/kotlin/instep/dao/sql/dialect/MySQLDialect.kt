package instep.dao.sql.dialect

import instep.dao.DaoException
import instep.dao.sql.DateTimeColumn
import instep.dao.sql.DateTimeColumnType
import instep.dao.sql.IntegerColumn
import instep.dao.sql.IntegerColumnType
import java.sql.PreparedStatement
import java.sql.Time
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

open class MySQLDialect : CommentInTableDefinitionDialect() {
    override val returningClauseForInsert = "*"

    open class ResultSet(private val rs: java.sql.ResultSet) : AbstractDialect.ResultSet(rs) {
        private val calendar = Calendar.getInstance(TimeZone.getDefault())

        override fun getInstant(index: Int): Instant? {
            return rs.getTimestamp(index, calendar)?.let(Timestamp::toInstant)
        }

        override fun getInstant(label: String): Instant? {
            return rs.getTimestamp(label, calendar)?.let(Timestamp::toInstant)
        }

        override fun getLocalDate(index: Int): LocalDate? {
            return rs.getDate(index, calendar)?.let(java.sql.Date::toLocalDate)
        }

        override fun getLocalDate(label: String): LocalDate? {
            return rs.getDate(label, calendar)?.let(java.sql.Date::toLocalDate)
        }

        override fun getLocalTime(index: Int): LocalTime? {
            return rs.getTime(index, calendar)?.let(Time::toLocalTime)
        }

        override fun getLocalTime(label: String): LocalTime? {
            return rs.getTime(label, calendar)?.let(Time::toLocalTime)
        }

        override fun getLocalDateTime(index: Int): LocalDateTime? {
            return rs.getTimestamp(index, calendar)?.let(Timestamp::toLocalDateTime)
        }

        override fun getLocalDateTime(label: String): LocalDateTime? {
            return rs.getTimestamp(label, calendar)?.let(Timestamp::toLocalDateTime)
        }
    }

    override val offsetDateTimeSupported: Boolean = false

    override fun setParameterForPreparedStatement(stmt: PreparedStatement, index: Int, value: Any?) {
        val calendar = Calendar.getInstance()

        when (value) {
            is Instant -> stmt.setTimestamp(index, Timestamp.from(value), calendar)
            is LocalDate -> stmt.setDate(index, java.sql.Date.valueOf(value), calendar)
            is LocalTime -> stmt.setTime(index, Time.valueOf(value), calendar)
            is LocalDateTime -> stmt.setTimestamp(index, Timestamp.valueOf(value), calendar)
            else -> super.setParameterForPreparedStatement(stmt, index, value)
        }
    }

    override fun definitionForAutoIncrementColumn(column: IntegerColumn): String = when (column.type) {
        IntegerColumnType.Long -> "BIGINT AUTO_INCREMENT"
        else -> "INTEGER AUTO_INCREMENT"
    }

    override fun definitionForDateTimeColumn(column: DateTimeColumn): String {
        return when (column.type) {
            DateTimeColumnType.DateTime -> "DATETIME"
            DateTimeColumnType.OffsetDateTime -> throw DaoException("DateTimeColumn.OffsetDateTime is not support")
            else -> return super.definitionForDateTimeColumn(column);
        }
    }
}