package instep.util

import java.util.*

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

fun String.snakeToCamelCase(locale: Locale? = null): String {
    return "_([a-zA-Z\\d])".toRegex().replace(this) {
        it.groupValues[1].uppercase(locale ?: Locale.getDefault())
    }
}
