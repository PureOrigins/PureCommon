package it.pureorigins.common

import io.netty.util.concurrent.Future
import net.minecraft.network.protocol.Packet
import net.minecraft.server.dedicated.DedicatedServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.craftbukkit.v1_18_R2.CraftServer
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftEntity
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

val server: DedicatedServer = Bukkit.getServer().nms

val Player.nms: ServerPlayer get() = (this as CraftPlayer).player as ServerPlayer
val World.nms: ServerLevel get() = (this as CraftWorld).handle as ServerLevel
val Entity.nms: net.minecraft.world.entity.Entity get() = (this as CraftEntity).handle
val Server.nms: DedicatedServer get() = (this as CraftServer).server

fun ServerPlayer.sendPacket(packet: Packet<*>) = connection.send(packet)
fun ServerPlayer.sendPacket(packet: Packet<*>, callback: (Future<*>) -> Unit) = connection.send(packet, callback)

fun Player.sendPacket(packet: Packet<*>) = nms.sendPacket(packet)
fun Player.sendPacket(packet: Packet<*>, callback: (Future<*>) -> Unit) = nms.sendPacket(packet, callback)
