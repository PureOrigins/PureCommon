package it.pureorigins.common

import java.lang.reflect.Field
import java.lang.reflect.Modifier

fun getPrivate(`class`: Class<*>, field: String, instance: Any? = null): Any? {
    with(`class`.getDeclaredField(field)) {
        val oldAccessible = @Suppress("DEPRECATION") isAccessible
        isAccessible = true
        val value = get(instance)
        isAccessible = oldAccessible
        return value
    }
}

fun setPrivateFinal(`class`: Class<*>, field: String, instance: Any? = null, value: Any?) {
    with(`class`.getDeclaredField(field)) {
        val oldAccessible = @Suppress("DEPRECATION") isAccessible
        isAccessible = true
        val modifiersField = Field::class.java.getDeclaredField("modifiers")
        modifiersField.isAccessible = true
        val oldModifiers = modifiersField.get(this)
        modifiersField.setInt(this, modifiers and Modifier.FINAL.inv())
        set(instance, value)
        modifiersField.set(this, oldModifiers)
        modifiersField.isAccessible = false
        isAccessible = oldAccessible
    }
}

fun updatePrivateFinal(`class`: Class<*>, field: String, instance: Any? = null, value: (Any?) -> Any?) {
    with(`class`.getDeclaredField(field)) {
        val oldAccessible = @Suppress("DEPRECATION") isAccessible
        isAccessible = true
        val modifiersField = Field::class.java.getDeclaredField("modifiers")
        modifiersField.isAccessible = true
        val oldModifiers = modifiersField.get(this)
        modifiersField.setInt(this, modifiers and Modifier.FINAL.inv())
        set(instance, value(get(instance)))
        modifiersField.set(this, oldModifiers)
        modifiersField.isAccessible = false
        isAccessible = oldAccessible
    }
}