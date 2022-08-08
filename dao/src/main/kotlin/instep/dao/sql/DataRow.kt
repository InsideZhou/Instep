package instep.dao.sql

import instep.Instep
import instep.InstepLogger
import instep.collection.AAKey
import instep.collection.AssocArray
import instep.typeconversion.Converter
import instep.typeconversion.ConverterEligible
import instep.typeconversion.TypeConversion
import instep.util.path
import instep.util.snakeToCamelCase
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset


class DataRow(keyIgnoreCase: Boolean = false) : AssocArray(keyIgnoreCase) {
    private val logger = InstepLogger.getLogger(DataRow::class.java)

    override fun generateKey(key: Any): AAKey {
        return when (key) {
            is Column<*> -> super.generateKey(key.name)
            else -> super.generateKey(key)
        }
    }

    fun <R : Any> fillUp(cls: Class<R>): R {
        val instance = cls.getDeclaredConstructor().newInstance()
        fillUp(instance)
        return instance
    }

    @Suppress("UNCHECKED_CAST")
    fun fillUp(instance: Any) {
        val typeConversion = Instep.make(TypeConversion::class.java)
        val targetMutableProperties = Instep.reflect(instance).getMutablePropertiesUntil(Any::class.java)

        entries.filter { null != it.second }.forEach { pair ->
            val property = targetMutableProperties.find { it.field.name.equals(pair.first.toString().snakeToCamelCase()) } ?: return@forEach
            val setterType = property.setter.parameterTypes.first()
            val value = pair.second!!
            val path = instance.javaClass.path(property.field)

            logger.message("setting value to target by setter")
                .context("property", path)
                .context("property_type", setterType.name)
                .context("value", value)
                .trace()

            try {
                property.setter.getAnnotationsByType(ConverterEligible::class.java)
                    .firstNotNullOfOrNull { converterEligible ->
                        (typeConversion.getConverter(converterEligible.type.java, setterType, path) as? Converter<Any, Any>)
                    }
                    ?.let { converter ->
                        property.setter.invoke(instance, converter.convert(value))
                        return@forEach
                    }

                (typeConversion.getConverter(value.javaClass, setterType) as? Converter<Any, Any>)?.let { converter ->
                    property.setter.invoke(instance, converter.convert(value))
                    return@forEach
                }

                if (setterType == String::class.java && value !is String) {
                    property.setter.invoke(instance, value.toString())
                    return@forEach
                }

                when (value) {
                    is String -> {
                        when {
                            setterType.isEnum -> {
                                property.setter.invoke(instance, setterType.enumConstants.first { it.toString() == value })
                            }

                            setterType == Byte::class.java -> value.toByte()
                            setterType == Short::class.java -> value.toShort()
                            setterType == Int::class.java -> value.toInt()
                            setterType == Long::class.java -> value.toLong()
                            setterType == BigInteger::class.java -> value.toBigInteger()

                            setterType == Float::class.java -> value.toFloat()
                            setterType == Double::class.java -> value.toDouble()
                            setterType == BigDecimal::class.java -> value.toBigDecimal()

                            setterType == Boolean::class.java -> value.toBoolean()

                            else -> property.setter.invoke(instance, value)
                        }
                    }

                    is LocalDateTime -> {
                        when (setterType) {
                            Instant::class.java -> property.setter.invoke(instance, value.toInstant(ZoneOffset.UTC))
                            else -> property.setter.invoke(instance, value)
                        }
                    }

                    else -> property.setter.invoke(instance, value)
                }
            }
            catch (e: IllegalArgumentException) {
                logger.message("cannot set value to instance using setter")
                    .context("class", instance.javaClass.name)
                    .context("setter", property.setter.name)
                    .context("setterType", setterType.name)
                    .context("valueClass", value.javaClass.name)
                    .context("value", value.toString())
                    .error()

                throw e
            }
        }
    }
}