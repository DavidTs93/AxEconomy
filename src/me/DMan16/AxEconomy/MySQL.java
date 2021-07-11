package me.DMan16.AxEconomy;

import me.Aldreda.AxUtils.AxUtils;
import me.Aldreda.AxUtils.Utils.Utils;
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
		Statement statement = AxUtils.getConnection().createStatement();
		DatabaseMetaData data = AxUtils.getConnection().getMetaData();
		statement.execute("CREATE TABLE IF NOT EXISTS Economy (UUID VARCHAR(36) NOT NULL UNIQUE,Balance DECIMAL(62,2) NOT NULL,Banks DECIMAL(4,0) NOT NULL,BankBalance DECIMAL(62,2) NOT NULL);");
		if (!data.getColumns(null,null,"Economy","UUID").next())
			statement.execute("ALTER TABLE Economy ADD UUID VARCHAR(36) NOT NULL UNIQUE;");
		if (!data.getColumns(null,null,"Economy","Balance").next())
			statement.execute("ALTER TABLE Economy ADD Balance DECIMAL(62,2) NOT NULL;");
		if (!data.getColumns(null,null,"Economy","Banks").next())
			statement.execute("ALTER TABLE Economy ADD Banks DECIMAL(4,0) NOT NULL;");
		if (!data.getColumns(null,null,"Economy","BankBalance").next())
			statement.execute("ALTER TABLE Economy ADD BankBalance DECIMAL(62,2) NOT NULL;");
		int[] nums;
		for (int i = 1; i <= Values.maxBanks; i++) {
			List<String> columns = new ArrayList<String>();
			for (int j = 1; j <= Values.perBank; j++) columns.add("Item" + j + " TEXT");
			statement.execute("CREATE TABLE IF NOT EXISTS Bank" + i + " (UUID VARCHAR(36) NOT NULL UNIQUE," + String.join(",",columns) + ");");
			if (!data.getColumns(null,null,"Bank" + i,"UUID").next())
				statement.execute("ALTER TABLE Bank" + i + " ADD UUID VARCHAR(36) NOT NULL UNIQUE;");
			columns.clear();
			ResultSetMetaData metaData = statement.executeQuery("SELECT * FROM Bank" + i).getMetaData();
			nums = new int[Values.perBank];
			for (int k = 1; k <= metaData.getColumnCount(); k++) try {
				nums[Integer.parseInt(metaData.getColumnName(k).toLowerCase().replace("item","")) - 1] = 1;
			} catch (Exception e) {}
			for (int k = 0; k < nums.length; k++) if (nums[k] != 1) columns.add("Item" + (k + 1) + " TEXT");
			if (!columns.isEmpty()) for (String str : columns) statement.execute("ALTER TABLE Bank" + i + " ADD " + str + ";");
		}
		statement.close();
	}
	
	boolean checkPlayerInDatabase(UUID ID) throws SQLException {
		if (ID == null) throw new SQLException("ID can't be null");
		boolean result = true;
		PreparedStatement statement = AxUtils.getConnection().prepareStatement("SELECT * FROM Economy WHERE UUID=?;");
		statement.setString(1,ID.toString());
		result = result && statement.executeQuery().next();
		statement.close();
		for (int i = 1; i <= getPlayerBankCountFromDatabase(ID); i++) {
			statement = AxUtils.getConnection().prepareStatement("SELECT * FROM Bank" + i + " WHERE UUID=?;");
			statement.setString(1,ID.toString());
			result = result && statement.executeQuery().next();
			statement.close();
		}
		return result;
	}
	
	void addPlayerToDatabase(UUID ID) throws SQLException {
		if (ID == null) throw new SQLException("ID can't be null");
		PreparedStatement statement;
		statement = AxUtils.getConnection().prepareStatement("INSERT INTO Economy (UUID,Balance,Banks,BankBalance) VALUES (?,?,?,?);");
		statement.setString(1,ID.toString());
		statement.setDouble(2,Values.startingBalance);
		statement.setInt(3,Values.startingBanks);
		statement.setInt(4,0);
		try {
			statement.executeUpdate();
		} catch (SQLException e) {}
		statement.close();
		for (int i = 1; i <= Values.startingBanks; i++) {
			statement = AxUtils.getConnection().prepareStatement("INSERT INTO Bank" + i + " (UUID) VALUES (?);");
			statement.setString(1,ID.toString());
			try {
				statement.executeUpdate();
			} catch (SQLException e) {}
			statement.close();
		}
		statement.close();
	}
	
	double getPlayerBalanceFromDatabase(UUID ID) throws SQLException {
		if (ID == null) throw new SQLException("ID can't be null");
		PreparedStatement statement = AxUtils.getConnection().prepareStatement("SELECT * FROM Economy WHERE UUID=?;");
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
		PreparedStatement statement = AxUtils.getConnection().prepareStatement("UPDATE Economy SET Balance=? WHERE UUID=?;");
		statement.setDouble(1,amount);
		statement.setString(2,ID.toString());
		statement.executeUpdate();
		statement.close();
	}
	
	double getPlayerBankBalanceFromDatabase(UUID ID) throws SQLException {
		if (ID == null) throw new SQLException("ID can't be null");
		PreparedStatement statement = AxUtils.getConnection().prepareStatement("SELECT * FROM Economy WHERE UUID=?;");
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
		PreparedStatement statement = AxUtils.getConnection().prepareStatement("UPDATE Economy SET BankBalance=? WHERE UUID=?;");
		statement.setDouble(1,amount);
		statement.setString(2,ID.toString());
		statement.executeUpdate();
		statement.close();
	}
	
	int getPlayerBankCountFromDatabase(UUID ID) throws SQLException {
		if (ID == null) throw new SQLException("ID can't be null");
		PreparedStatement statement = AxUtils.getConnection().prepareStatement("SELECT * FROM Economy WHERE UUID=?;");
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
		PreparedStatement statement = AxUtils.getConnection().prepareStatement("INSERT INTO Bank" + playerBanks + " (UUID) VALUES (?);");
		statement.setString(1,ID.toString());
		statement.executeUpdate();
		statement = AxUtils.getConnection().prepareStatement("UPDATE Economy SET Banks=? WHERE UUID=?;");
		statement.setDouble(1,playerBanks);
		statement.setString(2,ID.toString());
		statement.executeUpdate();
		statement.close();
		return playerBanks;
	}
	
	private List<ItemStack> getPlayerBankFromDatabase(UUID ID, int bank) throws SQLException {
		List<ItemStack> items = new ArrayList<ItemStack>();
		PreparedStatement statement = AxUtils.getConnection().prepareStatement("SELECT * FROM Bank" + bank + " WHERE UUID=?;");
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
		PreparedStatement statement = AxUtils.getConnection().prepareStatement("UPDATE Bank" + bank + " Set " + String.join(",",itemsStr) +
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
}