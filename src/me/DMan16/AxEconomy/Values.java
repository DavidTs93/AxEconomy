package me.DMan16.AxEconomy;

import java.util.Arrays;
import java.util.List;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

class Values {
	static final int BankViewerPreviousYesModel = 2;
	static final int BankViewerPreviousNoModel = 1;
	static final int BankViewerNextYesModel = 4;
	static final int BankViewerNextNoModel = 3;
	static final int BankViewerCloseModel = 5;
	static final int BankBalanceViewerPreviousModel = 2;
	static final double maxBalance = 1e12;
	static final double minBalance = 0;
	static final int maxBanks = 23;
	static final int perBank = 45;
	static final double startingBalance = 0;
	static final int startingBanks = 3;
	static final List<Double> maxBankBalanceBanks = Arrays.asList(1e6,1e7,2e7,5e7,1e8,2e8,5e8,1e9,2e9,5e9,1e10,2e10,5e10,1e11,2e11,3e11,4e11,5e11,6e11,8e11,1e12);
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
	static final String nameBanker = "banker";
}