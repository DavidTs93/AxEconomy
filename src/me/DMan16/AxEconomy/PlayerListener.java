package me.DMan16.AxEconomy;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import me.Aldreda.AxUtils.Classes.Listener;
import me.Aldreda.AxUtils.Utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class PlayerListener extends Listener {
	
	public PlayerListener() {
		register(AxEconomyMain.getInstance());
	}
	
	@EventHandler
	public void addPlayerToDatabase(AsyncPlayerPreLoginEvent event) {
		try {
			if (Utils.isPlayerNPC(Bukkit.getPlayer(event.getUniqueId()))) return;
		} catch (Exception e) {}
		if (AxEconomyMain.getEconomy().hasAccount(event.getUniqueId())) if (!AxEconomyMain.getEconomy().createPlayerAccount(event.getUniqueId(),event.getName()))
			event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,Component.translatable("multiplayer.aldreda.login_error",NamedTextColor.RED));
	}
}