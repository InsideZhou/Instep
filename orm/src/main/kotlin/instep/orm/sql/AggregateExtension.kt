package instep.orm.sql


infix fun <T : Number> Aggregate.eq(value: T): Condition = Condition.eq(alias, value)

infix fun <T : Number> Aggregate.gt(value: T): Condition = Condition.gt(alias, value)

infix fun <T : Number> Aggregate.gte(value: T): Condition = Condition.gte(alias, value)

infix fun <T : Number> Aggregate.lt(value: T): Condition = Condition.lt(alias, value)

infix fun <T : Number> Aggregate.lte(value: T): Condition = Condition.lte(alias, value)
