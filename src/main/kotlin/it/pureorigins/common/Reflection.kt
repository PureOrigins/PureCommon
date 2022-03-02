package it.pureorigins.common

import java.lang.reflect.Field
import java.lang.reflect.Modifier

fun getPrivate(field: Field, instance: Any? = null): Any? {
    val oldAccessible = @Suppress("DEPRECATION") field.isAccessible
    field.isAccessible = true
    val value = field[instance]
    field.isAccessible = oldAccessible
    return value
}

fun getPrivate(`class`: Class<*>, field: String, instance: Any? = null): Any? {
    return getPrivate(`class`.getDeclaredField(field))
}

fun setPrivateFinal(field: Field, instance: Any? = null, value: Any?) {
    val oldAccessible = @Suppress("DEPRECATION") field.isAccessible
    field.isAccessible = true
    val modifiersField = Field::class.java.getDeclaredField("modifiers")
    modifiersField.isAccessible = true
    val oldModifiers = modifiersField[field]
    modifiersField.setInt(field, field.modifiers and Modifier.FINAL.inv())
    field[instance] = value
    modifiersField.set(field, oldModifiers)
    modifiersField.isAccessible = false
    field.isAccessible = oldAccessible
}

fun setPrivateFinal(`class`: Class<*>, field: String, instance: Any? = null, value: Any?) {
    return setPrivateFinal(`class`.getDeclaredField(field), instance, value)
}

fun updatePrivateFinal(field: Field, instance: Any? = null, value: (Any?) -> Any?) {
    val oldAccessible = @Suppress("DEPRECATION") field.isAccessible
    field.isAccessible = true
    val modifiersField = Field::class.java.getDeclaredField("modifiers")
    modifiersField.isAccessible = true
    val oldModifiers = modifiersField.get(field)
    modifiersField.setInt(field, field.modifiers and Modifier.FINAL.inv())
    field[instance] = value(field[instance])
    modifiersField.set(field, oldModifiers)
    modifiersField.isAccessible = false
    field.isAccessible = oldAccessible
}

fun updatePrivateFinal(`class`: Class<*>, field: String, instance: Any? = null, value: (Any?) -> Any?) {
    return updatePrivateFinal(`class`.getDeclaredField(field), instance, value)
}
