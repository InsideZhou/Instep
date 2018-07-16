package instep.reflection

import instep.Instep
import org.testng.Assert
import org.testng.annotations.Test

open class A(open val name: String) {
    var idOfA = "A's id"
}

open class B(override val name: String) : A(name) {
    var idOfB = "B's id"
}

open class C(override val name: String) : B(name) {
    var idOfC = "C's id"
}

class D(override val name: String) : C(name) {
    var idOfD = "d's id"
}

object MirrorTest {
    @Test
    fun parentsOfClass() {
        val parentsOfD = Instep.reflect(D::class.java).parents
        assert(!parentsOfD.contains(Any::class.java), { "parents of class should not include 'Any'" })
    }

    @Test
    fun inheritedProperties() {
        val mirror = JMirror(D("ddd"))
        Assert.assertEquals(
            mirror.getPropertiesUntil(B::class.java).map { it.field.name }.toSet(),
            setOf(D::idOfD.name, D::idOfC.name, D::name.name)
        )
    }
}