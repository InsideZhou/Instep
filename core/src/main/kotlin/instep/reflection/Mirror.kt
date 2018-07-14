package instep.reflection

import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.superclasses

@Suppress("unused", "MemberVisibilityCanBePrivate")
class Mirror<T : Any>(val type: KClass<T>) {
    constructor(instance: T) : this(instance.javaClass.kotlin)

    val annotations: Set<Annotation> by lazy { type.annotations.toSet() }

    val parents: Set<KClass<*>> by lazy { type.superclasses.toSet() }

    val getters: Set<KProperty.Getter<*>> by lazy { type.memberProperties.map { it.getter }.toSet() }
    val setters: Set<KMutableProperty.Setter<*>> by lazy { mutableProperties.map { it.setter }.toSet() }

    val properties = type.memberProperties.toSet()

    val readableProperties = type.memberProperties.filterNot { it is KMutableProperty<*> }.toSet()
    val mutableProperties = type.memberProperties.mapNotNull { it as? KMutableProperty<*> }.toSet()
}