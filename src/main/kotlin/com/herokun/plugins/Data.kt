package com.herokun.plugins

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.Serializable
import java.util.*
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

class Data(
    var destroyTargets: List<Material>,
    var destroyedBlocks: MutableSet<String>,
    var unlockedInventories: MutableList<Boolean>,
    var rewardsStates: MutableMap<UUID, MutableList<Int>>,
    var stats: MutableMap<String, Stat>,
    var settings: Settings
) : Serializable {

    companion object {

        const val serialVersionUID = -192748393820983L

        fun loadData(path: String): Data? {
            return try{
                val boo = BukkitObjectInputStream(GZIPInputStream(FileInputStream(path)))
                val result = boo.readObject() as Data
                boo.close()
                result
            }catch(e: IOException){
                e.printStackTrace()
                null
            }
        }

    }

    fun saveData(path: String): Boolean {
        return try{
            val boo = BukkitObjectOutputStream(GZIPOutputStream(FileOutputStream(path)))
            boo.writeObject(this)
            boo.close()
            true
        }catch(e: IOException){
            e.printStackTrace()
            false
        }
    }

    data class Settings(
        var enabled: Boolean,
        var answerShowing: MutableMap<UUID, Boolean>,
        var answerInventoryBackup: MutableMap<UUID, List<ItemStack?>?>
    ): Serializable

    data class Stat(
        val playerName: String,
        var blocks: MutableList<String>,
        var inventories: MutableList<String>
    ): Serializable

}
