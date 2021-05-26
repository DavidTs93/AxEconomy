package me.DMan16.AxEconomy;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.Aldreda.AxUtils.Classes.Listener;
import me.Aldreda.AxUtils.Utils.ListenerInventory;
import me.Aldreda.AxUtils.Utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

class BankViewerBalance extends ListenerInventory {
	private final Player player;
	
	private final static int size = 1 * 9;
	private static final ItemStack itemBalance = BankViewer.makeBankItem(Utils.makeItem(Material.EMERALD,
			Component.translatable(Values.translateBalance,Values.translateBalanceColor).decoration(TextDecoration.ITALIC,false),ItemFlag.values()));
	static final Component nameDeposit = Component.translatable(Values.translateDeposit,Values.translateDepositColor).decoration(TextDecoration.ITALIC,false);
	static final Component nameWithdraw = Component.translatable(Values.translateWithdraw,Values.translateWithdrawColor).decoration(TextDecoration.ITALIC,false);
	private static final ItemStack itemDeposit = BankViewer.makeBankItem(Utils.makeItem(Material.EMERALD,nameDeposit,ItemFlag.values()));
	private static final ItemStack itemWithdraw = BankViewer.makeBankItem(Utils.makeItem(Material.EMERALD,nameWithdraw,ItemFlag.values()));
	private static final ItemStack itemDepositAll = BankViewer.makeBankItem(Utils.makeItem(Material.EMERALD,nameDeposit.append(Component.text(" - ").append(
			Component.translatable(Values.translateAll).color(Values.translateDepositColor).decoration(TextDecoration.ITALIC,false))),ItemFlag.values()));
	private static final ItemStack itemWithdrawAll = BankViewer.makeBankItem(Utils.makeItem(Material.EMERALD,nameWithdraw.append(Component.text(" - ").append(
			Component.translatable(Values.translateAll).color(Values.translateWithdrawColor).decoration(TextDecoration.ITALIC,false))),ItemFlag.values()));
	private static final ItemStack previous = BankViewer.makeBankItem(Utils.makeItem(Material.ARROW,Component.translatable(Values.translatePrevious,
			Values.translatePreviousColor).decoration(TextDecoration.ITALIC,false),Values.BankBalanceViewerPreviousModel,ItemFlag.values()));
	private static final int slotPrevious = 0;
	private static final int slotDepositAll = 2;
	private static final int slotDeposit = 3;
	private static final int slotBalance = 4;
	private static final int slotWithdraw = 5;
	private static final int slotWithdrawAll = 6;
	
	public BankViewerBalance(Player player) {
		super(Bukkit.getServer().createInventory(player,size,Component.translatable(Values.translateBank,
				Values.translateBankColor).decoration(TextDecoration.ITALIC,false)));
		this.player = player;
		for (int i = 0; i < size; i++) this.inventory.setItem(i,BankViewer.itemEmpty);
		this.inventory.setItem(slotPrevious,previous);
		this.inventory.setItem(slotDeposit,itemDeposit);
		this.inventory.setItem(slotWithdraw,itemWithdraw);
		update();
		this.register(AxEconomyMain.getInstance());
		player.openInventory(inventory);
	}
	
