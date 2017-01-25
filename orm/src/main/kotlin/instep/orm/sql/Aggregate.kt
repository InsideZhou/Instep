package instep.orm.sql

/**
 * Table column aggregate.
 */
interface Aggregate {
    val expression: String
    val alias: String
}