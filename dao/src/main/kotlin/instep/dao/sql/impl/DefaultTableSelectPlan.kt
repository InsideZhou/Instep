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
                val selectTexts = select.joinToString(",") {
                    var txt = if (it is ColumnSelectExpression && join.isNotEmpty()) {
                        "${it.column.table.tableName}.${it.column.name}"
                    }
                    else {
                        it.text
                    }

                    if (it.alias.isNotBlank()) {
                        txt += " AS ${it.alias}"
                    }

                    txt
                }

                "$selectWords $selectTexts FROM ${from.tableName}"
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
            val txt = join.joinToString("\n") { "${joinTypeToStr(it.joinType)} ${it.text} ON ${it.joinCondition.text}" }
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
                val txt = groupBy.joinToString(",") {
                    if (it.table == from) {
                        it.name
                    }
                    else {
                        it.qualifiedName
                    }
                }

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
            val params = join.flatMap { it.parameters + it.joinCondition.parameters } + where.parameters + having.parameters
            return from.dialect.pagination.parameters(params, limit, offset)
        }

    override var select = emptyList<SelectExpression>()

    override var distinct: Boolean = false

    override var join = emptyList<JoinItem<*>>()

    override var where: Condition = Condition.empty

    override var groupBy = emptyList<Column<*>>()

    override var having: Condition = Condition.empty

    override var orderBy: List<OrderBy> = emptyList()

    override var limit: Int = -1

    override var offset: Int = 0

    override var alias: String = ""

    override fun selectExpression(vararg selectExpression: SelectExpression): TableSelectPlan {
        this.select += selectExpression.toList()
        return this
    }

    override fun distinct(): TableSelectPlan {
        this.distinct = true
        return this
    }

    override fun groupBy(vararg columns: Column<*>): TableSelectPlan {
        this.groupBy += columns
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

    override fun join(joinItem: JoinItem<*>): TableSelectPlan {
        this.join += listOf(joinItem)
        return this
    }

    override fun join(from: Column<*>, to: Column<*>): TableSelectPlan {
        val condition = Condition("${from.table.tableName}.${from.name} = ${to.table.tableName}.${to.name}")
        return join(TableJoinItem(JoinType.Inner, to, condition))
    }

    override fun join(to: Column<*>): TableSelectPlan {
        val from = this.from.primaryKey ?: throw IllegalArgumentException("primary column required as join from")
        return join(from, to)
    }

    override fun leftJoin(from: Column<*>, to: Column<*>): TableSelectPlan {
        val condition = Condition("${from.table.tableName}.${from.name} = ${to.table.tableName}.${to.name}")
        return join(TableJoinItem(JoinType.Left, from, condition))
    }

    override fun rightJoin(from: Column<*>, to: Column<*>): TableSelectPlan {
        val condition = Condition("${from.table.tableName}.${from.name} = ${to.table.tableName}.${to.name}")
        return join(TableJoinItem(JoinType.Right, from, condition))
    }

    override fun outerJoin(from: Column<*>, to: Column<*>): TableSelectPlan {
        val condition = Condition("${from.table.tableName}.${from.name} = ${to.table.tableName}.${to.name}")
        return join(TableJoinItem(JoinType.Outer, from, condition))
    }
}