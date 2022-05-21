package instep.dao.sql


infix fun <T : Number> Aggregate.eq(value: T): Condition = Package.dialect.eq(alias, value)

infix fun <T : Number> Aggregate.gt(value: T): Condition = Package.dialect.gt(alias, value)

infix fun <T : Number> Aggregate.gte(value: T): Condition = Package.dialect.gte(alias, value)

infix fun <T : Number> Aggregate.lt(value: T): Condition = Package.dialect.lt(alias, value)

infix fun <T : Number> Aggregate.lte(value: T): Condition = Package.dialect.lte(alias, value)
