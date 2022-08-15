package instep.util

import java.util.*

@JvmOverloads
fun String.capitalize(locale: Locale? = null): String {
    return this.replaceFirstChar {
        if (it.isLowerCase()) {
            it.titlecase(locale ?: Locale.getDefault())
        }
        else {
            it.toString()
        }
    }
}

@JvmOverloads
fun String.snakeToCamelCase(locale: Locale? = null): String {
    return "_([a-zA-Z\\d])".toRegex().replace(this) {
        it.groupValues[1].uppercase(locale ?: Locale.getDefault())
    }
}

@JvmOverloads
fun String.camelCaseToSnake(locale: Locale? = null): String {
    val len = this.length
    val sb = StringBuilder(len)

    this.forEachIndexed { index, item ->
        if (!item.isLetter()) {
            sb.append(item)
            return@forEachIndexed
        }

        if (0 == index) {
            sb.append(item.lowercase(locale ?: Locale.getDefault()))
            return@forEachIndexed
        }

        val previous = this[index - 1]
        val next = if (index + 1 < len) {
            "${this[index + 1]}"
        }
        else {
            ""
        }

        val nextChar = next.firstOrNull()

        if (item.isUpperCase()) {
            if ('_' != previous && (!previous.isUpperCase() || (next.isNotBlank() && !nextChar!!.isUpperCase() && !nextChar.isDigit() && '_' != nextChar))) {
                sb.append("_")
            }
        }

        sb.append(item.lowercase(locale ?: Locale.getDefault()))
    }

    return sb.toString()
}
