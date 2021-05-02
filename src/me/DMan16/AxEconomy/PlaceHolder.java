package me.DMan16.AxEconomy;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class PlaceHolder extends PlaceholderExpansion {

	public boolean persist() {
		return true;
	}

	public boolean canRegister() {
		return true;
	}

	public String getAuthor() {
		return AxEconomyMain.getInstance().getDescription().getAuthors().toString();
	}

	public String getIdentifier() {
		return "AxEconomy";
	}

	public String getVersion() {
		return AxEconomyMain.getInstance().getDescription().getVersion();
	}
	
	public String onRequest(OfflinePlayer player, String identifier) {
		if (player == null) return "";
		if (identifier.equals("balance")) return AxEconomyMain.getEconomy().format(AxEconomyMain.getEconomy().getBalance(player));
		return null;
	}

	public String onPlaceholderRequest(Player player, String identifier) {
		return onRequest(player,identifier);
	}
}