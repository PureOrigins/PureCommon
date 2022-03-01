package it.pureorigins.common

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.MessageType
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.chat.ComponentSerializer
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.ChatType
import net.minecraft.server.level.ServerPlayer
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

typealias LegacyText = String
typealias SpigotText = Array<out BaseComponent>
typealias PaperText = Component
typealias MinecraftText = net.minecraft.network.chat.Component

data class MultiText(val json: String) {
    val spigot by lazy {
        spigotTextFromJson(json)
    }
    
    val paper by lazy {
        paperTextFromJson(json)
    }
    
    val minecraft by lazy {
        minecraftTextFromJson(json)
    }
}

@Suppress("DEPRECATION")
fun CommandSender.sendMessage(text: SpigotText?, position: ChatMessageType = ChatMessageType.SYSTEM, sender: UUID? = null) {
    if (text != null) {
        if (this is Player) {
            spigot().sendMessage(position, sender, *text)
        } else {
            spigot().sendMessage(sender, *text)
        }
    }
}

fun Audience.sendMessage(text: PaperText?, position: ChatMessageType = ChatMessageType.SYSTEM, sender: UUID? = null) {
    if (text != null) {
        when (position) {
            ChatMessageType.SYSTEM -> sendMessage(sender?.let { Identity.identity(it) } ?: Identity.nil(), text, MessageType.SYSTEM)
            ChatMessageType.CHAT -> sendMessage(sender?.let { Identity.identity(it) } ?: Identity.nil(), text, MessageType.CHAT)
            ChatMessageType.ACTION_BAR -> sendActionBar(text)
        }
    }
}

fun CommandSourceStack.sendMessage(text: SpigotText?, position: ChatMessageType = ChatMessageType.SYSTEM, sender: UUID? = null) {
    return bukkitSender.sendMessage(text, position, sender)
}

fun CommandSourceStack.sendMessage(text: PaperText?, position: ChatMessageType = ChatMessageType.SYSTEM, sender: UUID? = null) {
    return bukkitSender.sendMessage(text, position, sender)
}

fun ServerPlayer.sendMessage(text: MinecraftText?, position: ChatMessageType = ChatMessageType.SYSTEM, sender: UUID? = null) {
    if (text != null) {
        when (position) {
            ChatMessageType.SYSTEM -> sendMessage(text, ChatType.SYSTEM, sender)
            ChatMessageType.CHAT -> sendMessage(text, ChatType.CHAT, sender)
            ChatMessageType.ACTION_BAR -> sendMessage(text, ChatType.GAME_INFO, sender)
        }
    }
}

@Suppress("DEPRECATION")
fun broadcastMessage(text: SpigotText?) {
    if (text != null) {
        Bukkit.broadcast(*text)
    }
}

fun broadcastMessage(text: PaperText?) {
    if (text != null) {
        Bukkit.broadcast(text)
    }
}

fun broadcastMessage(text: PaperText?, permission: String) {
    if (text != null) {
        Bukkit.broadcast(text, permission)
    }
}

fun spigotTextFromJson(json: String): SpigotText = ComponentSerializer.parse(json)
fun paperTextFromJson(json: String): PaperText = GsonComponentSerializer.gson().deserialize(json)
fun minecraftTextFromJson(json: String): MinecraftText = net.minecraft.network.chat.Component.Serializer.fromJson(json) ?: error("empty string")
fun SpigotText.toJson(): String = ComponentSerializer.toString(*this)
fun PaperText.toJson(): String = GsonComponentSerializer.gson().serialize(this)
fun MinecraftText.toJson(): String = net.minecraft.network.chat.Component.Serializer.toJson(this)
fun SpigotText.toLegacyText(): LegacyText = BaseComponent.toLegacyText(*this)
fun PaperText.toLegacyText(legacyCharacter: Char = '§'): LegacyText = LegacyComponentSerializer.legacy(legacyCharacter).serialize(this)
fun SpigotText.toPlainText(): String = BaseComponent.toPlainText(*this)
fun PaperText.toPlainText(): String = PlainTextComponentSerializer.plainText().serialize(this)
fun MinecraftText.toPlainText(): String = this.string
fun LegacyText.toSpigotText(): SpigotText = TextComponent.fromLegacyText(this)
fun LegacyText.toPaperText(legacyCharacter: Char = '§'): PaperText = LegacyComponentSerializer.legacy(legacyCharacter).deserialize(this)
