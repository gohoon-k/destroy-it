package com.herokun.plugins

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

class PlayerDetector(private val plugin: JavaPlugin): Listener {

    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent){
        if(!(plugin as Main).data.stats.containsKey(e.player.uniqueId.toString())){
            plugin.data.stats[e.player.uniqueId.toString()] = Data.Stat(e.player.name, mutableListOf(), mutableListOf())
        }

        if(!plugin.data.rewardsStates.containsKey(e.player.uniqueId)){
            plugin.data.rewardsStates[e.player.uniqueId] = MutableList(41) {
                if(plugin.data.unlockedInventories[it]) -1 else 0
            }
        }

        val unlockedInventories = mutableListOf<String>()
        e.player.inventory.forEachIndexed { index, _ ->
            if(plugin.data.rewardsStates[e.player.uniqueId]?.get(index) == 1){
                e.player.inventory.setItem(index, ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1))

                unlockedInventories.add("(${InventoryUpdater.getInventoryPosition(index)})")

                if (plugin.data.rewardsStates.containsKey(e.player.uniqueId)) {
                    plugin.data.rewardsStates[e.player.uniqueId]?.set(index, -1)
                }
            }
        }

        if(unlockedInventories.isNotEmpty()) {
            e.player.sendMessage("" + ChatColor.YELLOW + "Other players unlocked these slots")
            e.player.sendMessage("" + ChatColor.YELLOW + "while you are not in server.")
            e.player.sendMessage("" + ChatColor.YELLOW + "Check out these slots: ")
            e.player.sendMessage("->" + ChatColor.GOLD + unlockedInventories.joinToString(", "))
        }
    }

}
