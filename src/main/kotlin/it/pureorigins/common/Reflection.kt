package it.pureorigins.common

import java.lang.invoke.MethodHandles
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
    return getPrivate(`class`.getDeclaredField(field), instance)
}

fun setPrivateFinal(field: Field, instance: Any? = null, value: Any?) {
    try {
        val oldAccessible = @Suppress("DEPRECATION") field.isAccessible
        field.isAccessible = true
        val modifiersField = MethodHandles.privateLookupIn(Field::class.java, MethodHandles.lookup()).findVarHandle(Field::class.java, "modifiers", Integer.TYPE)
        val oldModifiers = modifiersField[field]
        modifiersField.set(field, field.modifiers and Modifier.FINAL.inv())
        field[instance] = value
        modifiersField.set(field, oldModifiers)
        field.isAccessible = oldAccessible
    } catch (e: IllegalAccessException) {
        System.err.println("This function requires '--add-opens java.base/java.lang.reflect=ALL-UNNAMED' to function properly.")
        throw e
    }
}

fun setPrivateFinal(`class`: Class<*>, field: String, instance: Any? = null, value: Any?) {
    return setPrivateFinal(`class`.getDeclaredField(field), instance, value)
}

fun updatePrivateFinal(field: Field, instance: Any? = null, value: (Any?) -> Any?) {
    try {
        val oldAccessible = @Suppress("DEPRECATION") field.isAccessible
        field.isAccessible = true
        val modifiersField = MethodHandles.privateLookupIn(Field::class.java, MethodHandles.lookup()).findVarHandle(Field::class.java, "modifiers", Integer.TYPE)
        val oldModifiers = modifiersField.get(field)
        modifiersField.set(field, field.modifiers and Modifier.FINAL.inv())
        field[instance] = value(field[instance])
        modifiersField.set(field, oldModifiers)
        field.isAccessible = oldAccessible
    } catch (e: IllegalAccessException) {
        System.err.println("This function requires '--add-opens java.base/java.lang.reflect=ALL-UNNAMED' to function properly.")
        throw e
    }
}

fun updatePrivateFinal(`class`: Class<*>, field: String, instance: Any? = null, value: (Any?) -> Any?) {
    return updatePrivateFinal(`class`.getDeclaredField(field), instance, value)
}
