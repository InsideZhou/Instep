package instep.util

import java.util.*

/**
 * 64 bits id generator. twitter snowflake.
 * 0 - timestamp - highPadding - worker - lowPadding - sequence
 */
@Suppress("MemberVisibilityCanBePrivate", "CanBeParameter")
open class LongIdGenerator(
    val workerId: Int,
    val timestampBits: Int,
    val highPaddingBits: Int,
    val workerIdBits: Int,
    val lowPaddingBits: Int,
    val epoch: Long,
    val sequenceStartRange: Int,
    val random: Random? = null
) {
    constructor(
        workerId: Int,
        timestampBits: Int,
        highPaddingBits: Int,
        workerIdBits: Int,
        lowPaddingBits: Int
    ) : this(
        workerId, timestampBits, highPaddingBits, workerIdBits, lowPaddingBits,
        1517414400L, //Thu Feb 01 2018 00:00:00 GMT, seconds
        1000,
        Random()
    )

    constructor(workerId: Int) : this(
        workerId,
        32,
        0,
        12,
        0
    )

    val sequenceBits = 63 - timestampBits - highPaddingBits - workerIdBits - lowPaddingBits

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
        maxWorkerId = maxIntegerAtBits(workerIdBits)
        maxSequenceValue = maxIntegerAtBits(sequenceBits)
        workerIdShift = sequenceBits + lowPaddingBits
        timestampShift = sequenceBits + lowPaddingBits + workerIdBits + highPaddingBits

        if (workerId > maxWorkerId || workerId < 0) {
            throw IllegalArgumentException("worker Id can't be greater than $maxWorkerId or less than 0")
        }
    }

    @Synchronized
    fun generate(): Long {
        var timestamp = timeGen()

        if (timestamp < lastTimestamp) {
            throw Exception("Clock moved backwards.  Refusing to generate id for ${lastTimestamp - timestamp} seconds")
        }

        if (lastTimestamp == timestamp) {
            sequence = sequence + 1 and maxSequenceValue

            if (0 == sequence) {
                timestamp = nextTick(lastTimestamp)
            }
        }
        else if (sequenceStartRange > 0) {
            sequence = random?.nextInt(sequenceStartRange) ?: sequenceStartRange
        }
        else {
            sequence = random?.nextInt(maxIntegerAtBits(sequenceBits)) ?: 0
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

    protected open fun timeGen(): Long {
        return System.currentTimeMillis() / 1000
    }

    companion object {
        fun maxIntegerAtBits(bits: Int): Int = -1 xor (-1 shl bits)
    }
}