	private void update() {
		double bank = AxEconomyMain.getEconomy().getBankBalance(player);
		ItemStack balance = itemBalance.clone();
		ItemMeta meta = balance.getItemMeta();
		meta.lore(Arrays.asList(Component.text(AxEconomyMain.getEconomy().format(bank,true),NamedTextColor.GOLD).decoration(TextDecoration.ITALIC,false)));
		balance.setItemMeta(meta);
		this.inventory.setItem(slotBalance,balance);
		double limit = AxEconomyMain.getEconomy().getMaxBankBalance(player);
		ItemStack depositAll = itemDepositAll.clone();
		meta = depositAll.getItemMeta();
		meta.lore(Arrays.asList(nameDeposit.append(Component.text(": ").color(nameDeposit.color()).append(Component.text(AxEconomyMain.getEconomy().format(Math.min(limit - bank,
				AxEconomyMain.getEconomy().getBalance(player)))).decoration(TextDecoration.ITALIC,false)))));
		depositAll.setItemMeta(meta);
		this.inventory.setItem(slotDepositAll,depositAll);
		ItemStack withdrawAll = itemWithdrawAll.clone();
		meta = withdrawAll.getItemMeta();
		meta.lore(Arrays.asList(nameWithdraw.append(Component.text(": ").color(nameWithdraw.color()).append(Component.text(AxEconomyMain.getEconomy().format(bank)).decoration(
				TextDecoration.ITALIC,false)))));
		withdrawAll.setItemMeta(meta);
		this.inventory.setItem(slotWithdrawAll,withdrawAll);
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onClickEvent(InventoryClickEvent event) {
		if (!event.getView().getTopInventory().equals(inventory)) return;
		int slot = event.getRawSlot();
		if (slot >= size || (!event.isRightClick() && !event.isLeftClick())) return;
		event.setCancelled(true);
		if (slot == slotPrevious) new BankViewer(player,player.getUniqueId(),null);
		else if (slot == slotDepositAll) deposit(AxEconomyMain.getEconomy().getBalance(player));
		else if (slot == slotDeposit || slot == slotWithdraw) {
			cancelCloseUnregister = true;
			try {
				new PriceListener(slot == slotDeposit);
			} catch (Exception e) {
				cancelCloseUnregister = false;
			}
		} else if (slot == slotWithdrawAll) withdraw(AxEconomyMain.getEconomy().getBankBalance(player));
	}
	
	private void withdraw(double amount) {
		if (AxEconomyMain.getEconomy().depositPlayer(player,amount).transactionSuccess())
			if (AxEconomyMain.getEconomy().withdrawBankPlayer(player,amount).transactionSuccess()) update();
			else AxEconomyMain.getEconomy().withdrawPlayer(player,amount);
	}
	
	private void deposit(double amount) {
		amount = Math.min(AxEconomyMain.getEconomy().getMaxBankBalance(player) - AxEconomyMain.getEconomy().getBankBalance(player),amount);
		if (AxEconomyMain.getEconomy().withdrawPlayer(player,amount).transactionSuccess())
			if (AxEconomyMain.getEconomy().depositBankPlayer(player,amount).transactionSuccess()) update();
			else AxEconomyMain.getEconomy().depositPlayer(player,amount);
	}
	
	private class PriceListener extends Listener {
		private double limit;
		private boolean deposit;
		
		public PriceListener(boolean deposit) throws Exception {
			double max = AxEconomyMain.getEconomy().getMaxBankBalance(player);
			double balance = AxEconomyMain.getEconomy().getBankBalance(player);
			limit = deposit ? max - balance : balance;
			if (limit == 0) throw new Exception();
			this.deposit = deposit;
			player.closeInventory();
			player.sendMessage(Component.translatable(deposit ? Values.translateDeposit : Values.translateWithdraw).append(Component.text(": 0 - " +
					limit)).color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC,false));
			Utils.addCancelledPlayer(player,true,false);
			register(AxEconomyMain.getInstance());
		}
		
		@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
		public void onChatAmount(AsyncChatEvent event) {
			if (!event.getPlayer().equals(player)) return;
			event.setCancelled(true);
			boolean done = false;
			try {
				String read = ((TextComponent) event.message()).content().trim();
				double amount = Double.parseDouble(read);
				if (amount < 0 || amount > limit) throw new Exception();
				if (this.deposit) deposit(amount);
				else withdraw(amount);
				done = true;
			} catch (ClassCastException e) {
				done = true;
			} catch (Exception e) {
				Utils.chatColors(player,"&cError"); // error
			}
			if (done) {
				cancelCloseUnregister = false;
				unregister();
				Utils.removeCancelledPlayer(player);
				new BukkitRunnable() {
					public void run() {
						player.openInventory(inventory);
					}
				}.runTask(AxEconomyMain.getInstance());
			}
		}
		
		@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
		public void unregisterOnLeaveEvent(PlayerQuitEvent event) {
			if (event.getPlayer().getUniqueId().equals(player.getUniqueId())) unregister();
		}
	}
}