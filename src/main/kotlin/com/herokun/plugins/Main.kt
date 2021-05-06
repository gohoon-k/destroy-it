package com.herokun.plugins

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.command.BlockCommandSender
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.plugin.java.JavaPlugin
import java.io.FileNotFoundException
import java.util.*
import java.util.logging.Level
import kotlin.random.Random

class Main : JavaPlugin() {

    private var _data: Data? = null
    val data get() = _data!!

    override fun onEnable() {
        super.onEnable()

        // print activated log
        server.logger.log(Level.ALL, "Destroy It plugin is now active")

        // register command: /destroy_it
        this.getCommand("destroy_it")?.setExecutor(this)
        this.getCommand("destroy_it")?.tabCompleter = this

        // load data with given file.
        try {
            _data = Data.loadData(Constants.DATA_PATH)
        } catch (e: FileNotFoundException) {
            server.logger.log(Level.WARNING, "Unable to find data file. Creating...")
        }
        // if file is null, initialize it
        if (_data == null) initData()

        // register inventory updater BukkitRunnable to runtime
        InventoryUpdater(this).runTaskTimer(this, 0, 1)

        // register event listeners: PlayerDetector, BlockDestroyDetector
        server.pluginManager.registerEvents(PlayerDetector(this), this)
        server.pluginManager.registerEvents(DestroyDetector(this), this)
        server.pluginManager.registerEvents(BarrierDetector(), this)
        server.pluginManager.registerEvents(InventoryDetector(this), this)

        // Initialize Legacy Material Support.
        //ItemStack(Material.BARRIER).data?.itemType?.name

    }

