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
        val idGenerator = LongIdGenerator(0, 32, 0, 12, 0, 1517414400L, -1, 1000, Random());
        idGenerator.generate()

        assert(idGenerator.sequence > 0)
    }

    @Test(expectedExceptions = [IllegalArgumentException::class], expectedExceptionsMessageRegExp = "^not enough sequence bits.+")
    fun sequenceBitsNotEnough() {
        LongIdGenerator(0, 48, 0, 12, 0, 1517414400L, 1000, 1, Random());
    }

    @Test(expectedExceptions = [IllegalArgumentException::class], expectedExceptionsMessageRegExp = "^not enough timestamp bits.+")
    fun timestampBitsNotEnough() {
        LongIdGenerator(0, 32, 0, 12, 0, 1517414400L, -1, 1, Random());
    }

    @Test
    fun positiveId() {
        val idGenerator = LongIdGenerator(0, 48, 0, 12, 0, 1517414400L, -1, 1);

        assert(idGenerator.generate() > 0)
    }
}