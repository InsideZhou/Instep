package instep.reflection

import instep.util.capitalize
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier

@Suppress("UNCHECKED_CAST", "unused")
open class JMirror<T : Any>(val type: Class<T>) {
    constructor(instance: T) : this(instance::class.java as Class<T>)

    val annotations: Set<Annotation> by lazy { type.annotations.toSet() }

    val parents: Set<Class<in T>> by lazy {
        val superClasses = mutableSetOf<Class<in T>>()
        var parent = type.superclass
        while (null != parent && parent != Any::class.java) {
            superClasses.add(parent)
            parent = parent.superclass
        }
        superClasses
    }

    val properties: Set<Property> by lazy {
        type.declaredFields.map { f ->
            val getter = type.declaredMethods.find {
                it.name == "get${f.name.capitalize()}" && it.returnType.isAssignableFrom(f.type) ||
                        Boolean::class.java == f.type && it.name == "is${f.name.capitalize()}"
            }

            val setter = type.declaredMethods.find {
                it.name == "set${f.name.capitalize()}" && it.parameterTypes.size == 1 && f.type.isAssignableFrom(it.parameterTypes[0])
            }

            Property(f, getter, setter)
        }.toSet()
    }

    val mutableProperties: Set<MutableProperty> by lazy {
        pickMutableProperties(properties).toSet()
    }

    val readableProperties: Set<ReadableProperty> by lazy {
        pickReadableProperties(properties).toSet()
    }

    fun getPropertiesUntil(cls: Class<*>): Set<Property> {
        val index = parents.indexOf(cls)
        if (-1 == index) return emptySet()

        return properties + parents.take(index).flatMap { JMirror(it as Class<*>).properties }
    }

    fun getMutablePropertiesUntil(cls: Class<*>): Set<MutableProperty> {
        return pickMutableProperties(getPropertiesUntil(cls)).toSet()
    }

    fun getReadablePropertiesUntil(cls: Class<*>): Set<ReadableProperty> {
        return pickReadableProperties(getPropertiesUntil(cls)).toSet()
    }

    fun findFactoryMethodBy(cls: Class<*>): Method? {
        return type.declaredMethods.find {
            Modifier.isPublic(it.modifiers) &&
                    1 == it.parameterCount &&
                    it.parameterTypes[0].isAssignableFrom(cls) &&
                    type.isAssignableFrom(it.returnType)
        }
    }

    fun findFactoryConstructorBy(cls: Class<*>): Constructor<*>? {
        return type.declaredConstructors.find {
            Modifier.isPublic(it.modifiers) &&
                    1 == it.parameterCount &&
                    it.parameterTypes[0].isAssignableFrom(cls)
        }
    }

    companion object {
        fun pickMutableProperties(properties: Iterable<Property>): Iterable<MutableProperty> {
            return properties.filterNot { null == it.setter }.map { MutableProperty(it.field, it.setter!!) }
        }

        fun pickReadableProperties(properties: Iterable<Property>): Iterable<ReadableProperty> {
            return properties.filterNot { null == it.getter }.map { ReadableProperty(it.field, it.getter!!) }
        }
    }
}

data class Property(val field: Field, val getter: Method?, val setter: Method?)
data class MutableProperty(val field: Field, val setter: Method)
data class ReadableProperty(val field: Field, val getter: Method)
