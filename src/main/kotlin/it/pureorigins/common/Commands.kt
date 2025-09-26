package it.pureorigins.common

import com.mojang.brigadier.Command
import com.mojang.brigadier.Message
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.bukkit.plugin.java.JavaPlugin


fun JavaPlugin.registerCommand(
    literal: LiteralCommandNode<CommandSourceStack>,
    vararg aliases: String,
    description: String? = null
) {
    this.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
        val dispatcher = event.registrar()
        dispatcher.register(literal, description, listOf(*aliases))
    }
}

fun literal(literal: String, builder: LiteralArgumentBuilder<CommandSourceStack>.() -> Unit) = Commands.literal(literal).apply(builder).build()!!

fun <S, B : ArgumentBuilder<S, B>, R> ArgumentBuilder<S, B>.argument(
    name: String,
    type: ArgumentType<R>,
    block: RequiredArgumentBuilder<S, R>.() -> Unit
): ArgumentBuilder<S, B> =
    then(RequiredArgumentBuilder.argument<S, R>(name, type).also(block))


fun ArgumentBuilder<CommandSourceStack, *>.requiresPermission(name: String, orElse: PermissionDefault = PermissionDefault.OP) =
    requires(requirement.and { it.sender.hasPermission(Permission(name, null, orElse)) })!!

inline fun ArgumentBuilder<CommandSourceStack, *>.success(crossinline block: CommandContext<CommandSourceStack>.() -> Unit) =
    executes { block(it); Command.SINGLE_SUCCESS }!!

inline fun RequiredArgumentBuilder<CommandSourceStack, *>.suggestions(crossinline block: CommandContext<CommandSourceStack>.() -> Iterable<String>) =
    suggests { context, builder ->
        context.block().forEach { builder.suggest(it) }
        builder.buildFuture()
    }!!

inline fun <T> RequiredArgumentBuilder<CommandSourceStack, *>.suggestions(
    noinline suggestions: (T) -> String,
    noinline tooltips: (T) -> Message,
    crossinline block: CommandContext<CommandSourceStack>.() -> Iterable<T>
) =
    suggests { context, builder ->
        context.block().forEach { builder.suggest(suggestions(it), tooltips(it)) }
        builder.buildFuture()
    }