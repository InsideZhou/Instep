package instep.dao.sql


@Suppress("MemberVisibilityCanBePrivate", "CanBeParameter")
open class ColumnCondition(val column: Column<*>, val operation: String, val value: Any? = null) :
    Condition("${column.qualifiedName} $operation") {

    init {
        if (null != value) {
            this.addParameters(value)
        }
    }
}
