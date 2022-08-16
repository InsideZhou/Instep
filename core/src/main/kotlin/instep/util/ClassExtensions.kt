package instep.util

import java.lang.reflect.Field
import java.lang.reflect.Method


fun Class<*>.isCommon(): Boolean {
    val special = this.isAnnotation || this.isEnum || this.isInterface || this.isPrimitive || this.isContainer()

    return !special
}

fun Class<*>.isContainer(): Boolean {
    val special = this.isArray || Map::class.java.isAssignableFrom(this) || Collection::class.java.isAssignableFrom(this)

    return !special
}

fun Field.path(): String = "${this.declaringClass.name}#${this.name}"
fun Method.path(): String = "${this.declaringClass.name}#${this.name}"
