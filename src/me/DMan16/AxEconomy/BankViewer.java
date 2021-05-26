package me.DMan16.AxEconomy;

import me.Aldreda.AxUtils.Utils.ListenerInventoryPages;
import me.Aldreda.AxUtils.Utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

class BankViewer extends ListenerInventoryPages {
	private final static NamespacedKey pageKey = Utils.namespacedKey("bank_viewer_page");
	static final ItemStack itemEmpty = makeBankItem(Utils.makeItem(Material.GRAY_STAINED_GLASS_PANE,Component.empty(),ItemFlag.values()));
	private static final ItemStack itemBankBalance = makeBankItem(Utils.makeItem(Material.EMERALD,
			BankViewerBalance.nameDeposit.append(Component.text("/").color(NamedTextColor.GRAY).append(BankViewerBalance.nameWithdraw).decoration(TextDecoration.ITALIC,
					false)),ItemFlag.values()));
	private static final ItemStack itemBankPurchasePage = makeBankItem(Utils.makeItem(Material.PAPER,
			Component.translatable(Values.translatePurchase,NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC,false),ItemFlag.values()));
	private int slotBankBalance;
	private int slotPurchasePage;
	
	private HashMap<Integer,List<ItemStack>> originalBank;
	private HashMap<Integer,List<ItemStack>> updatingBank;
	private boolean owner;
	private UUID ID;
	private boolean first;
	private boolean allowEdit;
	
	public BankViewer(Player viewer, UUID ID, String name) {
		this(viewer,ID,name,false);
	}
	
	public BankViewer(Player viewer, UUID ID, String name, boolean allowEdit) {
		super(viewer,viewer,5,Component.translatable(Values.translateBank,Values.translateBankColor).append(Component.text(name == null ||
				!viewer.getUniqueId().equals(ID) ? "": " " + name)).decoration(TextDecoration.ITALIC,false),AxEconomyMain.getInstance(),ID,name,allowEdit);
	}
	
	@Override
	protected void first(Object ... objs) {
		this.ID = (UUID) objs[0];
		this.owner = ((String) objs[1]) == null || !this.player.getUniqueId().equals(this.ID);
		this.allowEdit = this.owner || (boolean) objs[2];
		//if (!this.owner) this.player = null;
		this.originalBank = AxEconomyMain.getEconomy().getBank(this.ID);
		this.updatingBank = AxEconomyMain.getEconomy().getBank(this.ID);
		closeSlot = size - 9;
		nextSlot = size - 1;
		previousSlot = size - 2;
		slotBankBalance = size - 5;
		slotPurchasePage = size - 8;
		/*alwaysSetNext = true;
		alwaysSetPrevious = true;*/
		first = true;
	}
	
	@Override
	protected boolean isEmpty(ItemStack item) {
		return super.isEmpty(item) || Utils.sameItem(itemEmpty,item);
	}
	
	@Override
	protected boolean firstSlotCheck(int slot, ClickType click) {
		return false;
	}
	
	@Override
	protected boolean cancelCheck(int slot, ClickType click) {
		return !owner || (slot < size && slot >= size - 9);
	}
	
	@Override
	protected boolean secondSlotCheck(int slot, ClickType click) {
		return slot >= size || slot < size - 9 || click.isCreativeAction();
	}
	
	@Override
	protected void otherSlot(InventoryClickEvent event, int slot, ItemStack slotItem) {
		if (!this.owner) return;
		if (slot == slotBankBalance) new BankViewerBalance(this.player);
		else if (slot == slotPurchasePage) new BankViewerUpgradeConfirm(this.player);
	}
	
	@Override
	public int maxPage() {
		return updatingBank.size();
	}
	
	@Override
	protected void reset() {
		for (int i = 0; i < size - 9; i++) inventory.setItem(i,null);
		for (int i = size - 9; i < size; i++) inventory.setItem(i,itemEmpty);
	}
	
	@Override
	protected void setPageContents(int page) {
		List<ItemStack> bank = updatingBank.get(page);
		for (int i = 0; i < bank.size(); i++) inventory.setItem(i,bank.get(i));
		if (this.owner) {
			inventory.setItem(slotBankBalance,itemBankBalance);
			int banks = AxEconomyMain.getEconomy().getBanksCount(player);
			if (banks > 0 && banks < Values.maxBanks) {
				ItemStack purchasePage = itemBankPurchasePage.clone();
				ItemMeta meta = purchasePage.getItemMeta();
				meta.displayName(((TranslatableComponent) meta.displayName()).args(Component.translatable(Values.translatePage).args(Component.text("#" +
						(banks + 1)).decoration(TextDecoration.ITALIC,false))));
				purchasePage.setItemMeta(meta);
				inventory.setItem(slotPurchasePage,purchasePage);
			}
		}
	}
	
	@Override
	protected void beforeSetPage(int page) {
		if (this.allowEdit) saveBankPage();
	}
	
	@Override
	protected ItemStack close(int page) {
		return makeBankItem(Utils.makeItem(Material.BARRIER,Component.translatable(Values.translateClose,
				Values.translateCloseColor).decoration(TextDecoration.ITALIC,false),Values.BankViewerCloseModel,ItemFlag.values()),page);
	}
	
	@Override
	protected ItemStack next(int page) {
		int model = (page > 0 && page < updatingBank.size() - 1 ? Values.BankViewerNextYesModel : Values.BankViewerNextNoModel);
		return makeBankItem(Utils.makeItem(Material.ARROW,Component.translatable(Values.translateNext,
				Values.translateNextColor).append(Component.text(" (" + (page + 1) + ")")).decoration(TextDecoration.ITALIC,false),model,ItemFlag.values()));
	}
	
	@Override
	protected ItemStack previous(int page) {
		int model = (page > 1 && page < updatingBank.size() ? Values.BankViewerPreviousYesModel : Values.BankViewerPreviousNoModel);
		return makeBankItem(Utils.makeItem(Material.ARROW,
				Component.translatable(Values.translatePrevious,Values.translatePreviousColor).append(Component.text(" (" + (page - 1) + ")")).decoration(TextDecoration.ITALIC,false),
				model,ItemFlag.values()));
	}
	
	@Override
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onInventoryDrag(InventoryDragEvent event) {
		if (!event.getView().getTopInventory().equals(inventory)) return;
		if (!this.allowEdit) event.setCancelled(true);
		else for (int slot : event.getRawSlots()) if (slot < size && slot >= size - 9) {
			event.setCancelled(true);
			return;
		}
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onCloseSaveEvent(InventoryCloseEvent event) {
		if (this.allowEdit && event.getView().getTopInventory().equals(inventory) && event.getPlayer().equals(player) && !cancelCloseUnregister) saveBanks();
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onQuitSaveEvent(PlayerQuitEvent event) {
		if (this.allowEdit && event.getPlayer().equals(player)) saveBanks();
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
		if (!this.owner) AxEconomyMain.getEconomy().setBank(ID,updatingBank);
		else AxEconomyMain.getEconomy().setBank(player,updatingBank);
	}
	
	private void saveBankPage() {
		if (first) {
			first = false;
			return;
		}
		List<ItemStack> pageItems = new ArrayList<ItemStack>();
		for (int i = 0; i < size - 9; i++) {
			ItemStack item = inventory.getItem(i);
			pageItems.add(Utils.isNull(item) ? null : item);
		}
		updatingBank.put(currentPage,pageItems);
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