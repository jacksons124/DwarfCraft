package com.smartaleq.bukkit.dwarfcraft.events;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.block.Action;

import com.smartaleq.bukkit.dwarfcraft.DCCraftSchedule;
import com.smartaleq.bukkit.dwarfcraft.DCPlayer;
import com.smartaleq.bukkit.dwarfcraft.DwarfCraft;
import com.smartaleq.bukkit.dwarfcraft.Effect;
import com.smartaleq.bukkit.dwarfcraft.EffectType;
import com.smartaleq.bukkit.dwarfcraft.Skill;
import com.smartaleq.bukkit.dwarfcraft.Util;

public class DCPlayerListener extends PlayerListener {
	private final DwarfCraft plugin;

	public DCPlayerListener(final DwarfCraft plugin) {
		this.plugin = plugin;
	}

	/**
	 * When a player joins the server this initialized their data from the
	 * database or creates new info for them.
	 * 
	 * also broadcasts a welcome "player" message
	 */
	@Override
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		DCPlayer dCPlayer = plugin.getDataManager().find(player);
		if (dCPlayer == null) {
			dCPlayer = plugin.getDataManager().createDwarf(player);
		}
		if (!plugin.getDataManager().getDwarfData(dCPlayer)) {
			plugin.getDataManager().createDwarfData(dCPlayer);
		}
		plugin.getOut().welcome(plugin.getServer(), dCPlayer);
	}
	
    /**	
	 * Called when a player interacts	
     *	
	 * @param event Relevant event details	
     */
	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {
		//Crafting changes
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.WORKBENCH){
			DCCraftSchedule sched = new DCCraftSchedule(plugin, plugin.getDataManager().find(event.getPlayer()));
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, sched, 5);
		}
		
		Player     player = event.getPlayer();
		DCPlayer dCPlayer = plugin.getDataManager().find(player);
		HashMap<Integer, Skill> skills = dCPlayer.getSkills();
		ItemStack item = player.getItemInHand();
		int itemId = -1;
		short durability = 0;
		
		if (item != null) {
			itemId = item.getTypeId();
			durability = item.getDurability();
		}
		
		//EffectType.PLOWDURABILITY And EffectType.PLOW
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK){			
			Block block = event.getClickedBlock();
			Location loc = block.getLocation();
			Material material = block.getType();
			for (Skill s : skills.values()) {
				for (Effect e : s.getEffects()) {
					if (e.getEffectType() == EffectType.PLOWDURABILITY) {
						for (int id : e.getTools()) {
							if (id == itemId && (material == Material.DIRT || material == Material.GRASS)) {
								double effectAmount = e.getEffectAmount(dCPlayer);
								if (DwarfCraft.debugMessagesThreshold < 3)
									System.out.println("DC2: affected durability of a hoe - old:" + durability);
								
								item.setDurability((short)(durability + Util.randomAmount(effectAmount)));
								
								if (DwarfCraft.debugMessagesThreshold < 3)
									System.out.println("DC3: affected durability of a hoe - new:" + item.getDurability());
								
								Util.toolChecker(player);
								block.setTypeId(60);
							}
						}
					}
					if (e.getEffectType() == EffectType.PLOW) {
						for (int id : e.getTools()) {
							if (id == itemId && material == Material.GRASS) {
								loc.setY(loc.getY()+1);
								Util.dropBlockEffect(loc, e, e.getEffectAmount(dCPlayer), true, (byte) 0);
								if (DwarfCraft.debugMessagesThreshold < 3)
									System.out.println("DC3: hoed some ground:" + e.getEffectAmount(dCPlayer));
							}
						}
					}
				}
			}
		}

		//EffectType.EAT
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR){	
			for (Skill s : skills.values()) {
				for (Effect e : s.getEffects()) {
					if (e.getEffectType() == EffectType.EAT && e.getInitiatorId() == itemId) {
						if (DwarfCraft.debugMessagesThreshold < 8)
							System.out.println("DC8: ate food:" + item.getType().toString() + " for " + e.getEffectAmount(dCPlayer));
						
						player.setHealth(Math.min((int) (player.getHealth() + Util.randomAmount(e.getEffectAmount(dCPlayer))), 20));
						player.getInventory().removeItem(item);
						event.setCancelled(true);
					}
				}
			}
		}
	}
}