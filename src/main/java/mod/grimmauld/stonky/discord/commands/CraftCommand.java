package mod.grimmauld.stonky.discord.commands;

import mod.grimmauld.stonky.data.DataManager;
import mod.grimmauld.stonky.data.Rarity;
import mod.grimmauld.stonky.data.TradeElement;
import net.dv8tion.jda.api.EmbedBuilder;
import org.apache.commons.lang3.text.StrBuilder;

import java.util.Comparator;
import java.util.Set;

import static mod.grimmauld.stonky.data.Rarity.*;

public class CraftCommand extends RarityBasedUpdateBoardCommand {

	public CraftCommand(String name, DataManager dataManager) {
		super(name, "Crafting", "Get info on part crafting", dataManager, Set.of(RARE, SPECIAL, EPIC, LEGENDARY));
	}

	@Override
	protected void populateEmbedForRarity(EmbedBuilder eb, Rarity rarity) {
		eb.setTitle(titleForRarity(rarity), "https://crossoutdb.com/#preset=crafting.rarity=" + rarity.rarityName.toLowerCase() + ".craftable=true");
		dataManager.factionManager.getFactions().forEach((factionId, factionName) -> {
			StrBuilder parts = new StrBuilder();
			dataManager.getTradeElements()
				.stream()
				.filter(rarity::contains)
				.filter(TradeElement::isCraftable)
				.filter(tradeElement -> tradeElement.getFactionNumber() == factionId)
				.filter(tradeElement -> tradeElement.getCraftingMargin() > 0)
				.sorted(Comparator.comparing(TradeElement::getCraftingMargin).reversed())
				.limit(5)
				.forEach(tradeElement -> parts.appendln(tradeElement.getLocalizedName() + ": " + tradeElement.getFormatCraftingMargin()));
			if (!parts.isEmpty())
				eb.addField(factionName, parts.toString(), true);
		});
	}
}
