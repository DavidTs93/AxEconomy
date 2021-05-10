package me.DMan16.AxEconomy;

import me.Aldreda.AxUtils.AxUtils;
import me.Aldreda.AxUtils.Utils.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

class MySQL {
	
	MySQL() throws SQLException {
		createTable();
	}
	
	private void createTable() throws SQLException {
		Statement statement = AxUtils.getMySQL().getConnection().createStatement();
		DatabaseMetaData data = AxUtils.getMySQL().getConnection().getMetaData();
		statement.execute("CREATE TABLE IF NOT EXISTS Economy (UUID VARCHAR(36) NOT NULL UNIQUE);");
		if (!data.getColumns(null,null,"Economy","UUID").next())
			statement.execute("ALTER TABLE Economy ADD UUID VARCHAR(36) NOT NULL UNIQUE;");
		if (!data.getColumns(null,null,"Economy","Balance").next())
			statement.execute("ALTER TABLE Economy ADD Balance DECIMAL(62,2) NOT NULL;");
		if (!data.getColumns(null,null,"Economy","Name").next())
			statement.execute("ALTER TABLE Economy ADD Name TEXT NOT NULL;");
		if (!data.getColumns(null,null,"Economy","Banks").next())
			statement.execute("ALTER TABLE Economy ADD Banks DECIMAL(4,0) NOT NULL;");
		if (!data.getColumns(null,null,"Economy","BankBalance").next())
			statement.execute("ALTER TABLE Economy ADD BankBalance DECIMAL(62,2) NOT NULL;");
		for (int i = 1; i <= Values.maxBanks; i++) {
			List<String> columns = new ArrayList<String>();
			for (int j = 1; j <= Values.perBank; j++) columns.add("Item" + j + " TEXT");
			statement.execute("CREATE TABLE IF NOT EXISTS Bank" + i + " (UUID VARCHAR(36) NOT NULL UNIQUE," + String.join(",",columns) + ");");
			if (!data.getColumns(null,null,"Bank" + i,"UUID").next())
				statement.execute("ALTER TABLE Bank" + i + " ADD UUID VARCHAR(36) NOT NULL UNIQUE;");
			columns.clear();
			for (int j = 1; j <= Values.perBank; j++)
				if (!data.getColumns(null,null,"Bank" + i,"Item" + j).next()) columns.add("Item" + j + " TEXT");
			if (!columns.isEmpty()) for (String str : columns) statement.execute("ALTER TABLE Bank" + i + " ADD " + str + ";");
		}
		statement.close();
	}
	
	boolean checkPlayerInDatabase(UUID ID) throws SQLException {
		if (ID == null) throw new SQLException("ID can't be null");
		boolean result = true;
		PreparedStatement statement = AxUtils.getMySQL().getConnection().prepareStatement("SELECT * FROM Economy WHERE UUID=?;");
		statement.setString(1,ID.toString());
		result = result && statement.executeQuery().next();
		statement.close();
		for (int i = 1; i <= getPlayerBankCountFromDatabase(ID); i++) {
			statement = AxUtils.getMySQL().getConnection().prepareStatement("SELECT * FROM Bank" + i + " WHERE UUID=?;");
			statement.setString(1,ID.toString());
			result = result && statement.executeQuery().next();
			statement.close();
		}
		return result;
	}
	
	void addPlayerToDatabase(OfflinePlayer player) throws SQLException {
		if (player == null) throw new SQLException("Player can't be null");
		addPlayerToDatabase(player.getUniqueId(),player.getName());
	}
	
	void addPlayerToDatabase(UUID ID, String name) throws SQLException {
		if (ID == null) throw new SQLException("ID can't be null");
		if (name == null) throw new SQLException("Name can't be null");
		PreparedStatement statement;
		statement = AxUtils.getMySQL().getConnection().prepareStatement("INSERT INTO Economy (UUID,Balance,Name,Banks,BankBalance) VALUES (?,?,?,?,?);");
		statement.setString(1,ID.toString());
		statement.setDouble(2,Values.startingBalance);
		statement.setString(3,name);
		statement.setInt(4,Values.startingBanks);
		statement.setInt(5,0);
		try {
			statement.executeUpdate();
		} catch (SQLException e) {}
		statement.close();
		for (int i = 1; i <= Values.startingBanks; i++) {
			statement = AxUtils.getMySQL().getConnection().prepareStatement("INSERT INTO Bank" + i + " (UUID) VALUES (?);");
			statement.setString(1,ID.toString());
			try {
				statement.executeUpdate();
			} catch (SQLException e) {}
			statement.close();
		}
		statement.close();
	}
	
	void updatePlayerNameDatabase(UUID ID, String name) throws SQLException {
		PreparedStatement statement = AxUtils.getMySQL().getConnection().prepareStatement("UPDATE Economy SET Name=? WHERE UUID=?;");
		statement.setString(1,ID.toString());
		statement.setString(2,name);
		statement.executeUpdate();
		statement.close();
	}
	
