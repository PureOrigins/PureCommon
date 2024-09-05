package it.pureorigins.common

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType.string
import com.mojang.brigadier.suggestion.SuggestionProvider
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class PureCommon : JavaPlugin() {
    override fun onEnable() {
        val config = json.readFileAs(file("config.json"), DatabaseCommand.Config())
        overrideComponentSerializer()
        DatabaseCommand(this, config).register()

        //command example
        val command = Commands.literal("new-command").requiresPermission("purecommon.new-command").executes {
            it.source.sendNullableMessage("Hello, world!")
            Command.SINGLE_SUCCESS
        }.then(Commands.argument("name", ArgumentTypes.player()).suggests { context, builder ->
            builder.suggest("Notch").suggest("pippo").buildFuture()
        }).executes {
            return@executes Command.SINGLE_SUCCESS
        }
    }
}