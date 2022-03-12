package mod.grimmauld.stonky.discord.commands;

import mod.grimmauld.stonky.data.Crate;
import mod.grimmauld.stonky.data.DataManager;
import mod.grimmauld.stonky.data.Rarity;
import mod.grimmauld.stonky.data.TradeElement;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.apache.commons.lang3.text.StrBuilder;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CrateCommand extends UpdateBoardCommand {
	private static final DecimalFormat df = new DecimalFormat("0.00");
	private final DataManager dataManager;

	public CrateCommand(String name, DataManager dataManager) {
		super(name, "Crates", "Opening (.+)", "Information on Crate rerolls");
		this.dataManager = dataManager;
	}

	@Override
	protected Stream<MessageEmbed> createEmbedsForEvent(SlashCommandInteractionEvent event) {
		return dataManager.crateManager.crates.stream()
				.map(crate -> crate.getAsTradeElement(dataManager))
				.filter(Objects::nonNull)
				.map(TradeElement::getLocalizedName)
				.map(name -> "Opening " + name)
				.map(this::lazyCreateEmbedForTitle);
	}

	@Override
	@Nullable
	protected MessageEmbed createEmbedForTitle(String title) {
		Crate crate = dataManager.crateManager.getCrateByName(title.replaceFirst(titleRegex, "$1"), dataManager);
		if (crate == null)
			return null;

		EmbedBuilder eb = new EmbedBuilder();
		TradeElement tradeElement = crate.getAsTradeElement(dataManager);
		if (tradeElement != null)
			eb.setColor(Rarity.fromTradeElement(tradeElement).color);

		eb.setTimestamp(Instant.now());
		eb.setTitle(title, "https://crossoutdb.com/item/" + crate.getId());


		List<TradeElement> loot = crate.getContents(dataManager);

		double average = loot.stream().collect(Collectors.summarizingDouble(TradeElement::getSellPrice)).getAverage() * 0.9 / 100;
		double rerollThreshold = average - crate.getRerollCost(dataManager);
		double count = crate.lootSize();

		StrBuilder keep = new StrBuilder();
		StrBuilder reroll = new StrBuilder();

		AtomicInteger goodItems = new AtomicInteger();
		DoubleAdder goodPrices = new DoubleAdder();

		loot.stream()
				.sorted(Comparator.comparing(TradeElement::getSellPrice).reversed())
				.forEach(te -> {
					double price = te.getSellPrice() * 0.9 / 100;
					if (price > rerollThreshold) {
						keep.appendln(te.getLocalizedName()
								+ ": " + te.getFormatSellPrice());
						goodPrices.add(price);
						goodItems.incrementAndGet();
					} else {
						reroll.appendln(te.getLocalizedName()
								+ ": " + te.getFormatSellPrice());
					}
				});

		eb.addField("Keep", keep.toString(), true);
		eb.addField("Reroll", reroll.toString(), true);

		StrBuilder extraInfo = new StrBuilder();
		extraInfo.appendln("Reroll uses " + crate.getRerollAmount() + " Scrap costing " + df.format(crate.getRerollCost(dataManager)));
		extraInfo.appendln("Reroll for items giving less than " + df.format(rerollThreshold) + " after tax");
		extraInfo.appendln("Crate returns avg. " + df.format(average) + " without reroll");
		extraInfo.appendln("Crate returns avg. " + df.format(goodPrices.doubleValue() / count + rerollThreshold * (1 - goodItems.get() / count)) + " with optimal reroll");
		eb.addField("Info", extraInfo.toString(), true);

		return eb.build();
	}
}
