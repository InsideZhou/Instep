package instep.orm.sql.impl

import instep.Instep
import instep.orm.sql.Condition
import instep.orm.sql.Dialect
import instep.orm.sql.ObjectSelectPlan
import instep.orm.sql.dialect.H2Dialect
import instep.servicecontainer.ServiceNotFoundException

class DefaultObjectSelectPlan(val obj: Any, val dialect: Dialect) : ObjectSelectPlan {
    constructor(obj: Any) : this(obj, Instep.make(Dialect::class.java))

    override val statement: String
        get() {
            var sql = if (select.isEmpty()) "SELECT * FROM $from" else "SELECT ${select.joinToString(",")} FROM $from"

            val whereClause = where?.expression
            if (null != whereClause) {
                sql += "\nWHERE $whereClause"
            }

            if (groupBy.isNotEmpty()) {
                sql += "\nGROUP BY ${groupBy.joinToString(",")}"
            }

            val havingClause = having?.expression
            if (null != havingClause) {
                sql += "\nHAVING $havingClause"
            }

            if (orderBy.isNotEmpty()) {
                sql += "\nORDER BY ${orderBy.joinToString(",")}"
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

    override var select: List<String> = Instep.reflect(obj).properties.map { it.name.toUpperCase() }

    override var from: String = obj.javaClass.simpleName.toUpperCase()

    override var where: Condition? = null

    override var groupBy: List<String> = emptyList()

    override var having: Condition? = null

    override var orderBy: List<String> = emptyList()

    override var limit: Int = -1

    override var offset: Int = 0

    override fun where(vararg conditions: Condition): ObjectSelectPlan {
        where = conditions.reduce(Condition::and)
        return this
    }

    override fun groupBy(vararg columns: String): ObjectSelectPlan {
        groupBy += columns
        return this
    }

    override fun having(vararg conditions: Condition): ObjectSelectPlan {
        having = conditions.reduce(Condition::and)
        return this
    }

    override fun orderBy(vararg orderBys: String): ObjectSelectPlan {
        orderBy += orderBys
        return this
    }

    override fun limit(limit: Int): ObjectSelectPlan {
        this.limit = limit
        return this
    }

    override fun offset(offset: Int): ObjectSelectPlan {
        this.offset = offset
        return this
    }

    companion object {
        private const val serialVersionUID = 3835427608445193282L

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