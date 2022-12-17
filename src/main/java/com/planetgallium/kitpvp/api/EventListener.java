package com.planetgallium.kitpvp.api;

import com.planetgallium.kitpvp.Game;
import com.planetgallium.kitpvp.util.Resources;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import com.planetgallium.kitpvp.game.Arena;
import com.planetgallium.kitpvp.util.Toolkit;;

public class EventListener implements Listener {

	private final Resources resources;
	private final Arena arena;
	
	public EventListener(Game plugin) {
		this.resources = plugin.getResources();
		this.arena = plugin.getArena();
	}
	
	@EventHandler
	public void onAbility(PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (e.getHand() != null && e.getHand() != EquipmentSlot.HAND) return;
			if (Toolkit.inArena(e.getPlayer())) {
				Player p = e.getPlayer();

				if (resources.getConfig().getBoolean("Arena.AbilitiesRequireKit") &&
						!arena.getKits().hasKit(p.getName())) {
					return; // if "AbilitiesRequireKit" true, and player does not have kit, return
				}

				ItemStack currentItem = Toolkit.getMainHandItem(p);

				if (currentItem.hasItemMeta() && currentItem.getItemMeta().hasDisplayName()) {
					Ability abilityResult = arena.getAbilities().getAbilityByActivator(currentItem);

					if (abilityResult != null) {
						Bukkit.getPluginManager().callEvent(new PlayerAbilityEvent(p, abilityResult));
						e.setCancelled(true);
					}
				}
			}
		}
	}

}
