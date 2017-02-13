package instep.servicecontainer

import instep.Instep
import org.testng.annotations.Test

interface ServiceTest
class ServiceTestImplA : ServiceTest
class ServiceTestImplB : ServiceTest

open class A(open val name: String)
open class B(override val name: String) : A(name)
open class C(override val name: String) : B(name)
class D(override val name: String) : C(name)

object ServiceContainerTest {
    @Test
    fun bind() {
        Instep.bind(ServiceTest::class.java, ServiceTestImplA())
        Instep.bind(ServiceTest::class.java, ServiceTestImplB(), "ImplB")

        assert(Instep.make(ServiceTest::class.java) is ServiceTestImplA)
        assert(Instep.make(ServiceTest::class.java, "ImplB") is ServiceTestImplB)

        val d = D("D")
        Instep.bind(D::class.java, d)

        assert(Instep.make(A::class.java) == d)
        assert(Instep.make(B::class.java) == d)
        assert(Instep.make(C::class.java) == d)
        Instep.serviceContainer.remove(D::class.java)
    }
}