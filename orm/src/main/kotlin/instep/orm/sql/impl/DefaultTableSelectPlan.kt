package instep.orm.sql.impl

import instep.Instep
import instep.collection.AssocArray
import instep.orm.OrmException
import instep.orm.sql.*
import instep.orm.sql.dialect.H2Dialect
import instep.servicecontainer.ServiceNotFoundException

class DefaultTableSelectPlan(override val from: Table, val dialect: Dialect) : TableSelectPlan {
    constructor(table: Table) : this(table, Instep.make(Dialect::class.java))

    override val statement: String
        get() {
            val selectClause = select.filterNotNull().map {
                when (it) {
                    is Column<*> -> it.name
                    is Aggregate -> it.alias
                    else -> throw OrmException("Expression for SELECT must be Column or Aggregate, now got ${it.javaClass.name}.")
                }
            }.joinToString(",")

            var sql = if (selectClause.isBlank()) "SELECT * FROM ${from.tableName}" else "SELECT $selectClause FROM ${from.tableName}"

            val whereClause = where?.expression
            if (null != whereClause) {
                sql += "\nWHERE $whereClause"
            }

            val groupByClause = groupBy.map { it.name }.joinToString(",")
            if (groupByClause.isNotBlank()) {
                sql += "\nGROUP BY $groupByClause"
            }

            val havingClause = having?.expression
            if (null != havingClause) {
                sql += "\nHAVING $havingClause"
            }

            val orderByClause = orderBy.map {
                val result = if (it.descending) "${it.column.name} DESC" else it.column.name
                return@map if (it.nullFirst) "$result NULL FIRST" else result
            }.joinToString(",")

            if (orderByClause.isNotBlank()) {
                sql += "\nORDER BY $orderByClause"
            }

            return dialect.pagination.statement(sql, limit, offset)
        }

    override val parameters: List<Any?>
        get() {
            var params = where?.parameters ?: emptyList()

            val havingParams = having?.parameters
            if (null != havingParams) {
                params += havingParams
            }

            return dialect.pagination.parameters(params, limit, offset)
        }

    override var select: AssocArray = AssocArray()
        private set

    override var where: Condition? = null
        private set

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

    override fun where(vararg conditions: Condition): TableSelectPlan {
        where = conditions.reduce(Condition::and)
        return this
    }

    override fun groupBy(vararg columns: Column<*>): TableSelectPlan {
        groupBy += columns
        return this
    }

    override fun having(vararg conditions: Condition): TableSelectPlan {
        having = conditions.reduce(Condition::and)
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

    override fun clone(): TableSelectPlan {
        val n = DefaultTableSelectPlan(from, dialect)
        n.select = select
        n.where = where
        n.groupBy = groupBy
        n.having = having
        n.limit = limit
        n.offset = offset

        return n
    }

    companion object {
        private const val serialVersionUID = -3599950472910618651L

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