package instep.dao.sql.impl

import instep.collection.AssocArray
import instep.dao.DaoException
import instep.dao.impl.AbstractPlan
import instep.dao.sql.*

class DefaultTableSelectPlan(override val from: Table) : AbstractPlan<TableSelectPlan>(), TableSelectPlan {
    override val statement: String
        get() {
            val selectClause = select.filterNotNull().map {
                when (it) {
                    is Column<*> -> it.name
                    is Aggregate -> "${it.expression} AS ${it.alias}"
                    else -> throw DaoException("Expression for SELECT must be Column or Aggregate, now got ${it.javaClass.name}.")
                }
            }.joinToString(",")

            val selectTxt = if (distinct) "SELECT DISTINCT" else "SELECT"

            var sql = if (selectClause.isBlank()) "$selectTxt * FROM ${from.tableName}" else "$selectTxt $selectClause FROM ${from.tableName}"

            val whereClause = where?.expression
            if (null != whereClause) {
                sql += "\nWHERE $whereClause"
            }

            val groupByClause = groupBy.map { it.name }.joinToString(",")
            if (groupByClause.isNotBlank()) {
                sql += "\nGROUP BY $groupByClause"
            }

            having?.let { sql += "\nHAVING  ${it.expression}" }

            val orderByClause = orderBy.map {
                val result = if (it.descending) "${it.column.name} DESC" else it.column.name
                return@map if (it.nullFirst) "$result NULL FIRST" else result
            }.joinToString(",")

            if (orderByClause.isNotBlank()) {
                sql += "\nORDER BY $orderByClause"
            }

            return from.dialect.pagination.statement(sql, limit, offset)
        }

    override val parameters: List<Any?>
        get() {
            var params = where?.parameters ?: emptyList()

            val havingParams = having?.parameters
            if (null != havingParams) {
                params += havingParams
            }

            return from.dialect.pagination.parameters(params, limit, offset)
        }

    override var select: AssocArray = AssocArray()
        private set

    override var distinct: Boolean = false
        private set

    override var where: Condition? = null

    override var groupBy: List<Column<*>> = emptyList()
        private set

    override var having: Condition? = null
        private set

    override var orderBy: List<OrderBy> = emptyList()
        private set

    override var limit: Int = -1
        private set

    override var offset: Int = 0
        private set

    override fun select(vararg columnOrAggregates: Any): TableSelectPlan {
        this.select.add(*columnOrAggregates)
        return this
    }

    override fun distinct(): TableSelectPlan {
        this.distinct = true
        return this
    }

    override fun groupBy(vararg columns: Column<*>): TableSelectPlan {
        groupBy += columns
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
        orderBy += orderBys
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