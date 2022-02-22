package it.pureorigins.common

import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

typealias Text = Array<BaseComponent>

fun CommandSender.sendMessage(text: Text?, position: ChatMessageType = ChatMessageType.SYSTEM, sender: UUID? = null) {
    if (text != null) {
        if (this is Player) {
            spigot().sendMessage(position, sender, *text)
        } else {
            spigot().sendMessage(sender, *text)
        }
    }
}
