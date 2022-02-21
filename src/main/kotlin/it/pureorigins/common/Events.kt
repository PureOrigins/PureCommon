package it.pureorigins.common

import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

fun JavaPlugin.registerEvents(listener: Listener) {
    Bukkit.getPluginManager().registerEvents(listener, this)
}
