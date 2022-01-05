package mod.grimmauld.stonky.discord.commands;

import mod.grimmauld.stonky.data.DataManager;
import mod.grimmauld.stonky.data.Rarity;
import mod.grimmauld.stonky.data.TradeElement;
import net.dv8tion.jda.api.EmbedBuilder;
import org.apache.commons.lang3.text.StrBuilder;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.DoubleAdder;

import static mod.grimmauld.stonky.data.Rarity.*;

public class SalvageCommand extends RarityBasedUpdateBoardCommand {
	protected static final Map<Rarity, Collection<ImmutablePair<Integer, Integer>>> SALVAGE_RETURNS = new EnumMap<>(Rarity.class);
	private static final DecimalFormat df = new DecimalFormat("0.00");

	static {
		SALVAGE_RETURNS.put(RARE, List.of(
			ImmutablePair.of(53, 150), // Scrap
			ImmutablePair.of(43, 50) // Copper
		));
		SALVAGE_RETURNS.put(SPECIAL, List.of(
			ImmutablePair.of(53, 40), // Scrap
			ImmutablePair.of(43, 100), // Copper
			ImmutablePair.of(85, 60), // Wires
			ImmutablePair.of(785, 30) // Plastic
		));
		SALVAGE_RETURNS.put(EPIC, List.of(
			ImmutablePair.of(53, 80), // Scrap
			ImmutablePair.of(43, 150), // Copper
			ImmutablePair.of(85, 170), // Wires
			ImmutablePair.of(785, 80) // Plastic
		));
		SALVAGE_RETURNS.put(LEGENDARY, List.of(
			ImmutablePair.of(53, 50), // Scrap
			ImmutablePair.of(43, 250), // Copper
			ImmutablePair.of(168, 250), // Electronics
			ImmutablePair.of(784, 250) // batteries
		));
	}

	public SalvageCommand(String name, DataManager dataManager) {
		super(name, "Salvaging", "Get info on part salvaging", dataManager, SALVAGE_RETURNS.keySet());
	}

	@Override
	protected void populateEmbedForRarity(EmbedBuilder eb, Rarity rarity) {
		Collection<ImmutablePair<Integer, Integer>> salvageRecipe = SALVAGE_RETURNS.get(rarity);
		eb.setTitle(titleForRarity(rarity), "https://crossoutdb.com/tools/salvage");
		if (salvageRecipe == null)
			return;

		StrBuilder resources = new StrBuilder();
		DoubleAdder sellPrice = new DoubleAdder();
		DoubleAdder buyPrice = new DoubleAdder();
		salvageRecipe.stream()
			.map(pair -> ImmutablePair.of(dataManager.getById(pair.getLeft()), pair.getRight()))
			.filter(pair -> pair.getLeft() != null).forEach(tradeElementIntegerPair -> {
				TradeElement tradeElement = tradeElementIntegerPair.getLeft();
				int amount = tradeElementIntegerPair.getRight();
				resources.appendln(tradeElement.getStrippedName() + " " + amount);
				sellPrice.add(tradeElement.getSellPrice() * amount / (tradeElement.getAmount() * 100d));
				buyPrice.add(tradeElement.getBuyPrice() * amount / (tradeElement.getAmount() * 100d));
			});
		eb.addField("Resources", resources.toString(), true);
		eb.addField("Coins", "Sell total " + df.format(sellPrice.doubleValue()) + "\nSell total with Tax " + df.format(sellPrice.doubleValue() * 0.9)
			+ "\nBuy total " + df.format(buyPrice.doubleValue()) + "\nBuy total with Tax " + df.format(buyPrice.doubleValue() * 0.9), true);
	}
}