    override fun onDisable() {
        super.onDisable()

        // save all data to file
        data.saveData(Constants.DATA_PATH)

        // print deactivated log
        Bukkit.getConsoleSender().sendMessage("DestroyIt plugin is now deactivated")
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        // run only command sender is player and command name is 'destroy_it'
        if (sender is Player && label == "destroy_it") {
            if (args.isEmpty()) {
                // destroy_it <enter>
                sender.sendMessage("" + ChatColor.RED + "No value passed for: <operation>")
                sender.sendMessage("" + ChatColor.GRAY + "This can be one of these values: [ reset | enabled | action ]")
                return true
            }

            when (args[0]) {
                "reset" -> {
                    if (args.size == 1) {
                        // destroy_it reset <enter>
                        initData()
                        data.settings.enabled = false
                        sender.sendMessage("" + ChatColor.GRAY + "reset destroy it plugin complete, enabled: false")
                    } else {
                        if (args[1] == "true") {
                            // destroy_it reset true <enter>
                            initData()
                            data.settings.enabled = true
                            sender.sendMessage("" + ChatColor.GRAY + "reset destroy it plugin complete, enabled: true")
                        } else {
                            // destroy_it reset invalid_value <enter>
                            sender.sendMessage("" + ChatColor.RED + "Invalid value for <enabled>")
                            sender.sendMessage("" + ChatColor.GRAY + "Possible values: [ true | <no_value> ]")
                        }
                    }
                }
                "enabled" -> {
                    if (args.size == 1) {
                        // destroy_it enabled <enter>
                        sender.sendMessage("" + ChatColor.GRAY + "Destroy It plugin is now ${if (data.settings.enabled) "enabled" else "disabled"}")
                    } else {
                        when (args[1]) {
                            "true" -> {
                                // destroy_it enabled true <enter>
                                data.settings.enabled = true
                                sender.sendMessage("" + ChatColor.GRAY + "Destroy It plugin is enabled")
                            }
                            "false" -> {
                                // destroy_it enabled false <enter>
                                data.settings.enabled = false
                                sender.sendMessage("" + ChatColor.GRAY + "Destroy It plugin is disabled")
                            }
                            else -> {
                                // destroy_it enabled invalid_value <enter>
                                sender.sendMessage("" + ChatColor.RED + "Invalid value for <enabled>")
                                sender.sendMessage("" + ChatColor.GRAY + "Possible values: [ true | false ]")
                            }
                        }
                    }
                }
                "action" -> {
                    // do not execute further if not data.settings.enabled
                    if (!data.settings.enabled) {
                        sender.sendMessage("" + ChatColor.RED + "Destroy It plugin is not enabled!!")
                        return true
                    }

                    if (args.size == 1) {
                        // destroy_it action <enter>
                        sender.sendMessage("" + ChatColor.RED + "No value passed for: <action>")
                        sender.sendMessage("" + ChatColor.GRAY + "This can be one of these values: [ query | unlock ]")
                        return true
                    }
                    when (args[1]) {
                        "query" -> {
                            if (args.size == 2) {
                                // destroy_it action query <enter>
                                sender.sendMessage("" + ChatColor.RED + "No value passed for: <target>")
                                sender.sendMessage("" + ChatColor.GRAY + "This can be one of these values: [ keys | destroyed | stats ]")
                                return true
                            }
                            when (args[2]) {
                                "answer" -> {
                                    if (args.size == 3) {
                                        sender.sendMessage("" + ChatColor.RED + "No value passed for: <action>")
                                        sender.sendMessage("" + ChatColor.GRAY + "This can be one of these values: [ show | return ]")
                                        return true
                                    }
                                    when (args[3]) {
                                        "show" -> {
                                            showAnswer(sender)
                                            data.settings.answerShowing[sender.uniqueId] = true
                                        }
                                        "return" -> {
                                            returnAnswer(sender)
                                            data.settings.answerShowing[sender.uniqueId] = false
                                        }
                                        else -> {
                                            sender.sendMessage("" + ChatColor.RED + "No value passed for: <action>")
                                            sender.sendMessage("" + ChatColor.GRAY + "This can be one of these values: [ show | return ]")
                                            return true
                                        }
                                    }
                                    return true
                                }
                                "keys" -> {
                                    if (args.size == 3) {
                                        // destroy_it action query keys <enter>
                                        sender.sendMessage("" + ChatColor.RED + "No value passed for: <filter>")
                                        sender.sendMessage("" + ChatColor.GRAY + "Expected #1: all")
                                        sender.sendMessage("" + ChatColor.GRAY + "Expected #1: locked")
                                        sender.sendMessage("" + ChatColor.GRAY + "Expected #1: <RowIndex 1~5> <ColumnIndex 1~9>")
                                        sender.sendMessage("" + ChatColor.GRAY + "Expected #2: [ boots | legs | chest | helmet | left_hand ]")
                                        return true
                                    }
                                    when (args.size) {
                                        4 -> {
                                            when (args[3]) {
                                                // destroy_it action query keys all <enter>
                                                "all" -> printAllKeys(sender)
                                                // destroy_it action query keys locked <enter>
                                                "locked" -> printLockedKeys(sender)
                                                // destroy_it action query keys slot_name <enter>
                                                else -> printSpecificKeys(sender, args[3])
                                            }
                                        }
                                        5 -> {
                                            // destroy_it action query keys row_index column_index <enter>
                                            printSpecificKeys(sender, "${args[3]} ${args[4]}")
                                        }
                                        else -> {
                                            // invalid values
                                            sender.sendMessage("" + ChatColor.RED + "Too many arguments for: <filter>")
                                        }
                                    }
                                }
                                "destroyed" -> {
                                    performPrintDestroyed(args, sender)
                                }
                                "stats" -> {
                                    // destroy_it action query stats <enter>
                                    printStats(sender)
                                }
                                else -> {
                                    // destroy_it action query invalid_value
                                    sender.sendMessage("" + ChatColor.RED + "Unknown target: '${args[2]}'")
                                    sender.sendMessage("" + ChatColor.GRAY + "Possible values: [ keys | stats ]")
                                }
                            }
                        }
                        "unlock" -> {
                            if (args.size == 2) {
                                // destroy_it action unlock <enter>
                                sender.sendMessage("" + ChatColor.RED + "No value passed for: <target>")
                                sender.sendMessage("" + ChatColor.GRAY + "Expected #1: <RowIndex 1~5> <ColumnIndex 1~9>")
                                sender.sendMessage("" + ChatColor.GRAY + "Expected #2: [ boots | legs | chest | helmet | left_hand ]")
                            } else {
                                val inventorySlot = when (args.size) {
                                    // case A: destroy_it action unlock valid_slot_name <enter>
                                    3 -> InventoryUpdater.getInventoryPositionByName(args[2])
                                    // case B: destroy_it action unlock row_index column_index <enter>
                                    4 -> InventoryUpdater.getInventoryPositionByName("${args[2]} ${args[3]}")
                                    // case C: invalid values
                                    else -> -1
                                }
                                if (inventorySlot > 0 && !data.unlockedInventories[inventorySlot]) {
                                    // case A, B
                                    if (!data.stats.containsKey("<command>")) {
                                        data.stats["<command>"] =
                                            Data.Stat("<command>", mutableListOf(), mutableListOf())
                                    }
                                    data.stats["<command>"]!!.blocks.add(data.destroyTargets[inventorySlot].name)
                                    data.stats["<command>"]!!.inventories.add(
                                        InventoryUpdater.getInventoryPosition(
                                            inventorySlot
                                        )
                                    )
                                    unlockInventory(inventorySlot)

                                    sender.sendMessage("" + ChatColor.GRAY + "Inventory unlocked with command.")
                                } else if (inventorySlot < 0) {
                                    // case C
                                    sender.sendMessage("" + ChatColor.RED + "Too many arguments for: <target>")
                                } else {
                                    // if slot is already unlocked
                                    sender.sendMessage("" + ChatColor.RED + "Given slot is already unlocked.")
                                }
                            }
                        }
                        else -> {
                            // destroy_it action invalid_action <enter>
                            sender.sendMessage("" + ChatColor.RED + "Unknown action: '${args[1]}'")
                            sender.sendMessage("" + ChatColor.GRAY + "Possible values: [ query | unlock ]")
                        }
                    }
                }
                else -> {
                    // destroy_it invalid_operation <enter>
                    sender.sendMessage("" + ChatColor.RED + "Unknown operation: '${args[0]}'")
                }
            }
            return true
        } else if (sender is BlockCommandSender && label == "destroy_it") {
            if (args.slice(0 until args.size - 1).joinToString(" ") == "action query destroyed") {
                performPrintDestroyed(args, sender)
            } else {
                sender.sendMessage("" + ChatColor.RED + "This command not supports command block execution.")
            }
        }
        return super.onCommand(sender, command, label, args)
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        // returns list of tab completable command arguments
        // system includes space as 1 arguments, so arg.size starts with 1.
        return when (alias) {
            "destroy_it", "destroyIt" -> {
                when (args.size) {
                    1 -> {
                        mutableListOf("reset", "enabled", "action")
                    }
                    2 -> {
                        when (args[0]) {
                            "reset" -> mutableListOf("true")
                            "enabled" -> mutableListOf("true", "false")
                            "action" -> mutableListOf("query", "unlock")
                            else -> mutableListOf()
                        }
                    }
                    3 -> {
                        when (args[1]) {
                            "query" -> mutableListOf("answer", "keys", "destroyed", "stats")
                            "unlock" -> mutableListOf("boots", "legs", "chest", "helmet", "left_hand")
                            else -> mutableListOf()
                        }
                    }
                    4 -> {
                        when (args[2]) {
                            "keys" -> mutableListOf("all", "locked", "boots", "legs", "chest", "helmet", "left_hand")
                            "stats" -> mutableListOf()
                            "destroyed" -> MutableList(server.onlinePlayers.size) { server.onlinePlayers.toMutableList()[it].name }
                            "answer" -> mutableListOf("show", "return")
                            else -> mutableListOf()
                        }
                    }
                    else -> mutableListOf()
                }
            }
            else -> mutableListOf()
        }
    }

