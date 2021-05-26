package me.DMan16.AxEconomy;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import java.util.Arrays;
import java.util.List;

class Values {
	static final int BankViewerPreviousYesModel = 0;
	static final int BankViewerPreviousNoModel = 0;
	static final int BankViewerNextYesModel = 0;
	static final int BankViewerNextNoModel = 0;
	static final int BankViewerCloseModel = 0;
	static final int BankBalanceViewerPreviousModel = 0;
	static final double maxBalance = 1e12;
	static final double minBalance = 0;
	static final int maxBanks = 23;
	static final int perBank = 45;
	static final double startingBalance = 0;
	static final int startingBanks = 3;
	static final List<Double> maxBankBalanceBanks = Arrays.asList(1e6,1e7,2e7,5e7,1e8,2e8,5e8,1e9,2e9,5e9,1e10,2e10,5e10,1e11,2e11,3e11,4e11,5e11,6e11,8e11,1e12);
	static final List<Double> upgradeBankBalanceBanks = Arrays.asList(2e4,4e4,6e4,8e4,10e4,12e4,14e4,16e4,18e4,20e4,22e4,24e4,26e4,28e4,30e4,32e4,34e4,36e4,38e4,40e4);
	static final String translateBalance = "bank.aldreda.balance";
	static final NamedTextColor translateBalanceColor = NamedTextColor.GOLD;
	static final String translateDeposit = "bank.aldreda.deposit";
	static final NamedTextColor translateDepositColor = NamedTextColor.AQUA;
	static final String translateWithdraw = "bank.aldreda.withdraw";
	static final NamedTextColor translateWithdrawColor = NamedTextColor.GREEN;
	static final String translateAll = "gui.all";
	static final String translatePrevious = "spectatorMenu.previous_page";
	static final NamedTextColor translatePreviousColor = NamedTextColor.GOLD;
	static final String translateNext = "spectatorMenu.next_page";
	static final NamedTextColor translateNextColor = NamedTextColor.AQUA;
	static final String translateClose = "spectatorMenu.close";
	static final NamedTextColor translateCloseColor = NamedTextColor.RED;
	static final String translateBank = "bank.aldreda.bank";
	static final TextColor translateBankColor = NamedTextColor.DARK_GREEN;
	static final String translateUpgradeConfirmConfirm = "gui.ok";
	static final TextColor translateUpgradeConfirmConfirmColor = NamedTextColor.GREEN;
	static final String translateUpgradeConfirmNoFunds = "bank.aldreda.no_funds";
	static final TextColor translateUpgradeConfirmNoFundsColor = NamedTextColor.RED;
	static final String translateUpgradeConfirmCancel = "gui.cancel";
	static final TextColor translateUpgradeConfirmCancelColor = NamedTextColor.RED;
	static final String translatePage = "bank.aldreda.page";
	static final String translatePurchase = "bank.aldreda.purchase_x";
	static final TextColor costColor = NamedTextColor.YELLOW;
	static final String nameBanker = "banker";
}