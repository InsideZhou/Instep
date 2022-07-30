package instep.dao.sql

import instep.collection.AAKey
import instep.collection.AssocArray


class DataRow(keyIgnoreCase: Boolean = false) : AssocArray(keyIgnoreCase) {
    override fun generateKey(key: Any): AAKey {
        return when (key) {
            is Column<*> -> super.generateKey(key.name)
            else -> super.generateKey(key)
        }
    }
}