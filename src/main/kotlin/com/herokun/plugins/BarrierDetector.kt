package com.herokun.plugins

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerDropItemEvent

class BarrierDetector: Listener {

    @EventHandler
    fun onBarrierPlaced(event: BlockPlaceEvent){

        if(event.block.type == Material.BARRIER){
            event.isCancelled = true
        }

    }

    @EventHandler
    fun onBarrierDropped(event: PlayerDropItemEvent){

        if(event.itemDrop.itemStack.type == Material.BARRIER){
            event.isCancelled = true
        }

    }

}