    private fun initData() {
        // print initialize started log
        server.logger.log(Level.ALL, "[DestroyIt] Initializing Data...")

        // creates destroy targets
        // load all materials
        val allMaterials = Material.values()
        // initialize destroy targets with Materials.AIR
        val newDestroyTargets = MutableList(Constants.INVENTORY_SIZE) { Material.AIR }

        // fill destroy targets with random materials
        // first 4 inventory slots are unlocked by default, so loop only INVENTORY_SIZE - 4 times.
        for (index in 0 until Constants.INVENTORY_SIZE - 4) {
            // temporary value: Material.AIR, will be replaced with random material.
            var nextMaterial = Material.AIR

            // loop if nextMaterial is not block or destroy target already has nextMaterial.
            while (newDestroyTargets.contains(nextMaterial) || !nextMaterial.isBlock)
                nextMaterial = allMaterials[Random.nextInt(allMaterials.size)]

            // nextMaterial is random material, which is destroyable block and not in destroy targets.
            // first 4 inventory slots are unlocked by default. so skip first 4 indexes of destroy targets.
            newDestroyTargets[index + 4] = nextMaterial
        }


        // creates rewards states
        // key: player's UUID, value: MutableList of RewardState(Int)
        // RewardState -> -1: unlocked, reward given -> 0: locked -> 1: unlocked, reward not given
        val newRewardsStates = mutableMapOf<UUID, MutableList<Int>>()

        // creates stats map
        // key: player's UUID string, value: DestroyItData.Stat object
        val newStats = mutableMapOf<String, Data.Stat>()

        // forEach all players and creates RewardState list / Stat object.
        // first 4 inventory slots are unlocked by default.
        // so first 4 values are -1 (unlocked, reward given)
        server.onlinePlayers.forEach { player ->
            newRewardsStates[player.uniqueId] = MutableList(Constants.INVENTORY_SIZE) { if (it <= 3) -1 else 0 }
            newStats[player.uniqueId.toString()] = Data.Stat(player.name, mutableListOf(), mutableListOf())
        }
        server.offlinePlayers.forEach { player ->
            newRewardsStates[player.uniqueId] = MutableList(Constants.INVENTORY_SIZE) { if (it <= 3) -1 else 0 }
        }


        // creates DestroyItData instance
        _data = Data(
            destroyTargets = newDestroyTargets,
            destroyedBlocks = mutableSetOf(),
            unlockedInventories = MutableList(Constants.INVENTORY_SIZE) { it <= 3 },
            rewardsStates = newRewardsStates,
            stats = newStats,
            settings = Data.Settings(
                enabled = false,
                answerShowing = mutableMapOf(),
                answerInventoryBackup = mutableMapOf()
            )
        )
        // save data into file
        data.saveData(Constants.DATA_PATH)

        // print initialize finished log
        server.logger.log(Level.INFO, "[DestroyIt] Initialize Finished.")
    }

