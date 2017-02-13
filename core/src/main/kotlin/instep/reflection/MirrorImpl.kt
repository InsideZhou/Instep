package instep.reflection

import java.lang.reflect.Field
import java.lang.reflect.Method

class MirrorImpl<T : Any>(override val type: Class<T>) : Mirror<T> {
    constructor(instance: T) : this(instance.javaClass) {
    }

    override val annotations: Set<Annotation> by lazy {
        type.annotations.toSet()
    }

    override val parents: Set<Class<*>> by lazy {
        val superClasses = mutableSetOf<Class<*>>()
        var parent = type.superclass
        while (null != parent && parent != Any::class.java) {
            superClasses.add(parent)
            parent = parent.superclass
        }
        superClasses
    }

    override val getters: Set<Method>
    override val setters: Set<Method>

    override val fieldsWithGetter: Set<Field>
    override val fieldsWithSetter: Set<Field>

    override val properties: Set<Field>

    init {
        fieldsWithGetter = mutableSetOf<Field>()
        fieldsWithSetter = mutableSetOf<Field>()

        getters = type.declaredFields.map { f ->
            val m = type.declaredMethods.filter {
                it.name == "get${f.name.capitalize()}" && it.returnType.isAssignableFrom(f.type) ||
                    Boolean::class.java == f.type && it.name == "is${f.name.capitalize()}"
            }.singleOrNull()

            if (null != m) fieldsWithGetter.add(f)

            return@map m
        }.filterNotNull().toSet()

        setters = type.declaredFields.map { f ->
            val m = type.declaredMethods.filter {
                it.name == "set${f.name.capitalize()}" && it.parameterTypes.size == 1 && f.type.isAssignableFrom(it.parameterTypes[0])
            }.singleOrNull()

            if (null != m) fieldsWithSetter.add(f)

            return@map m
        }.filterNotNull().toSet()

        properties = fieldsWithGetter.intersect(fieldsWithSetter)
    }

    override fun findGetter(name: String, ignoreCase: Boolean): Method? {
        return getters.find { m -> m.name.endsWith(name, ignoreCase) }
    }

    override fun findSetter(name: String, ignoreCase: Boolean): Method? {
        return setters.find { m -> m.name.endsWith(name, ignoreCase) }
    }

    companion object {
        private const val serialVersionUID = -1198502315155859418L
    }
}