package instep.util

import org.testng.annotations.Test
import java.util.*


class LongIdGeneratorTest {
    @Test
    fun maxWorkerId() {
        val idGenerator = LongIdGenerator(0)

        assert(idGenerator.maxWorkerId == 4095)
    }

    @Test
    fun negativeSequenceStart() {
        val idGenerator = LongIdGenerator(0, 32, 0, 12, 0, 1517414400L, -1, Random());
        idGenerator.generate()

        assert(idGenerator.sequence > 0)
    }
}