    fun lockInventory() {
        if (_data == null) return
        if (!data.settings.enabled) return

        // forEach all online players and unlockedInventories.
        // unlockedInventories holds each player's inventory has unlocked or locked.
        // if slot is unlocked, just replace slot's item to AIR if slot's item is BARRIER.
        // if slot is locked, set slot's item to BARRIER.
        server.onlinePlayers.filter { data.settings.answerShowing[it.uniqueId] != true }.forEach { player ->
            data.unlockedInventories.forEachIndexed { index, unlocked ->
                if (!unlocked) {
                    val item = ItemStack(Material.BARRIER, 1)
                    val itemMeta = item.itemMeta
                    itemMeta?.setDisplayName("Locked Slot")
                    item.itemMeta = itemMeta
                    player.inventory.setItem(index, item)
                } else if (player.inventory.getItem(index)?.type == Material.BARRIER) {
                    player.inventory.setItem(index, null)
                }
            }
        }
    }

    private fun showAnswer(sender: CommandSender) {
        if (sender !is Player) return
        if (data.settings.answerShowing[sender.uniqueId] == true) {
            sender.sendMessage("" + ChatColor.RED + "Answer is already showing")
            return
        }

        data.settings.answerInventoryBackup[sender.uniqueId] =
            List(Constants.INVENTORY_SIZE) { sender.inventory.getItem(it) }

        for (slotIndex in 0 until Constants.INVENTORY_SIZE) {
            sender.inventory.setItem(slotIndex, ItemStack(data.destroyTargets[slotIndex], 1))
        }

        sender.sendMessage("" + ChatColor.GRAY + "Inventory replaced with answers.")
        sender.sendMessage("" + ChatColor.GRAY + "Execute '/destroy_it action query answer return' to restore inventory.")
    }

    private fun returnAnswer(sender: CommandSender) {
        if (sender !is Player) return
        if (data.settings.answerShowing[sender.uniqueId] == false) {
            sender.sendMessage("" + ChatColor.RED + "Answer is not showing")
            return
        }

        for (slotIndex in 0 until Constants.INVENTORY_SIZE) {
            val itemStack = data.settings.answerInventoryBackup[sender.uniqueId]?.get(slotIndex)
            if (itemStack != null)
                sender.inventory.setItem(slotIndex, itemStack)
        }
    }

