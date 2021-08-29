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