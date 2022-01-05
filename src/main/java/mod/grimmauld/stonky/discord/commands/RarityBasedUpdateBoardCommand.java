package mod.grimmauld.stonky.discord.commands;

import mod.grimmauld.stonky.data.DataManager;
import mod.grimmauld.stonky.data.Rarity;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.stream.Collectors;

public abstract class RarityBasedUpdateBoardCommand extends UpdateBoardCommand {
	protected final DataManager dataManager;
	protected final Collection<Rarity> eligible;

	protected RarityBasedUpdateBoardCommand(String name, String threadName, @Nullable String help, DataManager dataManager,
											Collection<Rarity> eligible) {
		super(name, threadName, createTitleRegex(eligible, threadName), help);
		this.dataManager = dataManager;
		this.eligible = eligible;
	}

	private static String createTitleRegex(Collection<Rarity> eligible, String threadName) {
		return threadName + " (" + eligible.stream().map(rarity -> "(" + rarity.rarityName + ")")
			.collect(Collectors.joining("|")) + ") parts";
	}

	@Override
	protected void sendMessages(MessageChannel channel) {
		eligible.stream()
			.map(this::createEmbedForRarity)
			.map(channel::sendMessageEmbeds)
			.map(RestAction::complete)
			.forEach(this::storeForUpdates);
	}

	protected abstract MessageEmbed createEmbedForRarity(Rarity rarity);

	protected String titleForRarity(Rarity rarity) {
		return threadName + " " + rarity.rarityName + " parts";
	}

	@Override
	protected MessageEmbed createEmbedForTitle(String title) {
		return createEmbedForRarity(Rarity.byName(title.replaceFirst(titleRegex, "$1")));
	}
}
