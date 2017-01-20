package instep.collection

import java.io.Serializable
import java.util.*

/**
 * Associative array.
 */
open class AssocArray(val keyIgnoreCase: Boolean = false) : Serializable, Collection<Any?> {
    constructor(entries: Array<out Pair<Any, Any?>>, keyIgnoreCase: Boolean = false) : this(keyIgnoreCase) {
        addPairs(*entries)
    }

    constructor(values: Array<out Any?>, keyIgnoreCase: Boolean = false) : this(keyIgnoreCase) {
        addValues(*values)
    }

    private val map = LinkedHashMap<AAKey, Any?>()
    private var maxIntKey = -1

    private fun addPairs(vararg entries: Pair<Any, Any?>) {
        entries.forEach { entry ->
            val aaKey = generateKey(entry.first)
            if (aaKey.usingInt) {
                maxIntKey = aaKey.intKey
            }
            map[aaKey] = entry.second
        }
    }

    private fun addValues(vararg values: Any?) {
        values.forEach { v ->
            val aaKey = generateKey(++maxIntKey)
            map[aaKey] = v
        }
    }

    override val size: Int
        get() = map.size

    override fun containsAll(elements: Collection<Any?>): Boolean {
        return elements.all { ele -> map.containsValue(ele) }
    }

    operator override fun contains(element: Any?): Boolean {
        return map.containsValue(element)
    }


    override fun iterator(): Iterator<Any?> {
        return map.values.toList().iterator()
    }

    fun entries(): Set<Pair<Any, Any?>> {
        return map.map { Pair(if (it.key.usingInt) it.key.intKey else it.key.stringKey, it.value) }.toSet()
    }


    override fun isEmpty(): Boolean {
        return map.isEmpty()
    }


    open fun add(vararg entries: Pair<Any, Any?>) {
        addPairs(*entries)
    }

    open fun add(vararg values: Any?) {
        addValues(*values)
    }

    open fun removeKey(key: Any): Any? {
        return map.remove(generateKey(key))
    }

    open fun remove(value: Any?): Int {
        val exists = map.filterValues { it == value }

        exists.forEach { removeKey(if (it.key.usingInt) it.key.intKey else it.key.stringKey) }

        return exists.count()
    }


    operator open fun get(key: Any): Any? {
        return map[generateKey(key)]
    }

    open fun set(value: Any?) {
        add(value)
    }

    operator open fun set(key: Any, value: Any?) {
        val aaKey = generateKey(key)
        if (aaKey.usingInt) {
            maxIntKey = aaKey.intKey
        }
        map[aaKey] = value
    }


    operator open fun plus(array: AssocArray): AssocArray {
        val result = AssocArray()

        result.map.plusAssign(map)
        result.map.plusAssign(array.map)

        result.maxIntKey = maxIntKey + array.maxIntKey

        return result
    }

    protected open fun generateKey(key: Any): AAKey {
        return when (key) {
            is String -> AAKey(if (keyIgnoreCase) key.toUpperCase() else key)
            is Int -> AAKey(key)
            else -> throw UnsupportedOperationException("type of key must be Int or String.")
        }
    }

    companion object {
        private const val serialVersionUID = -2336430043678048399L
    }
}

/**
 * Key of associative array.
 */
class AAKey private constructor() : Serializable, Any() {
    var stringKey = ""
        private set
    var intKey = 0
        private set

    var usingInt = false
        private set

    constructor(key: Int) : this() {
        intKey = key
        usingInt = true
    }

    constructor(key: String) : this() {
        try {
            intKey = key.toInt()
            usingInt = true
        }
        catch(e: NumberFormatException) {
            stringKey = key
        }
    }

    operator override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as AAKey

        if (stringKey != other.stringKey) return false
        if (intKey != other.intKey) return false
        if (usingInt != other.usingInt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = stringKey.hashCode()
        result = 31 * result + intKey
        result = 31 * result + usingInt.hashCode()
        return result
    }
}