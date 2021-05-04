package me.DMan16.AxEconomy;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import me.Aldreda.AxUtils.Utils.Utils;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.Trait;

public class Banker extends Trait {
	
	public Banker() {
		super(Values.nameBanker);
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void click(NPCRightClickEvent event) {
		if (event.isCancelled() || event.getNPC() != getNPC() || Utils.isPlayerNPC(event.getClicker())) return;
		event.setCancelled(true);
		new BankViewer(event.getClicker(),event.getClicker().getUniqueId(),null);
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void click(PlayerInteractEntityEvent event) {
		if (!event.isCancelled() && event.getRightClicked() == getNPC()) event.setCancelled(true);
	}
}