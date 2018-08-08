package instep.util

import org.testng.annotations.Test


class LongIdGeneratorTest {
    @Test
    fun g() {
        val idGenerator = LongIdGenerator(0)

        var counter = 0
        val ts = System.currentTimeMillis()
        while (System.currentTimeMillis() - ts < 1000) {
            counter += 1
            println(idGenerator.generate())
        }

        println(counter)
    }
}