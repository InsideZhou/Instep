package instep.util

import java.io.Serializable
import java.security.SecureRandom

/**
 * 64 bits id generator. twitter snowflake.
 */
class IdGenerator(val workerId: Long = 0, val regionId: Long = 0) : Serializable {
    private var sequence = 0L

    init {
        if (workerId > maxWorkerId || workerId < 0) {
            throw IllegalArgumentException("worker Id can't be greater than $maxWorkerId or less than 0")
        }

        if (regionId > maxRegionId || regionId < 0) {
            throw IllegalArgumentException("region Id can't be greater than $maxRegionId or less than 0")
        }
    }

    fun generate(): Long {
        return nextId(false, 0)
    }

    @Synchronized
    private fun nextId(isPadding: Boolean, busId: Long): Long {

        var timestamp = timeGen()
        var paddingnum = regionId

        if (isPadding) {
            paddingnum = busId
        }

        if (timestamp < lastTimestamp) {
            throw Exception("Clock moved backwards.  Refusing to generate id for ${lastTimestamp - timestamp} milliseconds")
        }

        if (lastTimestamp == timestamp) {
            sequence = sequence + 1 and sequenceMask
            if (sequence == 0L) {
                timestamp = tailNextMillis(lastTimestamp)
            }
        }
        else {
            sequence = SecureRandom().nextInt(10).toLong()
        }

        lastTimestamp = timestamp

        return timestamp - twepoch shl timestampLeftShift or (paddingnum shl regionIdShift) or (workerId shl workerIdShift) or sequence
    }

    private fun tailNextMillis(lastTimestamp: Long): Long {
        var timestamp = timeGen()
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen()
        }
        return timestamp
    }

    private fun timeGen(): Long {
        return System.currentTimeMillis()
    }

    companion object {
        private const val serialVersionUID = 8188036141814766454L

        var regionIdBits = 4
        var workerIdBits = 6

        val sequenceBits: Int
            get() = 23 - regionIdBits + workerIdBits //23 = 64 bits - timestamp bits

        private var lastTimestamp = -1L

        private val twepoch = 1288834974657L //Thu, 04 Nov 2010 01:42:54 GMT

        private val maxRegionId: Long
            get() = -1L xor (-1L shl regionIdBits)

        private val maxWorkerId: Long
            get() = -1L xor (-1L shl workerIdBits)

        private val sequenceMask: Long
            get() = -1L xor (-1L shl sequenceBits)

        private val workerIdShift: Int
            get() = sequenceBits

        private val regionIdShift: Int
            get() = sequenceBits + workerIdBits

        private val timestampLeftShift: Int
            get() = sequenceBits + workerIdBits + regionIdBits
    }
}
