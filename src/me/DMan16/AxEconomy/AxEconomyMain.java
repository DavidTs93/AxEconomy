package me.DMan16.AxEconomy;

import me.Aldreda.AxUtils.AxUtils;
import me.Aldreda.AxUtils.Utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public class AxEconomyMain extends JavaPlugin {
	private static AxEconomyMain instance = null;
	private static AxEconomy economy = null;
	private static MySQL SQL = null;
	
	public void onEnable() {
		instance = this;
		try {
			SQL = new MySQL();
		} catch (SQLException e) {
			Utils.chatColorsLogPlugin("&fAxEconomy &bMySQL connection: &cFAILURE!!!");
			this.getLogger().severe("MySQL error: ");
			e.printStackTrace();
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		economy = new AxEconomy();
		this.getServer().getServicesManager().register(net.milkbowl.vault.economy.Economy.class,economy,this,ServicePriority.Highest);
		AxUtils.AxEconomyReady();
		new PlayerListener();
		new CommandListener();
		new PlayerCommandListener();
		if (AxUtils.getPAPIManager() != null) new PlaceHolder().register();
		if (AxUtils.getCitizensManager() != null) AxUtils.getCitizensManager().registerTrait(Banker.class,Values.nameBanker);
		Utils.chatColorsLogPlugin("&fAxEconomy &aloaded! MySQL connection &2SUCCESS&a!");
	}
	
	public void onDisable() {
		Bukkit.getScheduler().cancelTasks(this);
		Utils.chatColorsLogPlugin("&fAxEconomy &adisabed");
	}
	
	static AxEconomyMain getInstance() {
		return instance;
	}
	
	static MySQL getSQL() {
		return SQL;
	}
	
	static AxEconomy getEconomy() {
		return economy;
	}
}