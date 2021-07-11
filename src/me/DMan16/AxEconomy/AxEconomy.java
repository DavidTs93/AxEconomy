package me.DMan16.AxEconomy;

import me.Aldreda.AxUtils.Utils.Utils;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class AxEconomy implements net.milkbowl.vault.economy.Economy {
	
	@Override
	public boolean isEnabled() {
		return AxEconomyMain.getInstance() != null;
	}
	
	@Override
	public String getName() {
		return "AldredaEconomy";
	}
	
	@Override
	public boolean hasBankSupport() {
		return false;
	}
	
	@Override
	public int fractionalDigits() {
		return 2;
	}
	
	public double format(String str) {
		double suffixMult = 1;
		if (str.toLowerCase().endsWith("k") || str.toLowerCase().endsWith("m") || str.toLowerCase().endsWith("b")) {
			String suffix = str.substring(str.length() - 1);
			str = str.substring(0,str.length() - 1).toLowerCase();
			if (suffix.equals("k")) suffixMult = 1e3;
			else if (suffix.equals("m")) suffixMult = 1e6;
			else suffixMult = 1e9;
		}
		return Double.parseDouble(str) * suffixMult;
	}
	
	@Override
	public String format(double amount) {
		return format(amount,false);
	}
	
	public String format(double amount, boolean noSuffix) {
		if (Double.isInfinite(amount) || amount < Values.minBalance || amount > Values.maxBalance) return "";
		amount = Utils.roundAfterDot(amount,fractionalDigits());
		double newAmount = amount;
		String name = newAmount == 1 ? currencyNameSingular() : currencyNamePlural();
		String suffix;
		String color;
		if (newAmount <= Values.minBalance) {
			suffix = "";
			color = "&c";
		} else if (newAmount < 1e3) {
			suffix = "";
			color = "&e";
		} else if (newAmount < 1e6) {
			suffix = "K";
			color = "&f";
			newAmount /= 1e3;
		} else if (newAmount < 1e9) {
			suffix = "M";
			color = "&a";
			newAmount /= 1e6;
		} else {
			suffix = "B";
			color = "&a";
			newAmount /= 1e9;
		}
		newAmount = Utils.roundAfterDot(newAmount,fractionalDigits());
		if (noSuffix) {
			suffix = "";
			newAmount = amount;
		}
		String formatAmount = Double.toString(newAmount);
		if (Math.round(newAmount) == newAmount) formatAmount = Integer.toString((int) Math.round(newAmount));
		return Utils.chatColors(color + formatAmount + suffix + " ") + name;
	}
	
	@Override
	public String currencyNamePlural() {
		return Utils.chatColors("&eCoins");
	}
	
	@Override
	public String currencyNameSingular() {
		return Utils.chatColors("&eCoin");
	}
	
	@Override
	public boolean hasAccount(String name) {
		return hasAccount(Utils.getPlayerUUIDByName(name));
	}
	
	@Override
	public boolean hasAccount(String name, String worldName) {
		return hasAccount(name);
	}
	
	public boolean hasAccount(UUID ID) {
		if (ID != null) try {
			return AxEconomyMain.getSQL().checkPlayerInDatabase(ID);
		} catch (SQLException e) {}
		return false;
	}
	
	@Override
	public boolean hasAccount(OfflinePlayer player) {
		if (player == null) return hasAccount((UUID) null);
		return hasAccount(player.getUniqueId());
	}
	
	@Override
	public boolean hasAccount(OfflinePlayer player, String worldName) {
		return hasAccount(player);
	}
	
	@Override
	public double getBalance(String name) {
		return getBalance(Utils.getPlayerUUIDByName(name));
	}
	
	@Override
	public double getBalance(String name, String world) {
		return getBalance(name);
	}
	
	public double getBalance(UUID ID) {
		if (ID != null) try {
			return AxEconomyMain.getSQL().getPlayerBalanceFromDatabase(ID);
		} catch (SQLException e) {}
		return Double.NEGATIVE_INFINITY;
	}
	
	@Override
	public double getBalance(OfflinePlayer player) {
		if (player == null) return getBalance((UUID) null);
		return getBalance(player.getUniqueId());
	}
	
	@Override
	public double getBalance(OfflinePlayer player, String world) {
		return getBalance(player);
	}
	
	@Override
	public boolean has(String name, double amount) {
		return getBalance(name) >= amount;
	}
	
	@Override
	public boolean has(String name, String worldName, double amount) {
		return getBalance(name,worldName) >= amount;
	}
	
	public boolean has(UUID ID, double amount) {
		return getBalance(ID) >= amount;
	}
	
	@Override
	public boolean has(OfflinePlayer player, double amount) {
		return getBalance(player) >= amount;
	}
	
	@Override
	public boolean has(OfflinePlayer player, String worldName, double amount) {
		return getBalance(player,worldName) >= amount;
	}
	
	@Override
	public EconomyResponse withdrawPlayer(String name, double amount) {
		return withdrawPlayer(Utils.getPlayerUUIDByName(name),amount);
	}
	
	@Override
	public EconomyResponse withdrawPlayer(String name, String worldName, double amount) {
		return withdrawPlayer(name,amount);
	}
	
	public EconomyResponse withdrawPlayer(UUID ID, double amount) {
		if (ID == null) return new EconomyResponse(0,0,ResponseType.FAILURE,"Account doesn't exist");
		double current;
		if (amount < 0) return new EconomyResponse(0,0,ResponseType.FAILURE,"Cannot withdraw negative funds");
		try {
			current = AxEconomyMain.getSQL().getPlayerBalanceFromDatabase(ID);
		} catch (SQLException e) {
			return new EconomyResponse(0,0,ResponseType.FAILURE,"Account doesn't exist");
		}
		if (current - amount < Values.minBalance) return new EconomyResponse(0,current,ResponseType.FAILURE,"Insufficient funds");
		try {
			AxEconomyMain.getSQL().updatePlayerBalanceDatabase(ID,current - amount);
			return new EconomyResponse(amount,current - amount,ResponseType.SUCCESS,null);
		} catch (SQLException e) {
			return new EconomyResponse(0,current,ResponseType.FAILURE,"SQL error: " + e.getMessage());
		}
	}
	
	@Override
	public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
		if (player == null) return withdrawPlayer((UUID) null,amount);
		return withdrawPlayer(player.getUniqueId(),amount);
	}
	
	@Override
	public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
		return withdrawPlayer(player,amount);
	}
	
	@Override
	public EconomyResponse depositPlayer(String name, double amount) {
		return depositPlayer(Utils.getPlayerUUIDByName(name),amount);
	}
	
	@Override
	public EconomyResponse depositPlayer(String name, String worldName, double amount) {
		return depositPlayer(name,amount);
	}
	
	public EconomyResponse depositPlayer(UUID ID, double amount) {
		if (ID == null) return new EconomyResponse(0,0,ResponseType.FAILURE,"Account doesn't exist");
		double current;
		if (amount < 0) return new EconomyResponse(0,0,ResponseType.FAILURE,"Cannot deposit negative funds");
		try {
			current = AxEconomyMain.getSQL().getPlayerBalanceFromDatabase(ID);
		} catch (SQLException e) {
			return new EconomyResponse(0,0,ResponseType.FAILURE,"Account doesn't exist");
		}
		if (current + amount > Values.maxBalance) return new EconomyResponse(0,current,ResponseType.FAILURE,"Overdraft");
		try {
			AxEconomyMain.getSQL().updatePlayerBalanceDatabase(ID,current + amount);
			return new EconomyResponse(amount,current + amount,ResponseType.SUCCESS,null);
		} catch (SQLException e) {
			return new EconomyResponse(0,current,ResponseType.FAILURE,"SQL error: " + e.getMessage());
		}
	}
	
	@Override
	public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
		if (player == null) return depositPlayer((UUID) null,amount);
		return depositPlayer(player.getUniqueId(),amount);
	}
	
	@Override
	public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
		return depositPlayer(player,amount);
	}
	
	@Deprecated
	@Override
	public EconomyResponse createBank(String name, String player) {
		return new EconomyResponse(0,0,ResponseType.NOT_IMPLEMENTED,"Currency bank managed per player");
	}
	
	@Deprecated
	@Override
	public EconomyResponse createBank(String name, OfflinePlayer player) {
		return new EconomyResponse(0,0,ResponseType.NOT_IMPLEMENTED,"Currency bank managed per player");
	}
	
	@Deprecated
	@Override
	public EconomyResponse deleteBank(String name) {
		return new EconomyResponse(0,0,ResponseType.NOT_IMPLEMENTED,"Currency bank managed per player");
	}
	
	@Deprecated
	@Override
	public EconomyResponse bankBalance(String name) {
		return new EconomyResponse(0,0,ResponseType.NOT_IMPLEMENTED,"Currency bank managed per player");
	}
	
	@Deprecated
	@Override
	public EconomyResponse bankHas(String name, double amount) {
		return new EconomyResponse(0,0,ResponseType.NOT_IMPLEMENTED,"Currency bank managed per player");
	}
	
	@Deprecated
	@Override
	public EconomyResponse bankWithdraw(String name, double amount) {
		return new EconomyResponse(0,0,ResponseType.NOT_IMPLEMENTED,"Currency bank managed per player");
	}
	
	@Deprecated
	@Override
	public EconomyResponse bankDeposit(String name, double amount) {
		return new EconomyResponse(0,0,ResponseType.NOT_IMPLEMENTED,"Currency bank managed per player");
	}
	
	@Deprecated
	@Override
	public EconomyResponse isBankOwner(String name, String playerName) {
		return new EconomyResponse(0,0,ResponseType.NOT_IMPLEMENTED,"Currency bank managed per player");
	}
	
	@Deprecated
	@Override
	public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
		return new EconomyResponse(0,0,ResponseType.NOT_IMPLEMENTED,"Currency bank managed per player");
	}
	
	@Deprecated
	@Override
	public EconomyResponse isBankMember(String name, String playerName) {
		return new EconomyResponse(0,0,ResponseType.NOT_IMPLEMENTED,"Currency bank managed per player");
	}
	
	@Deprecated
	@Override
	public EconomyResponse isBankMember(String name, OfflinePlayer player) {
		return new EconomyResponse(0,0,ResponseType.NOT_IMPLEMENTED,"Currency bank managed per player");
	}
	
	@Deprecated
	@Override
	public List<String> getBanks() {
		return null;
	}
	
	@Override
	public boolean createPlayerAccount(String name) {
		return createPlayerAccount(Utils.getPlayerUUIDByName(name),name);
	}
	
	@Override
	public boolean createPlayerAccount(String name, String worldName) {
		return createPlayerAccount(name);
	}
	
	public boolean createPlayerAccount(UUID ID, String name) {
		if (ID != null && name != null) try {
			if (hasAccount(ID));
			else AxEconomyMain.getSQL().addPlayerToDatabase(ID);
			return true;
		} catch (SQLException e) {}
		return false;
	}
	
	@Override
	public boolean createPlayerAccount(OfflinePlayer player) {
		if (player == null) return createPlayerAccount((UUID) null,(String) null);
		return createPlayerAccount(player.getUniqueId(),player.getName());
	}
	
	@Override
	public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
		return createPlayerAccount(player);
	}
	
	public HashMap<Integer,List<ItemStack>> getBank(UUID ID) {
		if (ID != null) try {
			return AxEconomyMain.getSQL().getPlayerBanksFromDatabase(ID);
		} catch (SQLException e) {}
		return null;
	}
	
	public HashMap<Integer,List<ItemStack>> getBank(OfflinePlayer player) {
		if (player == null) return getBank((UUID) null);
		return getBank(player.getUniqueId());
	}
	
	public boolean setBank(UUID ID, HashMap<Integer,List<ItemStack>> banks) {
		if (ID != null) try {
			AxEconomyMain.getSQL().updatePlayerBanksDatabase(ID,banks);
			return true;
		} catch (SQLException e) {}
		return false;
	}
	
	public boolean setBank(OfflinePlayer player, HashMap<Integer,List<ItemStack>> banks) {
		if (player == null) return false;
		return setBank(player.getUniqueId(),banks);
	}
	
	public double getBankBalance(String name) {
		return getBankBalance(Utils.getPlayerUUIDByName(name));
	}
	
	public double getBankBalance(String name, String world) {
		return getBankBalance(name);
	}
	
	public double getBankBalance(UUID ID) {
		if (ID != null) try {
			return AxEconomyMain.getSQL().getPlayerBankBalanceFromDatabase(ID);
		} catch (SQLException e) {}
		return Double.NEGATIVE_INFINITY;
	}
	
	public double getBankBalance(OfflinePlayer player) {
		if (player == null) return getBankBalance((UUID) null);
		return getBankBalance(player.getUniqueId());
	}
	
	public EconomyResponse withdrawBankPlayer(String name, double amount) {
		return withdrawBankPlayer(Utils.getPlayerUUIDByName(name),amount);
	}
	
	public EconomyResponse withdrawBankPlayer(UUID ID, double amount) {
		if (ID == null) return new EconomyResponse(0,0,ResponseType.FAILURE,"Account doesn't exist");
		double current;
		if (amount < 0) return new EconomyResponse(0,0,ResponseType.FAILURE,"Cannot withdraw negative funds");
		try {
			current = AxEconomyMain.getSQL().getPlayerBankBalanceFromDatabase(ID);
		} catch (SQLException e) {
			return new EconomyResponse(0,0,ResponseType.FAILURE,"Account doesn't exist");
		}
		if (current - amount < 0) return new EconomyResponse(0,current,ResponseType.FAILURE,"Insufficient funds");
		try {
			AxEconomyMain.getSQL().updatePlayerBankBalanceDatabase(ID,current - amount);
			return new EconomyResponse(amount,current - amount,ResponseType.SUCCESS,null);
		} catch (SQLException e) {
			return new EconomyResponse(0,current,ResponseType.FAILURE,"SQL error: " + e.getMessage());
		}
	}
	
	public EconomyResponse withdrawBankPlayer(OfflinePlayer player, double amount) {
		if (player == null) return withdrawBankPlayer((UUID) null,amount);
		return withdrawBankPlayer(player.getUniqueId(),amount);
	}
	
	public EconomyResponse depositBankPlayer(String name, double amount) {
		return depositBankPlayer(Utils.getPlayerUUIDByName(name),amount);
	}
	
	public EconomyResponse depositBankPlayer(UUID ID, double amount) {
		if (ID == null) return new EconomyResponse(0,0,ResponseType.FAILURE,"Account doesn't exist");
		double current;
		if (amount < 0) return new EconomyResponse(0,0,ResponseType.FAILURE,"Cannot deposit negative funds");
		try {
			current = AxEconomyMain.getSQL().getPlayerBankBalanceFromDatabase(ID);
		} catch (SQLException e) {
			return new EconomyResponse(0,0,ResponseType.FAILURE,"Account doesn't exist");
		}
		try {
			if (current + amount > getMaxBankBalance(ID))
				return new EconomyResponse(0,current,ResponseType.FAILURE,"Overdraft");
			AxEconomyMain.getSQL().updatePlayerBankBalanceDatabase(ID,current + amount);
			return new EconomyResponse(amount,current + amount,ResponseType.SUCCESS,null);
		} catch (SQLException e) {
			return new EconomyResponse(0,current,ResponseType.FAILURE,"SQL error: " + e.getMessage());
		}
	}
	
	public EconomyResponse depositBankPlayer(OfflinePlayer player, double amount) {
		if (player == null) return new EconomyResponse(0,0,ResponseType.FAILURE,"Account doesn't exist");
		return depositBankPlayer(player.getUniqueId(),amount);
	}
	
	public double getMaxBankBalance(String name) {
		return getMaxBankBalance(Utils.getPlayerUUIDByName(name));
	}
	
	public double getMaxBankBalance(UUID ID) {
		try {
			return AxEconomyMain.getSQL().getPlayerMaxBankBalanceDatabase(ID);
		} catch (SQLException e) {}
		return Double.NEGATIVE_INFINITY;
	}
	
	public double getMaxBankBalance(OfflinePlayer player) {
		return getMaxBankBalance(player.getUniqueId());
	}
	
	public int getBanksCount(String name) {
		return getBanksCount(Utils.getPlayerUUIDByName(name));
	}
	
	public int getBanksCount(String name, String world) {
		return getBanksCount(name);
	}
	
	public int getBanksCount(UUID ID) {
		if (ID != null) try {
			return AxEconomyMain.getSQL().getPlayerBankCountFromDatabase(ID);
		} catch (SQLException e) {}
		return -1;
	}
	
	public int getBanksCount(OfflinePlayer player) {
		if (player == null) return getBanksCount((UUID) null);
		return getBanksCount(player.getUniqueId());
	}
	
	public EconomyResponse increaseBanksCount(String name) {
		return increaseBanksCount(Utils.getPlayerUUIDByName(name));
	}
	
	public EconomyResponse increaseBanksCount(UUID ID) {
		if (ID == null) return new EconomyResponse(0,0,ResponseType.FAILURE,"Account doesn't exist");
		int current;
		try {
			current = AxEconomyMain.getSQL().getPlayerBankCountFromDatabase(ID);
		} catch (SQLException e) {
			return new EconomyResponse(0,0,ResponseType.FAILURE,"Account doesn't exist");
		}
		try {
			if (AxEconomyMain.getSQL().increasePlayerBankCountDatabase(ID) == current)
				return new EconomyResponse(0,current,ResponseType.FAILURE,"Maximum banks reached");
			return new EconomyResponse(1,current + 1,ResponseType.SUCCESS,null);
		} catch (SQLException e) {
			return new EconomyResponse(0,current,ResponseType.FAILURE,"SQL error: " + e.getMessage());
		}
	}
	
	public EconomyResponse increaseBanksCount(OfflinePlayer player) {
		if (player == null) return new EconomyResponse(0,0,ResponseType.FAILURE,"Account doesn't exist");
		return increaseBanksCount(player.getUniqueId());
	}
}