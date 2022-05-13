package instep.dao.sql


infix fun <T : Number> Aggregate.eq(value: T): Condition = PackageObject.dialect.eq(alias, value)

infix fun <T : Number> Aggregate.gt(value: T): Condition = PackageObject.dialect.gt(alias, value)

infix fun <T : Number> Aggregate.gte(value: T): Condition = PackageObject.dialect.gte(alias, value)

infix fun <T : Number> Aggregate.lt(value: T): Condition = PackageObject.dialect.lt(alias, value)

infix fun <T : Number> Aggregate.lte(value: T): Condition = PackageObject.dialect.lte(alias, value)
