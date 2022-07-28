package instep.typeconversion

import java.lang.annotation.Inherited
import java.lang.annotation.Repeatable
import kotlin.reflect.KClass


@Target(AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@MustBeDocumented
@Repeatable(
    ConverterEligible.List::class
)
annotation class ConverterEligible(
    val type: KClass<*>,
) {
    @Target(AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
    @Retention(AnnotationRetention.RUNTIME)
    @Inherited
    @MustBeDocumented
    annotation class List(vararg val value: ConverterEligible)
}
