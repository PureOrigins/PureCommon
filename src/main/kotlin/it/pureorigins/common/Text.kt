package it.pureorigins.common

import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.bukkit.command.CommandSender

typealias Text = Component

fun textFromJson(json: String): Text = GsonComponentSerializer.gson().deserialize(json)
fun Text.toJson(): String = GsonComponentSerializer.gson().serialize(this)
fun Text.toPlainText(): String = this.toString()
fun String.toText(): Text = Component.text(this)

fun CommandSender.sendNullableMessage(text: Text?) {
    if (text != null) {
        sendMessage(text)
    }
}

fun CommandSourceStack.sendNullableMessage(text: Text?) {
    if (text != null) {
        sender.sendMessage(text)
    }
}
