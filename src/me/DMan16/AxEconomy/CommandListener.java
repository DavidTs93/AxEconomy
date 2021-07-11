package me.DMan16.AxEconomy;

import me.Aldreda.AxUtils.Utils.Utils;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CommandListener implements CommandExecutor,TabCompleter {
	
	public CommandListener() {
		PluginCommand command = AxEconomyMain.getInstance().getCommand("eco");
		command.setExecutor(this);
		command.setTabCompleter(this);
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
			usage(sender);
			return true;
		}
		try {
			if (args[0].equalsIgnoreCase("pay")) {
				EconomyResponse response = AxEconomyMain.getEconomy().depositPlayer(args[1],Double.parseDouble(args[2]));
				if (response.transactionSuccess()) sender.sendMessage(Utils.chatColors("&aAdded ") + AxEconomyMain.getEconomy().format(response.amount,true) +
						Utils.chatColors(" &ato &f" + Utils.getPlayerNameByUUID(Utils.getPlayerUUIDByName(args[1]))));
				else Utils.chatColors(sender,"&c" + response.errorMessage);
			} else if (args[0].equalsIgnoreCase("reduct")) {
				EconomyResponse response = AxEconomyMain.getEconomy().withdrawPlayer(args[1],Double.parseDouble(args[2]));
				if (response.transactionSuccess()) sender.sendMessage(Utils.chatColors("&aRemoved ") + AxEconomyMain.getEconomy().format(response.amount,true) +
						Utils.chatColors(" &afrom &f" + Utils.getPlayerNameByUUID(Utils.getPlayerUUIDByName(args[1]))));
				else Utils.chatColors(sender,"&c" + response.errorMessage);
			} else if (args[0].equalsIgnoreCase("balance")) {
				double bal;
				String name;
				if (args.length == 1 && (sender instanceof Player)) {
					bal = AxEconomyMain.getEconomy().getBalance((Player) sender);
					name = ((Player) sender).getName();
				} else {
					bal = AxEconomyMain.getEconomy().getBalance(args[1]);
					if (Double.isInfinite(bal)) throw new SQLException();
					name = Utils.getPlayerNameByUUID(Utils.getPlayerUUIDByName(args[1]));
				}
				sender.sendMessage(Utils.chatColors("&aBalance for &f") + name + Utils.chatColors("&a: &f") + AxEconomyMain.getEconomy().format(bal,true));
			} else if (args[0].equalsIgnoreCase("bank")) {
				if (!(sender instanceof Player)) return true;
				UUID ID;
				String name = null;
				if (args.length == 1) new BankViewer((Player) sender,((Player) sender).getUniqueId(),name);
				else {
					ID = Utils.getPlayerUUIDByName(args[1]);
					if (ID == null) throw new SQLException();
					name = Utils.getPlayerNameByUUID(ID);
					try {
						if (Integer.parseInt(args[2]) == 16) new BankViewer((Player) sender,ID,name,true);
						else throw new Exception();
					} catch (Exception e) {
						new BankViewer((Player) sender,ID,name);
					}
				}
				
			}
		} catch (IndexOutOfBoundsException e) {
			usage(sender);
		} catch (SQLException e) {
			Utils.chatColors(sender,"&c" + AxEconomyMain.getEconomy().depositPlayer((UUID) null,0).errorMessage);
		} catch (NumberFormatException e) {
			Utils.chatColors(sender,"&cplease use only numbers");
		} catch (Exception e) {e.printStackTrace();};
		return true;
	}
	
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> base = Arrays.asList("pay","reduct","balance","bank","help");
		List<String> resultList = new ArrayList<String>();
		if (args.length == 1) for (String cmd : base) if (contains(args[0],cmd)) resultList.add(cmd.toLowerCase());
		if (args.length == 2 && base.contains(args[0].toLowerCase())) for (Player player : Bukkit.getServer().getOnlinePlayers())
			if (contains(args[1],player.getName())) resultList.add(player.getName());
		return resultList;
	}
	
	private void usage(CommandSender sender) {
		List<String> str = new ArrayList<String>();
		str.add("&b&lEconomy &fcommand:");
		str.add("  &bPay&a: &fadd funds to a player.\n  &6&nUsage:&f&l /eco pay <name> <amount>");
		str.add("  &bReduct&a: &fremove funds from a player.\n  &6&nUsage:&f&l /eco reduct <name> <amount>");
		str.add("  &bBalance&a: &fview the player's balance.\n  &6&nUsage:&f&l /eco balance <name>");
		str.add("  &bBank&a: &fview the player's bank.\n  &6&nUsage:&f&l /eco bank <name>");
		str.add("  &bReload&a: &freload the config file.\n  &6&nUsage:&f&l /eco reload");
		Utils.chatColors(sender,String.join("\n\n",str));
	}
	
	private boolean contains(String arg1, String arg2) {
		return (arg1 == null || arg1.isEmpty() || arg2.toLowerCase().contains(arg1.toLowerCase()));
	}
}