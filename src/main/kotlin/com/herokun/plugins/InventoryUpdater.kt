package com.herokun.plugins

import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable

class InventoryUpdater(
    private val plugin: JavaPlugin
) : BukkitRunnable() {

    companion object {

        fun getInventoryPosition(slot: Int): String {
            return if (slot < 36) {
                "${slot / 9 + 1}, ${slot % 9 + 1}"
            } else {
                when (slot) {
                    36 -> "boots"
                    37 -> "legs"
                    38 -> "chest"
                    39 -> "helmet"
                    40 -> "left_hand"
                    else -> "???"
                }
            }
        }

        fun getInventoryPositionByName(name: String): Int {
            return when (name) {
                "boots" -> 36
                "legs" -> 37
                "chest" -> 38
                "helmet" -> 39
                "left_hand" -> 40
                else -> {
                    try {
                        val args = name.split(" ")
                        (args[0].toInt() - 1) * 9 + (args[1].toInt() - 1)
                    } catch (e: Exception) {
                        -1
                    }
                }
            }
        }

    }

    override fun run() {
        if ((plugin as Main).data.settings.enabled) {
            plugin.lockInventory()
        }
    }

}
