package it.pureorigins.common

import io.netty.channel.ChannelHandler
import io.netty.util.concurrent.Future
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.ComponentUtils
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
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

val server: DedicatedServer get() = Bukkit.getServer().nms
val serverCommandSource: CommandSourceStack get() = server.createCommandSourceStack()

val Player.nms: ServerPlayer get() = (this as CraftPlayer).handle as ServerPlayer
val World.nms: ServerLevel get() = (this as CraftWorld).handle as ServerLevel
val Entity.nms: net.minecraft.world.entity.Entity get() = (this as CraftEntity).handle
val Server.nms: DedicatedServer get() = (this as CraftServer).server

fun ServerPlayer.sendPacket(packet: Packet<*>) = connection.send(packet)
fun ServerPlayer.sendPacket(packet: Packet<*>, callback: (Future<*>) -> Unit) = connection.send(packet, callback)

fun Player.sendPacket(packet: Packet<*>) = nms.sendPacket(packet)
fun Player.sendPacket(packet: Packet<*>, callback: (Future<*>) -> Unit) = nms.sendPacket(packet, callback)

fun Player.registerPacketHandler(handler: ChannelHandler, name: String = handler.javaClass.name) {
    nms.connection.connection.channel.pipeline()
        .addLast(name, handler)
}

fun Player.unregisterPacketHandler(handler: ChannelHandler) {
    nms.connection.connection.channel.eventLoop().submit {
        nms.connection.connection.channel.pipeline().remove(handler)
    }
}

fun JavaPlugin.registerPacketHandler(handler: ChannelHandler) {
    registerEvents(object : Listener {
        @EventHandler
        fun onPlayerJoin(event: PlayerJoinEvent) {
            event.player.registerPacketHandler(handler)
        }
    
        @EventHandler
        fun onPlayerQuit(event: PlayerQuitEvent) {
            event.player.unregisterPacketHandler(handler)
        }
    })
}

fun CommandSourceStack?.updateForEntity(text: Text?, sender: net.minecraft.world.entity.Entity?, depth: Int = 0): Text? {
    return ComponentUtils.updateForEntity(this, Optional.ofNullable(text), sender, depth).orElse(null)
}