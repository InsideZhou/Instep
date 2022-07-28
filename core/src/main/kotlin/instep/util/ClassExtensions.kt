package instep.util

import java.lang.reflect.Field


fun Class<*>.isCommon(): Boolean {
    val special = this.isAnnotation || this.isEnum || this.isInterface || this.isPrimitive || this.isContainer()

    return !special
}

fun Class<*>.isContainer(): Boolean {
    val special = this.isArray || Map::class.java.isAssignableFrom(this) || Collection::class.java.isAssignableFrom(this)

    return !special
}

fun Class<*>.path(field: Field): String = "${this.name}#${field.name}"
