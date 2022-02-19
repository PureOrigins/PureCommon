package it.pureorigins.common

import com.mojang.brigadier.Command.SINGLE_SUCCESS
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.commands.CommandListenerWrapper
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_18_R1.CraftServer


fun registerCommand(literal: LiteralArgumentBuilder<CommandListenerWrapper>, vararg aliases: String) {
    val dedicatedServer = (Bukkit.getServer() as CraftServer).server
    val dispatcher = dedicatedServer.vanillaCommandDispatcher.a()
    val node = dispatcher.register(literal)
    for (alias in aliases) {
        dispatcher.register(LiteralArgumentBuilder.literal<CommandListenerWrapper>(alias).redirect(node))
    }
}

inline fun literal(name: String, block: LiteralArgumentBuilder<CommandListenerWrapper>.() -> Unit) =
    LiteralArgumentBuilder.literal<CommandListenerWrapper>(name).apply(block)!!

inline fun <T> argument(name: String, type: ArgumentType<T>, block: RequiredArgumentBuilder<CommandListenerWrapper, T>.() -> Unit) =
    RequiredArgumentBuilder.argument<CommandListenerWrapper, T>(name, type).apply(block)!!

inline fun RequiredArgumentBuilder<CommandListenerWrapper, *>.suggests(crossinline block: CommandContext<CommandListenerWrapper>.(SuggestionsBuilder) -> Unit) =
    suggests { context, builder -> context.block(builder); builder.buildFuture() }!!

inline fun ArgumentBuilder<CommandListenerWrapper, *>.success(crossinline block: CommandContext<CommandListenerWrapper>.() -> Unit) =
    executes { block(it); SINGLE_SUCCESS }!!

fun ArgumentBuilder<CommandListenerWrapper, *>.requiresPermission(name: String, orElsePermissionLevel: Int = 2) =
    requires(requirement.and { it.hasPermission(orElsePermissionLevel, name) })!!

inline fun RequiredArgumentBuilder<CommandListenerWrapper, *>.suggestions(crossinline block: CommandContext<CommandListenerWrapper>.() -> Iterable<String>) =
    suggests { builder ->
        val partial = getArgument(name, String::class.java)
        block().forEach { if (it.startsWith(partial, ignoreCase = true)) builder.suggest(it) }
    }