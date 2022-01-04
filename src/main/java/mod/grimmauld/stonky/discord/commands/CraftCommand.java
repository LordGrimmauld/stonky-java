package mod.grimmauld.stonky.discord.commands;

import mod.grimmauld.stonky.data.DataManager;
import mod.grimmauld.stonky.data.Rarity;
import mod.grimmauld.stonky.data.TradeElement;
import mod.grimmauld.stonky.discord.GrimmSlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.requests.RestAction;
import org.apache.commons.lang3.text.StrBuilder;

import java.util.Comparator;
import java.util.Set;

import static mod.grimmauld.stonky.data.Rarity.*;

public class CraftCommand extends GrimmSlashCommand {
	private static final Set<Rarity> CONSIDERED_CRAFTABLE = Set.of(RARE, SPECIAL, EPIC, LEGENDARY);
	private final DataManager dataManager;

	public CraftCommand(String name, DataManager dataManager) {
		super(name, "Get info on part crafting");
		this.dataManager = dataManager;
	}

	@Override
	public void execute(SlashCommandEvent event) {
		sendResponse(event, "Creating thread...", true);
		trySendInThread("Crafting", event.getChannel(), event.getMember(),
			channel -> CONSIDERED_CRAFTABLE.stream()
				.map(this::createEmbedForRarity)
				.map(channel::sendMessageEmbeds)
				.map(RestAction::complete)
				.forEach(this::storeForUpdates));
	}

	private MessageEmbed createEmbedForRarity(Rarity rarity) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Crafting " + rarity.rarityName + " parts", "https://crossoutdb.com/#preset=crafting.rarity=" + rarity.rarityName.toLowerCase() + ".craftable=true");
		eb.setColor(rarity.color);
		dataManager.factionManager.getFactions().forEach((factionId, factionName) -> {
			StrBuilder parts = new StrBuilder();
			dataManager.getTradeElements()
				.stream()
				.filter(rarity::contains)
				.filter(TradeElement::isCraftable)
				.filter(tradeElement -> tradeElement.getFactionNumber() == factionId)
				.sorted(Comparator.comparing(TradeElement::getCraftingMargin).reversed())
				.limit(5)
				.forEach(tradeElement -> parts.appendln(tradeElement.getLocalizedName() + ": " + tradeElement.getFormatCraftingMargin()));
			if (!parts.isEmpty())
				eb.addField(factionName, parts.toString(), true);
		});
		return eb.build();
	}
}
