@file:Suppress("unused")

package instep.dao.sql.impl

import instep.Instep
import instep.dao.sql.Dialect
import instep.dao.sql.ResultSetDelegate
import instep.dao.sql.ResultSetValueExtractor
import instep.reflection.Mirror
import java.sql.ResultSet

object Helper {
    fun <T : Any> resultSetToInstanceByInstanceFirst(rs: ResultSet, dialect: Dialect, mirror: Mirror<T>, columnInfoSet: Set<ResultSetColumnInfo>): T {
        val instance = mirror.type.newInstance()
        val resultSetDelegate = Instep.make(ResultSetDelegate::class.java).getDelegate(dialect, rs)
        val setters = mirror.setters
        val resultSetValueExtractor = Instep.make(ResultSetValueExtractor::class.java)

        setters.forEach { setter ->
            columnInfoSet.forEach columnLoop@{ col ->
                if (setter.name.contains(col.label, true)) {
                    setter.invoke(instance, resultSetValueExtractor.extract(setter.parameterTypes[0], resultSetDelegate, col.index))
                }
            }
        }

        return instance
    }

    fun <T : Any> resultSetToInstanceByRowFirst(rs: ResultSet, dialect: Dialect, mirror: Mirror<T>, columnInfoSet: Set<ResultSetColumnInfo>): T {
        val instance = mirror.type.newInstance()
        val resultSetDelegate = Instep.make(ResultSetDelegate::class.java).getDelegate(dialect, rs)
        val resultSetValueExtractor = Instep.make(ResultSetValueExtractor::class.java)

        columnInfoSet.forEach { col ->
            val setter = mirror.findSetter(col.label)

            setter?.invoke(instance, resultSetValueExtractor.extract(setter.parameterTypes[0], resultSetDelegate, col.index))
        }

        return instance
    }
}