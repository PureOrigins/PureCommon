package it.pureorigins.common

import com.google.gson.*
import io.netty.channel.ChannelHandler
import io.netty.util.concurrent.Future
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentUtils
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.network.protocol.Packet
import net.minecraft.server.dedicated.DedicatedServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.GsonHelper
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
import java.lang.reflect.Type
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
fun registerComponentSerializer(serializer: Any) {
    componentSerializerWrapper.register(serializer)
}
internal fun overrideComponentSerializer() {
    val gsonField = Component.Serializer::class.java.declaredFields.first { it.type == Gson::class.java }
    val oldGson = unsafeGetStaticField(gsonField) as Gson
    componentSerializerWrapper = ComponentSerializerWrapper(oldGson)
    val newGson = oldGson.newBuilder().registerTypeAdapter(Component::class.java, componentSerializerWrapper).create()
    unsafeSetStaticField(gsonField, newGson)
}

private lateinit var componentSerializerWrapper: ComponentSerializerWrapper
private class ComponentSerializerWrapper(
    private val gson: Gson,
    private val serializers: MutableList<Any> = mutableListOf()
) : JsonDeserializer<MutableComponent>, JsonSerializer<Component> {
    fun register(serializer: Any) {
        serializers.add(serializer)
    }
    
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): MutableComponent {
        var component: MutableComponent? = null
        for (serializer in serializers) {
            if (serializer is JsonDeserializer<*>) {
                component = serializer.deserialize(json, typeOfT, context) as MutableComponent?
                if (component != null) break
            }
        }
        return if (component != null && json is JsonObject) {
            if (json.has("extra")) {
                val extra = GsonHelper.getAsJsonArray(json, "extra")
                if (extra.size() <= 0) {
                    throw JsonParseException("Unexpected empty array of components")
                }
                extra.forEach {
                    component.append(context.deserialize<MutableComponent>(it, typeOfT))
                }
            }
            component.style = gson.fromJson(json, Style::class.java)
            component
        } else {
            gson.fromJson(json, typeOfT)
        }
    }
    
    override fun serialize(src: Component, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        var json: JsonElement = JsonNull.INSTANCE
        for (serializer in serializers) {
            if (serializer is JsonSerializer<*>) {
                @Suppress("UNCHECKED_CAST")
                json = (serializer as JsonSerializer<Component>).serialize(src, typeOfSrc, context)
                if (json != JsonNull.INSTANCE) break
            }
        }
        return if (json != JsonNull.INSTANCE && json is JsonObject) {
            if (!src.style.isEmpty) {
                val jsonStyle = gson.toJsonTree(src.style).asJsonObject
                jsonStyle.entrySet().forEach { (key, value) ->
                    json.add(key, value)
                }
            }
            
            if (src.siblings.isNotEmpty()) {
                val jsonSiblings = JsonArray()
                for (sibling in src.siblings) {
                    jsonSiblings.add(context.serialize(sibling))
                }
                json.add("extra", jsonSiblings)
            }
            json
        } else {
            gson.toJsonTree(src, typeOfSrc)
        }
    }
}