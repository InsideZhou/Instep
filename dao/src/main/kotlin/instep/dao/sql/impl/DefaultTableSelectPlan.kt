package instep.dao.sql.impl

import instep.dao.sql.*

open class DefaultTableSelectPlan(override val from: Table) : TableSelectPlan, SubSQLPlan<TableSelectPlan>() {
    protected open val selectWords get() = if (distinct) "SELECT DISTINCT" else "SELECT"

    protected open val baseSql: String
        get() {
            return if (select.isEmpty()) {
                "$selectWords * FROM ${from.tableName}"
            }
            else {
                val selectTxt = select.joinToString(",") {
                    if (it.alias.isBlank()) {
                        it.text
                    }
                    else {
                        "${it.text} AS ${it.alias}"
                    }
                }

                "$selectWords $selectTxt FROM ${from.tableName}"
            }
        }

    protected open fun joinTypeToStr(joinType: JoinType): String = when (joinType) {
        JoinType.Left -> "LEFT JOIN"
        JoinType.Right -> "RIGHT JOIN"
        JoinType.Inner -> "JOIN"
        JoinType.Outer -> "OUTER JOIN"
    }

    protected open val joinTxt: String
        get() {
            val txt = join.joinToString("\n") {
                val joinType = joinTypeToStr(it.joinType)

                if (it.alias.isBlank()) {
                    "$joinType ${it.text}"
                }
                else {
                    "$joinType ${it.text} AS ${it.alias}"
                }
            }

            return if (txt.isEmpty()) "" else "\n$txt"
        }

    protected open val whereTxt: String
        get() {
            return if (where.text.isBlank()) {
                ""
            }
            else {
                "\nWHERE ${where.text}"
            }
        }

    protected open val groupByTxt: String
        get() {
            return if (groupBy.isEmpty()) {
                ""
            }
            else {
                val txt = groupBy.joinToString(",") { it.text }
                "\nGROUP BY $txt"
            }
        }

    protected open val havingTxt: String
        get() {
            return if (having.text.isBlank()) {
                ""
            }
            else {
                "\nHAVING ${having.text}"
            }
        }

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
            val sql = baseSql + joinTxt + whereTxt + groupByTxt + havingTxt + orderByTxt
            return from.dialect.pagination.statement(sql, limit, offset)
        }

    override val parameters: List<Any?>
        get() {
            val params = join.flatMap { it.parameters } + where.parameters + having.parameters
            return from.dialect.pagination.parameters(params, limit, offset)
        }

    override var select = emptyList<ColumnExpression>()

    override var distinct: Boolean = false

    override var join = emptyList<FromItem<*>>()

    override var where: Condition = Condition.empty

    override var groupBy = emptyList<ColumnExpression>()

    override var having: Condition = Condition.empty

    override var orderBy: List<OrderBy> = emptyList()

    override var limit: Int = -1

    override var offset: Int = 0

    override var alias: String = ""

    override fun selectExpression(vararg columnExpressions: ColumnExpression): TableSelectPlan {
        this.select += columnExpressions.toList()
        return this
    }

    override fun distinct(): TableSelectPlan {
        this.distinct = true
        return this
    }

    override fun groupBy(vararg columnExpressions: ColumnExpression): TableSelectPlan {
        this.groupBy += columnExpressions
        return this
    }

    override fun having(vararg conditions: Condition): TableSelectPlan {
        this.having = if (this.having.text.isBlank()) {
            conditions.reduce(Condition::and)
        }
        else {
            this.having.andGroup(conditions.reduce(Condition::and))
        }

        return this
    }

    override fun orderBy(vararg orderBys: OrderBy): TableSelectPlan {
        this.orderBy += orderBys
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

    override fun join(fromItem: FromItem<*>): TableSelectPlan {
        this.join += listOf(fromItem)
        return this
    }

    override fun leftJoin(table: Table): TableSelectPlan {
        return join(TableFromItem(JoinType.Left, table))
    }

    override fun join(table: Table): TableSelectPlan {
        return join(TableFromItem(JoinType.Inner, table))
    }

    override fun rightJoin(table: Table): TableSelectPlan {
        return join(TableFromItem(JoinType.Right, table))
    }

    override fun outerJoin(table: Table): TableSelectPlan {
        return join(TableFromItem(JoinType.Outer, table))
    }
}