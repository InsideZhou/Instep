package instep.reflection

import java.io.Serializable
import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * Reflect details of type.
 */
interface Mirror<T : Any> : Serializable {
    val type: Class<T>

    val annotations: Set<Annotation>
    val parents: Set<Class<*>>

    val getters: Set<Method>
    val setters: Set<Method>

    val fieldsWithGetter: Set<Field>
    val fieldsWithSetter: Set<Field>

    val properties: Set<Field>

    /**
     * Find a getter.
     * @param name Getter's name,  with or without "get/is" prefix.
     * @param ignoreCase ignore name case or not.
     */
    fun findGetter(name: String, ignoreCase: Boolean = true): Method?

    /**
     * Find a setter.
     * @param name Setter's name,  with or without "set" prefix.
     * @param ignoreCase ignore name case or not.
     */
    fun findSetter(name: String, ignoreCase: Boolean = true): Method?
}
