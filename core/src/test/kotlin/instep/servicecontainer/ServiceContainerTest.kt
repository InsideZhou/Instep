package instep.servicecontainer

import instep.Instep
import org.testng.annotations.Test

interface ServiceTest
class ServiceTestImplA : ServiceTest
class ServiceTestImplB : ServiceTest

object ServiceContainerTest {
    @Test
    fun bind() {
        Instep.bind(ServiceTest::class.java, ServiceTestImplA())
        Instep.bind(ServiceTest::class.java, ServiceTestImplB(), "ImplB")

        assert(Instep.make(ServiceTest::class.java) is ServiceTestImplA)
        assert(Instep.make(ServiceTest::class.java, "ImplB") is ServiceTestImplB)
    }
}