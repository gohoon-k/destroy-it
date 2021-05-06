package com.herokun.plugins

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryType
import org.bukkit.plugin.java.JavaPlugin

class InventoryDetector(private val plugin: JavaPlugin): Listener {

    @EventHandler
    fun onInventoryClicked(event: org.bukkit.event.inventory.InventoryClickEvent){
        if(plugin !is Main) return

        if(plugin.data.settings.enabled) {
            if(event.clickedInventory?.type != InventoryType.PLAYER) return
            val validSlot = when (event.slotType) {
                InventoryType.SlotType.CONTAINER, InventoryType.SlotType.QUICKBAR, InventoryType.SlotType.ARMOR -> true
                else -> false
            }
            if (validSlot && !plugin.data.unlockedInventories[event.slot]) {
                event.isCancelled = true
            }
        }
    }

}