	double getPlayerBalanceFromDatabase(UUID ID) throws SQLException {
		if (ID == null) throw new SQLException("ID can't be null");
		PreparedStatement statement = AxUtils.getMySQL().getConnection().prepareStatement("SELECT * FROM Economy WHERE UUID=?;");
		statement.setString(1,ID.toString());
		ResultSet result = statement.executeQuery();
		result.next();
		double balance = result.getDouble("Balance");
		statement.close();
		int round = AxEconomyMain.getEconomy().fractionalDigits();
		if (round < 0) return balance;
		return Utils.roundAfterDot(balance,round);
	}
	
	void updatePlayerBalanceDatabase(UUID ID, double amount) throws SQLException {
		if (ID == null) throw new SQLException("ID can't be null");
		PreparedStatement statement = AxUtils.getMySQL().getConnection().prepareStatement("UPDATE Economy SET Balance=? WHERE UUID=?;");
		statement.setDouble(1,amount);
		statement.setString(2,ID.toString());
		statement.executeUpdate();
		statement.close();
	}
	
	void updatePlayerBalanceDatabase(OfflinePlayer player, double amount) throws SQLException {
		if (player == null) throw new SQLException("Player can't be null");
		PreparedStatement statement = AxUtils.getMySQL().getConnection().prepareStatement("UPDATE Economy SET Balance=?,Name=? WHERE UUID=?;");
		statement.setDouble(1,amount);
		statement.setString(2,player.getName());
		statement.setString(3,player.getUniqueId().toString());
		statement.executeUpdate();
		statement.close();
	}
	
	double getPlayerBankBalanceFromDatabase(UUID ID) throws SQLException {
		if (ID == null) throw new SQLException("ID can't be null");
		PreparedStatement statement = AxUtils.getMySQL().getConnection().prepareStatement("SELECT * FROM Economy WHERE UUID=?;");
		statement.setString(1,ID.toString());
		ResultSet result = statement.executeQuery();
		result.next();
		double balance = result.getDouble("BankBalance");
		statement.close();
		int round = AxEconomyMain.getEconomy().fractionalDigits();
		if (round < 0) return balance;
		return Utils.roundAfterDot(balance,round);
	}
	
	void updatePlayerBankBalanceDatabase(UUID ID, double amount) throws SQLException {
		if (ID == null) throw new SQLException("ID can't be null");
		PreparedStatement statement = AxUtils.getMySQL().getConnection().prepareStatement("UPDATE Economy SET BankBalance=? WHERE UUID=?;");
		statement.setDouble(1,amount);
		statement.setString(2,ID.toString());
		statement.executeUpdate();
		statement.close();
	}
	
	void updatePlayerBankBalanceDatabase(OfflinePlayer player, double amount) throws SQLException {
		if (player == null) throw new SQLException("Player can't be null");
		PreparedStatement statement = AxUtils.getMySQL().getConnection().prepareStatement("UPDATE Economy SET BankBalance=?,Name=? WHERE UUID=?;");
		statement.setDouble(1,amount);
		statement.setString(2,player.getName());
		statement.setString(3,player.getUniqueId().toString());
		statement.executeUpdate();
		statement.close();
	}
	
	int getPlayerBankCountFromDatabase(UUID ID) throws SQLException {
		if (ID == null) throw new SQLException("ID can't be null");
		PreparedStatement statement = AxUtils.getMySQL().getConnection().prepareStatement("SELECT * FROM Economy WHERE UUID=?;");
		statement.setString(1,ID.toString());
		ResultSet resultBank = statement.executeQuery();
		resultBank.next();
		int playerBanks = resultBank.getInt("Banks");
		resultBank.close();
		statement.close();
		return playerBanks;
	}
	
	double getPlayerMaxBankBalanceDatabase(UUID ID) throws SQLException {
		int playerBanks = getPlayerBankCountFromDatabase(ID);
		if (playerBanks < Values.startingBanks || playerBanks > Values.maxBanks) throw new SQLException("Illegal number of banks");
		return Values.maxBankBalanceBanks.get(playerBanks - Values.startingBanks);
	}
	
	int increasePlayerBankCountDatabase(UUID ID) throws SQLException {
		if (ID == null) throw new SQLException("ID can't be null");
		int playerBanks = getPlayerBankCountFromDatabase(ID) + 1;
		if (playerBanks > Values.maxBanks) throw new SQLException("Too many banks");
		PreparedStatement statement = AxUtils.getMySQL().getConnection().prepareStatement("UPDATE Economy SET Banks=? WHERE UUID=?;");
		statement.setDouble(1,playerBanks);
		statement.setString(2,ID.toString());
		statement.executeUpdate();
		statement.close();
		return playerBanks;
	}
	
