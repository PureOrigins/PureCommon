package it.pureorigins.common

import io.papermc.paper.adventure.AdventureComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.chat.ComponentSerializer
import net.minecraft.Util.NIL_UUID
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.ChatType
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer
import java.util.*

typealias LegacyText = String
typealias SpigotText = Array<out BaseComponent>
typealias PaperText = Component
typealias Text = net.minecraft.network.chat.Component
typealias MutableText = net.minecraft.network.chat.MutableComponent

fun spigotTextFromJson(json: String): SpigotText = ComponentSerializer.parse(json)
fun paperTextFromJson(json: String): PaperText = GsonComponentSerializer.gson().deserialize(json)
fun textFromJson(json: String): MutableText = net.minecraft.network.chat.Component.Serializer.fromJson(json) ?: error("empty string")
fun SpigotText.toJson(): String = ComponentSerializer.toString(*this)
fun PaperText.toJson(): String = GsonComponentSerializer.gson().serialize(this)
fun Text.toJson(): String = net.minecraft.network.chat.Component.Serializer.toJson(this)
fun SpigotText.toLegacyText(): LegacyText = BaseComponent.toLegacyText(*this)
fun PaperText.toLegacyText(legacyCharacter: Char = '§'): LegacyText = LegacyComponentSerializer.legacy(legacyCharacter).serialize(this)
fun SpigotText.toPlainText(): String = BaseComponent.toPlainText(*this)
fun PaperText.toPlainText(): String = PlainTextComponentSerializer.plainText().serialize(this)
fun Text.toPlainText(): String = this.string
fun LegacyText.toSpigotText(): SpigotText = TextComponent.fromLegacyText(this)
fun LegacyText.toPaperText(legacyCharacter: Char = '§'): PaperText = LegacyComponentSerializer.legacy(legacyCharacter).deserialize(this)
fun PaperText.toText(): Text = AdventureComponent(this)


fun CommandSourceStack.sendNullableMessage(text: Text?, sender: UUID = NIL_UUID) {
    if (text != null) {
        source.sendMessage(text, sender)
    }
}

fun ServerPlayer.sendNullableMessage(text: Text?, position: ChatType = ChatType.SYSTEM, sender: UUID = NIL_UUID) {
    if (text != null) {
        sendMessage(text, position, sender)
    }
}

fun Player.sendNullableMessage(text: Text?, position: ChatType = ChatType.SYSTEM, sender: UUID = NIL_UUID) {
    if (text != null) {
        if (this is ServerPlayer) {
            sendMessage(text, position, sender)
        } else {
            sendMessage(text, sender)
        }
    }
}

fun org.bukkit.entity.Player.sendNullableMessage(text: Text?, position: ChatType = ChatType.SYSTEM, sender: UUID = NIL_UUID) {
    if (text != null) {
        (this as CraftPlayer).handle.sendNullableMessage(text, position, sender)
    }
}