package com.herokun.plugins

import org.bukkit.*
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.plugin.java.JavaPlugin

class DestroyDetector(private val plugin: JavaPlugin): Listener {

    @EventHandler
    fun onBlockBreak(e: BlockBreakEvent){
        val main = plugin as Main
        if(!main.data.settings.enabled) return

        main.data.destroyedBlocks.add(e.block.type.name)

        if(main.data.destroyTargets.contains(e.block.type)){
            val index = main.data.destroyTargets.indexOf(e.block.type)
            if(!main.data.unlockedInventories[index]){
                main.data.stats[e.player.uniqueId.toString()]!!.blocks.add(e.block.type.name)
                main.data.stats[e.player.uniqueId.toString()]!!.inventories.add(InventoryUpdater.getInventoryPosition(index))
                main.unlockInventory(index)

                printMessage(e, index)
                createFireworks(e)
            }
        }

    }

    private fun printMessage(e: BlockBreakEvent, slot: Int){
        plugin.server.broadcastMessage(
            "" +
                    ChatColor.DARK_GREEN +
                    e.player.name +
                    ChatColor.RESET +
                    " has unlocked inventory[${InventoryUpdater.getInventoryPosition(slot)}]"
        )
        plugin.server.broadcastMessage(
            "by breaking " +
                    ChatColor.GOLD +
                    e.block.type.name +
                    ChatColor.RESET +
                    "!!"
        )
    }

    private fun createFireworks(e: BlockBreakEvent){
        //{LifeTime:20,FireworksItem:{id:"minecraft:firework_rocket",Count:1,tag:{Fireworks:{Flight:100,Explosions:[{Type:4,Colors:[I; 16777045]},{Type:4,Colors:[I; 16755200]}]}}}}
        val firework =
            e.player.world.spawnEntity(
                Location(e.player.world, e.block.location.x + 0.5, e.block.location.y + 0.5, e.block.location.z + 0.5),
                EntityType.FIREWORK
            ) as Firework
        val fireworkMeta = firework.fireworkMeta
        fireworkMeta.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.STAR).withColor(Color.YELLOW).build())
        fireworkMeta.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.STAR).withColor(Color.ORANGE).build())
        fireworkMeta.power = 0

        firework.fireworkMeta = fireworkMeta
    }

}
