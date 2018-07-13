package instep.util

import java.io.Serializable
import java.security.SecureRandom

@Suppress("unused", "CanBeParameter")
/**
 * 64 bits id generator. twitter snowflake.
 * timestamp - highPadding - worker - lowPadding - sequence
 */
class LongIdGenerator(
    val workerId: Int,
    timestampBits: Int,
    highPaddingBits: Int,
    workerIdBits: Int,
    lowPaddingBits: Int,
    val random: SecureRandom,
    val epoch: Long,
    val sequenceStartRange: Int
) : Serializable {

    constructor(
        workerId: Int,
        timestampBits: Int,
        highPaddingBits: Int,
        workerIdBits: Int,
        lowPaddingBits: Int
    ) : this(
        workerId, timestampBits, highPaddingBits, workerIdBits, lowPaddingBits,
        SecureRandom.getInstanceStrong(),
        1517414400L, //Thu Feb 01 2018 00:00:00 GMT, seconds
        1000
    )

    constructor(workerId: Int) : this(
        workerId,
        32,
        0,
        12,
        0
    )

    var maxWorkerId: Int = -1
        private set

    var maxSequenceValue: Int = -1
        private set

    var workerIdShift: Int = -1
        private set

    var timestampShift: Int = -1
        private set

    var sequence = 0
        private set

    var lastTimestamp = -1L
        private set

    init {
        val sequenceBits = 64 - timestampBits - highPaddingBits - workerIdBits - lowPaddingBits

        maxWorkerId = maxIntegerAtBits(workerIdBits)
        maxSequenceValue = maxIntegerAtBits(sequenceBits)
        workerIdShift = sequenceBits + lowPaddingBits
        timestampShift = sequenceBits + lowPaddingBits + workerIdBits + highPaddingBits

        if (workerId > maxWorkerId || workerId < 0) {
            throw IllegalArgumentException("worker Id can't be greater than $maxWorkerId or less than 0")
        }
    }

    @Throws(Exception::class)
    @Synchronized
    fun generate(): Long {
        var timestamp = timeGen()

        if (timestamp < lastTimestamp) {
            throw Exception("Clock moved backwards.  Refusing to generate id for ${lastTimestamp - timestamp} milliseconds")
        }

        if (lastTimestamp == timestamp) {
            sequence = sequence + 1 and maxSequenceValue

            if (0 == sequence) {
                timestamp = nextTick(lastTimestamp)
            }
        }
        else {
            sequence = random.nextInt(sequenceStartRange)
        }

        lastTimestamp = timestamp

        return ((timestamp - epoch) shl timestampShift) or (workerId shl workerIdShift).toLong() or sequence.toLong()
    }

    private fun nextTick(lastTimestamp: Long): Long {
        var timestamp = timeGen()
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen()
        }
        return timestamp
    }

    private fun timeGen(): Long {
        return System.currentTimeMillis() / 1000
    }

    companion object {
        private const val serialVersionUID = 8188036141814766454L

        fun maxIntegerAtBits(bits: Int): Int = -1 xor (-1 shl bits)
    }
}
