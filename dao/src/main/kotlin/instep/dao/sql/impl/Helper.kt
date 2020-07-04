@file:Suppress("unused")

package instep.dao.sql.impl

import instep.Instep
import instep.dao.sql.Dialect
import instep.dao.sql.ResultSetDelegate
import instep.dao.sql.ResultSetValueExtractor
import instep.reflection.JMirror
import java.sql.ResultSet

object Helper {
    fun <T : Any> resultSetToInstanceByInstanceFirst(rs: ResultSet, dialect: Dialect, mirror: JMirror<T>, columnInfoSet: Set<ResultSetColumnInfo>): T {
        val instance = mirror.type.getDeclaredConstructor().newInstance()
        val resultSetDelegate = Instep.make(ResultSetDelegate::class.java).getDelegate(dialect, rs)
        val resultSetValueExtractor = Instep.make(ResultSetValueExtractor::class.java)

        mirror.mutableProperties.forEach { p ->
            columnInfoSet.filter { p.field.name.equals(it.label, true) }.forEach columnLoop@{ col ->
                p.setter.invoke(instance, resultSetValueExtractor.extract(p.field.type, resultSetDelegate, col.index))
            }
        }

        return instance
    }

    fun <T : Any> resultSetToInstanceByRowFirst(rs: ResultSet, dialect: Dialect, mirror: JMirror<T>, columnInfoSet: Set<ResultSetColumnInfo>): T {
        val instance = mirror.type.getDeclaredConstructor().newInstance()
        val resultSetDelegate = Instep.make(ResultSetDelegate::class.java).getDelegate(dialect, rs)
        val resultSetValueExtractor = Instep.make(ResultSetValueExtractor::class.java)

        columnInfoSet.forEach { col ->
            mirror.mutableProperties.forEach { p ->
                p.setter.invoke(instance, resultSetValueExtractor.extract(p.field.type, resultSetDelegate, col.index))
            }
        }

        return instance
    }
}