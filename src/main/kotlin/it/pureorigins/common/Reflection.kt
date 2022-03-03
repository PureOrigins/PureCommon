package it.pureorigins.common

import sun.misc.Unsafe
import java.lang.reflect.Field

fun unsafeGetField(field: Field, instance: Any): Any? {
    return unsafe.getObject(instance, unsafe.objectFieldOffset(field))
}

fun unsafeGetField(`class`: Class<*>, field: String, instance: Any): Any? {
    return unsafeGetField(`class`.getDeclaredField(field), instance)
}

fun unsafeGetStaticField(field: Field): Any? {
    return unsafe.getObject(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field))
}

fun unsafeGetStaticField(`class`: Class<*>, field: String): Any? {
    return unsafeGetStaticField(`class`.getDeclaredField(field))
}

fun unsafeSetField(field: Field, instance: Any, value: Any?) {
    unsafe.putObject(instance, unsafe.objectFieldOffset(field), value)
}

fun unsafeSetField(`class`: Class<*>, field: String, instance: Any, value: Any?) {
    unsafeSetField(`class`.getDeclaredField(field), instance, value)
}

fun unsafeSetStaticField(field: Field, value: Any?) {
    unsafe.putObject(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field), value)
}

fun unsafeSetStaticField(`class`: Class<*>, field: String, value: Any?) {
    return unsafeSetStaticField(`class`.getDeclaredField(field), value)
}

private val unsafe by lazy {
    val field = Unsafe::class.java.getDeclaredField("theUnsafe")
    field.isAccessible = true
    field[null] as Unsafe
}
