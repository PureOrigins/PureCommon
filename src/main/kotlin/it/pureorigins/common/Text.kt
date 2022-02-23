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
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

typealias LegacyText = String
typealias SpigotText = Array<out BaseComponent>
typealias PaperText = Component

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
fun SpigotText.toJson(): String = ComponentSerializer.toString(*this)
fun PaperText.toJson(): String = GsonComponentSerializer.gson().serialize(this)
fun SpigotText.toLegacyText(): LegacyText = BaseComponent.toLegacyText(*this)
fun PaperText.toLegacyText(legacyCharacter: Char = '§'): LegacyText = LegacyComponentSerializer.legacy(legacyCharacter).serialize(this)
fun SpigotText.toPlainText(): String = BaseComponent.toPlainText(*this)
fun PaperText.toPlainText(): String = PlainTextComponentSerializer.plainText().serialize(this)
fun LegacyText.toSpigotText(): SpigotText = TextComponent.fromLegacyText(this)
fun LegacyText.toPaperText(legacyCharacter: Char = '§'): PaperText = LegacyComponentSerializer.legacy(legacyCharacter).deserialize(this)
