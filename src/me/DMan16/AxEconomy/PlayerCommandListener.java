package me.DMan16.AxEconomy;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import me.Aldreda.AxUtils.Utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

public class PlayerCommandListener implements CommandExecutor {
	
	public PlayerCommandListener() {
		PluginCommand command = AxEconomyMain.getInstance().getCommand("balance");
		command.setExecutor(this);
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Component msg = Component.translatable(Values.translateBalance).append(Component.text(": ")).color(Values.translateBalanceColor).append(Component.text(
					AxEconomyMain.getEconomy().format(AxEconomyMain.getEconomy().getBalance((Player) sender),true))).decoration(TextDecoration.ITALIC,false);
			((Player) sender).sendMessage(msg);
			//Utils.chatColors("&bBalance: ") + AldredaEconomy.getEconomy().format(AldredaEconomy.getEconomy().getBalance((Player) sender),true);
		} else sender.sendMessage(Utils.chatColors("&cOnly for players"));
		return true;
	}
}