package it.pureorigins.common

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import java.util.concurrent.Future

fun JavaPlugin.runTask(task: () -> Unit): BukkitTask = Bukkit.getScheduler().runTask(this, task)

fun JavaPlugin.runTaskAsynchronously(task: () -> Unit): BukkitTask =
    Bukkit.getScheduler().runTaskAsynchronously(this, task)

fun JavaPlugin.runTaskLater(delay: Long, task: () -> Unit): BukkitTask =
    Bukkit.getScheduler().runTaskLater(this, task, delay)

fun JavaPlugin.runTaskLaterAsynchronously(delay: Long, task: () -> Unit): BukkitTask =
    Bukkit.getScheduler().runTaskLaterAsynchronously(this, task, delay)

fun JavaPlugin.runTaskTimer(delay: Long, period: Long, task: () -> Unit): BukkitTask =
    Bukkit.getScheduler().runTaskTimer(this, task, delay, period)

fun JavaPlugin.runTaskTimerAsynchronously(delay: Long, period: Long, task: () -> Unit): BukkitTask =
    Bukkit.getScheduler().runTaskTimerAsynchronously(this, task, delay, period)

fun <T> JavaPlugin.callSync(task: () -> T): Future<T> = Bukkit.getScheduler().callSyncMethod(this, task)