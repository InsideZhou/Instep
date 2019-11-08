package instep.dao.sql.impl

import instep.collection.AssocArray
import instep.dao.DaoException
import instep.dao.sql.*
import java.io.Serializable

open class DefaultTableSelectPlan(override val from: Table) : TableSelectPlan, SubSQLPlan<TableSelectPlan>(), Serializable {
    val selectColumns
        get() =
            select.filterNotNull().joinToString(",") {
                when (it) {
                    is Column<*> -> it.name
                    is Aggregate -> "${it.expression} AS ${it.alias}"
                    else -> throw DaoException("Expression for SELECT must be Column or Aggregate, now got ${it.javaClass.name}.")
                }
            }

    open val selectWords get() = if (distinct) "SELECT DISTINCT" else "SELECT"

    open val baseSql get() = if (selectColumns.isBlank()) "$selectWords * FROM ${from.tableName}" else "$selectWords $selectColumns FROM ${from.tableName}"

    val whereTxt: String
        get() {
            val txt = where?.expression?.let {
                if (it.isNotBlank()) {
                    "\nWHERE $it"
                }
                else {
                    ""
                }
            }

            return txt ?: ""
        }

    val groupByTxt get() = groupBy.joinToString(",") { it.name }

    val havingTxt get() = having?.let { "\nHAVING  ${it.expression}" } ?: ""

    val orderByTxt: String
        get() {
            val txt = orderBy.map {
                val result = if (it.descending) "${it.column.name} DESC" else it.column.name
                return@map if (it.nullFirst) "$result NULL FIRST" else result
            }.joinToString(",")

            return if (txt.isEmpty()) "" else "\nORDER BY $txt"
        }

    override val statement: String
        get() {
            val sql = baseSql + whereTxt + groupByTxt + havingTxt + orderByTxt
            return from.dialect.pagination.statement(sql, limit, offset)
        }

    override val parameters: List<Any?>
        get() {
            var params = where?.parameters ?: emptyList()

            val havingParams = having?.parameters
            if (null != havingParams) {
                params = params + havingParams
            }

            return from.dialect.pagination.parameters(params, limit, offset)
        }

    override var select: AssocArray = AssocArray()

    override var distinct: Boolean = false

    override var where: Condition? = null

    override var groupBy: List<Column<*>> = emptyList()

    override var having: Condition? = null

    override var orderBy: List<OrderBy> = emptyList()

    override var limit: Int = -1

    override var offset: Int = 0

    override fun select(vararg columnOrAggregates: Any): TableSelectPlan {
        this.select.add(*columnOrAggregates)
        return this
    }

    override fun distinct(): TableSelectPlan {
        this.distinct = true
        return this
    }

    override fun groupBy(vararg columns: Column<*>): TableSelectPlan {
        groupBy = groupBy + columns
        return this
    }

    override fun having(vararg conditions: Condition): TableSelectPlan {
        if (null == having) {
            having = conditions.reduce(Condition::and)
        }
        else {
            val cond = having
            cond?.andGroup(conditions.reduce(Condition::and))
        }

        return this
    }

    override fun orderBy(vararg orderBys: OrderBy): TableSelectPlan {
        orderBy = orderBy + orderBys
        return this
    }

    override fun limit(limit: Int): TableSelectPlan {
        this.limit = limit
        return this
    }

    override fun offset(offset: Int): TableSelectPlan {
        this.offset = offset
        return this
    }

    companion object {
        private const val serialVersionUID = -3599950472910618651L
    }
}