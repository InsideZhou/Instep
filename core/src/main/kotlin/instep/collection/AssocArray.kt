@file:Suppress("SortModifiers")

package instep.collection

import java.util.*

/**
 * Associative array.
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
open class AssocArray(val keyIgnoreCase: Boolean = false) : Collection<Any?> {
    constructor(entries: Array<out Pair<Any, Any?>>, keyIgnoreCase: Boolean = false) : this(keyIgnoreCase) {
        addPairs(entries)
    }

    constructor(values: Array<out Any?>, keyIgnoreCase: Boolean = false) : this(keyIgnoreCase) {
        addValues(values)
    }

    private val map = LinkedHashMap<AAKey, Any?>()
    private var maxIntKey = -1

    private fun addPairs(entries: Array<out Pair<Any, Any?>>) {
        entries.forEach { entry ->
            val aaKey = generateKey(entry.first)
            if (aaKey.usingInt) {
                maxIntKey = aaKey.intKey
            }
            map[aaKey] = entry.second
        }
    }

    private fun addValues(values: Array<out Any?>) {
        values.forEach { v ->
            val aaKey = generateKey(++maxIntKey)
            map[aaKey] = v
        }
    }

    val entries: Set<Pair<Any, Any?>>
        get() = map.map { (if (it.key.usingInt) it.key.intKey else it.key.stringKey) to it.value }.toSet()

    override val size: Int
        get() = map.size

    override fun containsAll(elements: Collection<Any?>): Boolean {
        return elements.all { ele -> map.containsValue(ele) }
    }

    override operator fun contains(element: Any?): Boolean {
        return map.containsValue(element)
    }


    override fun iterator(): Iterator<Any?> {
        return map.values.toList().iterator()
    }


    override fun isEmpty(): Boolean {
        return map.isEmpty()
    }

    open fun add(vararg entries: Pair<Any, Any?>) {
        addPairs(entries)
    }

    open fun add(vararg values: Any?) {
        addValues(values)
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
            is String -> AAKey(if (keyIgnoreCase) key.uppercase(Locale.getDefault()) else key)
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
class AAKey private constructor() : Any() {
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
        } catch (e: NumberFormatException) {
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