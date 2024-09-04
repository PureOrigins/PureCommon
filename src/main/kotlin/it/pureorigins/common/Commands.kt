package it.pureorigins.common

import com.mojang.brigadier.Command.SINGLE_SUCCESS
import com.mojang.brigadier.Message
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin


fun registerCommand(
    manager: LifecycleEventManager<Plugin>,
    literal: LiteralArgumentBuilder<CommandSourceStack>,
    vararg aliases: String
) {
    manager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
        {
            val dispatcher = event.registrar().dispatcher
            val node = dispatcher.register(LiteralArgumentBuilder.literal<CommandSourceStack>(literal.literal))
            for (alias in aliases) {
                dispatcher.register(
                    LiteralArgumentBuilder.literal<CommandSourceStack>(alias).redirect(node)
                )
            }
        }
    }
}

inline fun literal(name: String, block: LiteralArgumentBuilder<CommandSourceStack>.() -> Unit) =
    LiteralArgumentBuilder.literal<CommandSourceStack>(name).apply(block)!!

inline fun <T> argument(
    name: String,
    type: ArgumentType<T>,
    block: RequiredArgumentBuilder<CommandSourceStack, T>.() -> Unit
) =
    RequiredArgumentBuilder.argument<CommandSourceStack, T>(name, type).apply(block)!!

inline fun RequiredArgumentBuilder<CommandSourceStack, *>.suggests(crossinline block: CommandContext<CommandSourceStack>.(SuggestionsBuilder) -> Unit) =
    suggests { context, builder -> context.block(builder); builder.buildFuture() }!!

inline fun ArgumentBuilder<CommandSourceStack, *>.success(crossinline block: CommandContext<CommandSourceStack>.() -> Unit) =
    executes { block(it); SINGLE_SUCCESS }!!

fun ArgumentBuilder<CommandSourceStack, *>.requiresPermission(name: String, orElsePermissionLevel: Int = 2) =
    requires(requirement.and { it.hasPermission(orElsePermissionLevel, name) })!!

inline fun RequiredArgumentBuilder<CommandSourceStack, *>.suggestions(crossinline block: CommandContext<CommandSourceStack>.() -> Iterable<String>) =
    suggests { builder ->
        SharedSuggestionProvider.suggest(block(), builder)
    }

inline fun <T> RequiredArgumentBuilder<CommandSourceStack, *>.suggestions(
    noinline suggestions: (T) -> String,
    noinline tooltips: (T) -> Message,
    crossinline block: CommandContext<CommandSourceStack>.() -> Iterable<T>
) =
    suggests { builder ->
        SharedSuggestionProvider.suggest(block(), builder, suggestions, tooltips)
    }

val CommandSourceStack.player get() = bukkitSender as? Player ?: throw CommandSourceStack.ERROR_NOT_PLAYER.create()
