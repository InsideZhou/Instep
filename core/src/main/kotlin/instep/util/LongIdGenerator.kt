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
    val epochInSeconds: Long,
    val sequenceStartRange: Int,
    val tickAccuracy: Int = 1000,
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

    val epochInMilliSeconds = epochInSeconds * 1000;
    val epochTick = epochInMilliSeconds / tickAccuracy;

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

    var lastTick = -1L
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
        var tick = generateTick()

        if (tick < lastTick) {
            throw Exception("Clock moved backwards. Refusing to generate id for ${lastTick - tick} ticks at $tickAccuracy accuracy.");
        }

        if (lastTick == tick) {
            sequence = sequence + 1 and maxSequenceValue

            if (0 == sequence) {
                tick = nextTick()
            }
        }
        else if (sequenceStartRange > 0) {
            sequence = random?.nextInt(sequenceStartRange) ?: sequenceStartRange
        }
        else {
            sequence = random?.nextInt(maxIntegerAtBits(sequenceBits)) ?: 0
        }

        lastTick = tick

        return ((tick - epochTick) shl timestampShift) or (workerId shl workerIdShift).toLong() or sequence.toLong()
    }

    private fun nextTick(): Long {
        var tick = generateTick()
        while (tick <= lastTick) {
            tick = generateTick()
        }
        return tick
    }

    private fun generateTick(): Long {
        return System.currentTimeMillis() / tickAccuracy
    }

    companion object {
        fun maxIntegerAtBits(bits: Int): Int = -1 xor (-1 shl bits)
    }
}