    fun unlockInventory(slot: Int) {
        if (_data == null) return
        if (!data.settings.enabled) return

        // if given slot is already unlocked, skip further execution
        if (data.unlockedInventories[slot]) return

        // unlock inventory:
        // set flag to unlocked
        data.unlockedInventories[slot] = true

        // forEach for all players.
        // set online players' unlocked inventory slot's item to golden apple, and set reward state to -1(unlocked, reward given)
        server.onlinePlayers.forEach {
            it.inventory.setItem(slot, ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1))
            if (data.rewardsStates.containsKey(it.uniqueId)) {
                data.rewardsStates[it.uniqueId]?.set(slot, -1)
            }
        }
        // offline player's inventories are not able to access, so just set reward state to 1(unlocked, reward not given) and
        // give rewards later. (see PlayerDetector)
        server.offlinePlayers.forEach {
            if (it.player == null) {
                if (data.rewardsStates.containsKey(it.uniqueId)) {
                    data.rewardsStates[it.uniqueId]?.set(slot, 1)
                }
            }
        }
    }

    private fun performPrintDestroyed(
        args: Array<out String>,
        sender: CommandSender
    ) {
        if (args.size < 4) {
            sender.sendMessage("" + ChatColor.RED + "No value passed for: <PrintTarget>")
            return
        }
        val targetPlayer = Bukkit.getPlayer(args[3])
        if (targetPlayer != null) {
            printDestroyedBlocks(targetPlayer)
        } else {
            sender.sendMessage("Cannot find given player.")
        }
    }

    // below: function which prints information

    private fun printAllKeys(sender: CommandSender) {
        sender.sendMessage("" + ChatColor.YELLOW + "all keys: ")
        sender.sendMessage(" " + ChatColor.GRAY + data.destroyTargets.joinToString(", "))
    }

    private fun printLockedKeys(sender: CommandSender) {
        val lockedTargets = data.destroyTargets.filter { !data.unlockedInventories[data.destroyTargets.indexOf(it)] }
        sender.sendMessage("" + ChatColor.YELLOW + "all locked keys: ")
        sender.sendMessage(" " + ChatColor.GRAY + lockedTargets.joinToString(", "))
        sender.sendMessage("" + ChatColor.BLUE + "total: ${lockedTargets.size} inventories(blocks)")
    }

    private fun printSpecificKeys(sender: CommandSender, subArgs: String) {
        val slotPos = InventoryUpdater.getInventoryPositionByName(subArgs)
        if (slotPos < 0) {
            sender.sendMessage("" + ChatColor.RED + "Invalid value: $subArgs")
            sender.sendMessage("" + ChatColor.GRAY + "Expected format #1: <RowIndex[1~5]> <ColumnIndex[1~9]>")
            sender.sendMessage("" + ChatColor.GRAY + "Expected format #2: [ boots | legs | chest | helmet | left_hand ]")
        } else {
            sender.sendMessage("" + ChatColor.YELLOW + "slot ($subArgs)'s key: " + ChatColor.RESET + data.destroyTargets[slotPos])
        }
    }

    private fun printDestroyedBlocks(target: Player) {

        target.sendMessage("" + ChatColor.YELLOW + "destroyed blocks list: ")
        target.sendMessage(" " + ChatColor.GRAY + data.destroyedBlocks.sorted().joinToString(", "))
        target.sendMessage("" + ChatColor.BLUE + "total ${data.destroyedBlocks.size} blocks")

    }

    private fun printStats(sender: CommandSender) {

        if (data.stats.keys.isEmpty()) {
            sender.sendMessage("" + ChatColor.GRAY + "no statistic data to print.")
            return
        }

        sender.sendMessage("" + ChatColor.GREEN + "Start of statistics")
        data.stats.keys.forEach {
            sender.sendMessage("" + ChatColor.BLUE + "start of ${data.stats[it]!!.playerName}'s statistics")
            if (data.stats[it]!!.blocks.isEmpty() || data.stats[it]!!.inventories.isEmpty()) {
                sender.sendMessage("" + ChatColor.YELLOW + "no destroyed data")
                sender.sendMessage("" + ChatColor.YELLOW + "no unlocked inventory")
            } else {
                sender.sendMessage("" + ChatColor.YELLOW + "destroyed: ")
                sender.sendMessage(" " + ChatColor.WHITE + data.stats[it]!!.blocks.joinToString(", "))

                sender.sendMessage("" + ChatColor.YELLOW + "unlocked: ")
                sender.sendMessage(" " + ChatColor.WHITE + data.stats[it]!!.inventories.joinToString { inventory -> "($inventory)" })
            }
            sender.sendMessage("" + ChatColor.YELLOW + "total: ${data.stats[it]!!.blocks.size} blocks(inventories)")
            sender.sendMessage("" + ChatColor.GRAY + "end of ${data.stats[it]!!.playerName}'s stat")
        }
        sender.sendMessage("" + ChatColor.DARK_GREEN + "End of statistics")

    }

}
