package it.pureorigins.common

import net.minecraft.server.level.ServerEntity
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

val Player.nms get() = nms() as ServerPlayer
val World.nms get() = nms() as ServerLevel
val Entity.nms get() = nms() as ServerEntity

fun Any.nms(methodName: String = "getHandle"): Any {
    return javaClass.getDeclaredMethod(methodName).invoke(this)
}