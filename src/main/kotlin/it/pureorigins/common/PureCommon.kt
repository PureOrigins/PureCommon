package it.pureorigins.common

import org.bukkit.plugin.java.JavaPlugin

class PureCommon : JavaPlugin() {
    override fun onEnable() {
        val config = json.readFileAs(file("config.json"), DatabaseCommand.Config())
        overrideComponentSerializer()
        DatabaseCommand(this, config).register()
    }
}