	int increasePlayerBankCountDatabase(OfflinePlayer player) throws SQLException {
		if (player == null) throw new SQLException("Player can't be null");
		int playerBanks = getPlayerBankCountFromDatabase(player.getUniqueId()) + 1;
		if (playerBanks > Values.maxBanks) throw new SQLException("Too many banks");
		PreparedStatement statement = AxUtils.getMySQL().getConnection().prepareStatement("UPDATE Economy SET Banks=?,Name=? WHERE UUID=?;");
		statement.setDouble(1,playerBanks);
		statement.setString(2,player.getName());
		statement.setString(3,player.getUniqueId().toString());
		statement.executeUpdate();
		statement.close();
		return playerBanks;
	}
	
	private List<ItemStack> getPlayerBankFromDatabase(UUID ID, int bank) throws SQLException {
		List<ItemStack> items = new ArrayList<ItemStack>();
		PreparedStatement statement = AxUtils.getMySQL().getConnection().prepareStatement("SELECT * FROM Bank" + bank + " WHERE UUID=?;");
		statement.setString(1,ID.toString());
		ResultSet resultBank = statement.executeQuery();
		if (!resultBank.next()) throw new SQLException();
		for (int i = 1; i <= Values.perBank; i++) {
			String itemBase64 = resultBank.getString("Item" + i);
			if (itemBase64 == null) items.add(null);
			else items.add((ItemStack) Utils.ObjectFromBase64(itemBase64));
		}
		resultBank.close();
		statement.close();
		return items;
	}
	
	HashMap<Integer,List<ItemStack>> getPlayerBanksFromDatabase(UUID ID) throws SQLException {
		if (ID == null) throw new SQLException("ID can't be null");
		HashMap<Integer,List<ItemStack>> banks = new HashMap<Integer,List<ItemStack>>();
		int playerBanks = getPlayerBankCountFromDatabase(ID);
		for (int i = 1; i <= playerBanks; i++) banks.put(i,getPlayerBankFromDatabase(ID,i));
		return banks;
	}
	
	private void updatePlayerBankDatabase(UUID ID, int bank, List<ItemStack> items) throws SQLException {
		if (ID == null) throw new SQLException("ID can't be null");
		if (bank < 1 || bank > Values.maxBanks) throw new SQLException("Illegal bank");
		if (items == null) items = new ArrayList<ItemStack>();
		while (items.size() < Values.perBank) items.add(null);
		List<String> itemsStr = new ArrayList<String>();
		for (int i = 1; i <= Values.perBank; i++) {
			ItemStack item = Utils.isNull(items.get(i - 1)) ? null : items.get(i - 1);
			itemsStr.add("Item" + i + "=" + (item == null ? "null" : "\"" + Utils.ObjectToBase64(item) + "\""));
		}
		PreparedStatement statement = AxUtils.getMySQL().getConnection().prepareStatement("UPDATE Bank" + bank + " Set " + String.join(",",itemsStr) +
				" WHERE UUID=?;");
		statement.setString(1,ID.toString());
		statement.executeUpdate();
		statement.close();
	}
	
	void updatePlayerBanksDatabase(UUID ID, HashMap<Integer,List<ItemStack>> banks) throws SQLException {
		if (ID == null) throw new SQLException("ID can't be null");
		if (banks == null) throw new SQLException("Banks can't be null");
		if (banks.isEmpty()) return;
		for (int i : banks.keySet()) updatePlayerBankDatabase(ID,i,banks.get(i));
	}
	
	void updatePlayerBanksDatabase(OfflinePlayer player, HashMap<Integer,List<ItemStack>> banks) throws SQLException {
		if (player == null) throw new SQLException("Player can't be null");
		if (banks == null) throw new SQLException("Banks can't be null");
		updatePlayerBanksDatabase(player.getUniqueId(),banks);
		updatePlayerNameDatabase(player.getUniqueId(),player.getName());
	}
	
	UUID getPlayerUUIDByName(String name) throws SQLException {
		if (name == null) throw new SQLException("Name can't be null");
		Statement statement = AxUtils.getMySQL().getConnection().createStatement();
		ResultSet result = statement.executeQuery("SELECT * FROM Economy;");
		UUID ID = null;
		while (result.next()) try {
			if (result.getString("Name").equalsIgnoreCase(name)) {
				ID = UUID.fromString(result.getString("UUID"));
				break;
			}
		} catch (Exception e) {}
		result.close();
		statement.close();
		return ID;
	}
	
	String getPlayerNameByUUID(UUID ID) throws SQLException {
		if (ID == null) throw new SQLException("ID can't be null");
		Statement statement = AxUtils.getMySQL().getConnection().createStatement();
		ResultSet result = statement.executeQuery("SELECT * FROM Economy;");
		String name = null;
		while (result.next()) try {
			if (ID.toString().equals(result.getString("UUID"))) {
				name = result.getString("Name");
				break;
			}
		} catch (Exception e) {}
		result.close();
		statement.close();
		return name;
	}
}