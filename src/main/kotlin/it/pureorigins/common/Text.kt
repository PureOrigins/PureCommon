package it.pureorigins.common

import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.entity.Player
import java.util.*

typealias Text = Array<BaseComponent>

fun Player.sendMessage(text: Text?, position: ChatMessageType = ChatMessageType.SYSTEM, sender: UUID? = null) {
    if (text != null) {
        spigot().sendMessage(position, sender, *text)
    }
}
