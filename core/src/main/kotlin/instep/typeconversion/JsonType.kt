package instep.typeconversion

/**
 * Used as type conversion target, get some object's JSON string representation.
 */
interface JsonType<S> {
    @Suppress("unused")
    val sourceType: S
    val value: String
}