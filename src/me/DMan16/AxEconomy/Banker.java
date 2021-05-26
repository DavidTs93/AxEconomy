package me.DMan16.AxEconomy;

import me.Aldreda.AxUtils.Utils.Utils;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

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
}