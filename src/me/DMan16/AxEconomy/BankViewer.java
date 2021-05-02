package me.DMan16.AxEconomy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import me.Aldreda.AxUtils.Utils.ListenerInventory;
import me.Aldreda.AxUtils.Utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

class BankViewer extends ListenerInventory {
	private HashMap<Integer,List<ItemStack>> originalBank;
	private HashMap<Integer,List<ItemStack>> updatingBank;
	private final boolean owner;
	private final UUID ID;
	private final Player player;
	
	private final static int size = 6 * 9;
	private final int slotPrevious = size - 2;
	private final int slotNext = size - 1;
	private final int slotClose = size - 9;
	private final int slotBankBalance = size - 5;
	private final static NamespacedKey pageKey = Utils.namespacedKey("bank_viewer_page");
	static final ItemStack itemEmpty = makeBankItem(Utils.makeItem(Material.GRAY_STAINED_GLASS_PANE,Component.empty(),ItemFlag.values()));
	private static final ItemStack itemBankBalance = makeBankItem(Utils.makeItem(Material.EMERALD,
			BankBalanceViewer.nameDeposit.append(Component.text("/").color(NamedTextColor.GRAY).append(BankBalanceViewer.nameWithdraw).decoration(TextDecoration.ITALIC,
					false)),ItemFlag.values()));
	
	public BankViewer(Player viewer, UUID ID, String name) {
		super(Bukkit.getServer().createInventory(viewer,size,Component.translatable(Values.translateBank,Values.translateBankColor).append(Component.text(name == null ||
				!viewer.getUniqueId().equals(ID) ? "": " " + name)).decoration(TextDecoration.ITALIC,false)));
		this.owner = name == null || !viewer.getUniqueId().equals(ID);
		this.ID = ID;
		this.player = this.owner ? viewer : null;
		this.originalBank = AxEconomyMain.getEconomy().getBank(ID);
		this.updatingBank = AxEconomyMain.getEconomy().getBank(ID);
		setPage(1);
		this.register(AxEconomyMain.getInstance());
		viewer.openInventory(inventory);
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onCloseSaveEvent(InventoryCloseEvent event) {
		if (owner && event.getPlayer().equals(player) && event.getView().getTopInventory().equals(inventory)) saveBanks();
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onQuitSaveEvent(PlayerQuitEvent event) {
		if (owner && event.getPlayer().equals(player)) saveBanks();
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onClickEvent(InventoryClickEvent event) {
		if (!event.getView().getTopInventory().equals(inventory)) return;
		int slot = event.getRawSlot();
		if (!owner || (slot < size && slot >= size - 9)) event.setCancelled(true);
		if (slot >= size || slot < size - 9 || (!event.isRightClick() && !event.isLeftClick())) return;
		if (slot == slotClose) event.getWhoClicked().closeInventory();
		else if (slot == slotNext) setPage(getPage() + 1);
		else if (slot == slotPrevious) setPage(getPage() - 1);
		else if (this.owner && slot == slotBankBalance) new BankBalanceViewer(this.player);
	}
	
	@Override
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onInventoryDrag(InventoryDragEvent event) {
		if (!event.getView().getTopInventory().equals(inventory)) return;
		if (!owner) event.setCancelled(true);
		else for (int slot : event.getRawSlots()) if (slot < size && slot >= size - 9) {
			event.setCancelled(true);
			return;
		}
	}
	
	private int getPage() {
		ItemStack item = inventory.getItem(slotClose);
		if (Utils.isNull(item)) return -1;
		return item.getItemMeta().getPersistentDataContainer().get(pageKey,PersistentDataType.INTEGER);
	}
	
	private void setPage(int page) {
		if (page < 1 || page > updatingBank.size()) return;
		if (owner) saveBankPage();
		for (int i = 0; i < size - 9; i++) inventory.setItem(i,null);
		for (int i = size - 9; i < size; i++) inventory.setItem(i,itemEmpty);
		List<ItemStack> bank = updatingBank.get(page);
		for (int i = 0; i < bank.size(); i++) inventory.setItem(i,bank.get(i));
		inventory.setItem(slotPrevious,previous(page));
		inventory.setItem(slotNext,next(page));
		inventory.setItem(slotClose,close(page));
		if (this.owner) inventory.setItem(slotBankBalance,itemBankBalance);
	}
	
	private ItemStack previous(int page) {
		int model = (page > 1 && page < updatingBank.size() ? Values.BankViewerPreviousYesModel : Values.BankViewerPreviousNoModel);
		return makeBankItem(Utils.makeItem(Material.ARROW,Component.translatable(Values.translatePrevious,
				Values.translatePreviousColor).decoration(TextDecoration.ITALIC,false),model,ItemFlag.values()));
	}
	
	private ItemStack next(int page) {
		int model = (page > 0 && page < updatingBank.size() - 1 ? Values.BankViewerNextYesModel : Values.BankViewerNextNoModel);
		return makeBankItem(Utils.makeItem(Material.ARROW,Component.translatable(Values.translateNext,
				Values.translateNextColor).decoration(TextDecoration.ITALIC,false),model,ItemFlag.values()));
	}
	
	private ItemStack close(int page) {
		return makeBankItem(Utils.makeItem(Material.BARRIER,Component.translatable(Values.translateClose,
				Values.translateCloseColor).decoration(TextDecoration.ITALIC,false),Values.BankViewerCloseModel,ItemFlag.values()),page);
	}
	
	private void saveBanks() {
		saveBankPage();
		List<Integer> remove = new ArrayList<Integer>();
		for (int i : updatingBank.keySet()) {
			List<ItemStack> itemsOriginal = originalBank.get(i);
			List<ItemStack> itemsUpdating = updatingBank.get(i);
			boolean found = false;
			for (int j = 0; j < itemsUpdating.size(); j++) if (!Utils.sameItem(itemsUpdating.get(j),itemsOriginal.get(j)) || (!Utils.isNull(itemsUpdating.get(j)) &&
					itemsOriginal.get(j).getAmount() != itemsUpdating.get(j).getAmount())) {
				found = true;
				break;
			}
			if (!found) remove.add(i);
		}
		for (int i : remove) updatingBank.remove(i);
		if (player == null) AxEconomyMain.getEconomy().setBank(ID,updatingBank);
		else AxEconomyMain.getEconomy().setBank(player,updatingBank);
	}
	
	private void saveBankPage() {
		int page = getPage();
		if (page <= 0) return;
		List<ItemStack> pageItems = new ArrayList<ItemStack>();
		for (int i = 0; i < size - 9; i++) {
			ItemStack item = inventory.getItem(i);
			pageItems.add(Utils.isNull(item) ? null : item);
		}
		updatingBank.put(page,pageItems);
	}

	static ItemStack makeBankItem(ItemStack item) {
		return makeBankItem(item,0);
	}
	
	private static ItemStack makeBankItem(ItemStack item, int num) {
		ItemMeta meta = item.getItemMeta();
		meta.getPersistentDataContainer().set(pageKey,PersistentDataType.INTEGER,num);
		item.setItemMeta(meta);
		return item;
	}
}