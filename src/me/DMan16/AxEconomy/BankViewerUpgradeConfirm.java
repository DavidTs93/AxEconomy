package me.DMan16.AxEconomy;

import me.Aldreda.AxUtils.Utils.ListenerInventory;
import me.Aldreda.AxUtils.Utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

class BankViewerUpgradeConfirm extends ListenerInventory {
	private final Player player;
	
	private static final ItemStack itemConfirmYes = BankViewer.makeBankItem(Utils.makeItem(Material.GREEN_WOOL,
			Component.translatable(Values.translateUpgradeConfirmConfirm,Values.translateUpgradeConfirmConfirmColor).decoration(TextDecoration.ITALIC,false),ItemFlag.values()));
	private static final ItemStack itemConfirmNo = BankViewer.makeBankItem(Utils.makeItem(Material.GRAY_WOOL,Component.translatable(Values.translateUpgradeConfirmConfirm,
			Values.translateUpgradeConfirmConfirmColor).decoration(TextDecoration.ITALIC,false).decoration(TextDecoration.STRIKETHROUGH,true),
			Arrays.asList(Component.translatable(Values.translateUpgradeConfirmNoFunds,Values.translateUpgradeConfirmNoFundsColor).decoration(TextDecoration.ITALIC,false)),ItemFlag.values()));
	private static final ItemStack itemCost = BankViewer.makeBankItem(Utils.makeItem(Material.PAPER,
			Component.translatable(Values.translatePurchase,NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC,false),ItemFlag.values()));
	private static final ItemStack itemCancel = BankViewer.makeBankItem(Utils.makeItem(Material.RED_WOOL,
			Component.translatable(Values.translateUpgradeConfirmCancel,Values.translateUpgradeConfirmCancelColor).decoration(TextDecoration.ITALIC,false),ItemFlag.values()));
	private static final int slotConfirm = 0;
	private static final int slotCost = 2;
	private static final int slotCancel = 4;
	private final boolean canUpgrade;
	private final double cost;
	
	public BankViewerUpgradeConfirm(Player player) {
		super(Bukkit.getServer().createInventory(player,InventoryType.HOPPER,Component.translatable(Values.translateBank,
				Values.translateBankColor).decoration(TextDecoration.ITALIC,false)));
		this.player = player;
		int banks = AxEconomyMain.getEconomy().getBanksCount(player);
		this.cost = Values.upgradeBankBalanceBanks.get(banks - Values.startingBanks);
		this.canUpgrade = AxEconomyMain.getEconomy().has(player,cost);
		this.inventory.setItem(slotConfirm,canUpgrade ? itemConfirmYes : itemConfirmNo);
		ItemStack itemCost = this.itemCost.clone();
		ItemMeta meta = itemCost.getItemMeta();
		meta.displayName(((TranslatableComponent) meta.displayName()).args(Component.translatable(Values.translatePage).args(Component.text("#" +
				(banks + 1)).decoration(TextDecoration.ITALIC,false))));
		meta.lore(Arrays.asList(Component.text(this.cost,Values.costColor).decoration(TextDecoration.ITALIC,false)));
		itemCost.setItemMeta(meta);
		this.inventory.setItem(slotCost,itemCost);
		this.inventory.setItem(slotCancel,itemCancel);
		this.register(AxEconomyMain.getInstance());
		player.openInventory(inventory);
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onClickEvent(InventoryClickEvent event) {
		if (!event.getView().getTopInventory().equals(inventory)) return;
		event.setCancelled(true);
		int slot = event.getRawSlot();
		if (slot > 4 || (!event.isRightClick() && !event.isLeftClick())) return;
		if (slot == slotConfirm && canUpgrade) if (AxEconomyMain.getEconomy().withdrawPlayer(player,this.cost).transactionSuccess())
			if (!AxEconomyMain.getEconomy().increaseBanksCount(player).transactionSuccess()) AxEconomyMain.getEconomy().depositPlayer(player,this.cost);
		if (slot == slotCancel || (slot == slotConfirm && canUpgrade)) new BankViewer(player,player.getUniqueId(),null);
	}
}