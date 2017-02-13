package instep.reflection

import instep.Instep
import org.testng.annotations.Test

open class A(open val name: String)

open class B(override val name: String) : A(name)

open class C(override val name: String) : B(name)

class D(override val name: String) : C(name)

object MirrorTest {
    @Test
    fun parentsOfClass() {
        val parentsOfD = Instep.reflect(D::class.java).parents
        assert(!parentsOfD.contains(Any::class.java), { "parents of class should not include 'Any'" })
    }
}