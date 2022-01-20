package mod.grimmauld.stonky.discord.commands;

import mod.grimmauld.stonky.data.DataManager;
import mod.grimmauld.stonky.data.Rarity;
import mod.grimmauld.stonky.discord.GrimmSlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Collection;
import java.util.stream.Collectors;

public abstract class RarityBasedUpdateBoardCommand extends UpdateBoardCommand {
	private static final String RARITY_OPTION_KEY = "rarity";
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
			.map(getOrCreateEmbedForExtraInfo(this::titleForRarity, this::createEmbedForRarity))
			.map(channel::sendMessageEmbeds)
			.forEach(GrimmSlashCommand::submitAndStore);
	}

	private MessageEmbed createEmbedForRarity(Rarity rarity) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(rarity.color);
		eb.setTimestamp(Instant.now());
		populateEmbedForRarity(eb, rarity);
		return eb.build();
	}

	protected abstract void populateEmbedForRarity(EmbedBuilder eb, Rarity rarity);

	protected String titleForRarity(Rarity rarity) {
		return threadName + " " + rarity.rarityName + " parts";
	}

	@Override
	protected MessageEmbed createEmbedForTitle(String title) {
		return createEmbedForRarity(Rarity.byName(title.replaceFirst(titleRegex, "$1")));
	}

	@Override
	protected CommandData attachExtraData(CommandData data) {
		OptionData optionData = new OptionData(OptionType.STRING, RARITY_OPTION_KEY, "Specify a rarity for board creation");
		eligible.forEach(rarity -> optionData.addChoice(rarity.rarityName, rarity.rarityName));
		return super.attachExtraData(data).addOptions(optionData);
	}

	@Override
	public void execute(SlashCommandEvent event) {
		Rarity rarity = event.getOptions()
			.stream()
			.filter(optionMapping -> optionMapping.getName().equals(RARITY_OPTION_KEY) && optionMapping.getType() == OptionType.STRING)
			.map(OptionMapping::getAsString)
			.findFirst()
			.flatMap(Rarity::byNameOptional)
			.orElse(null);

		if (rarity == null) {
			super.execute(event);
			return;
		}

		sendResponse(event, "Creating embed...", true);
		MessageChannel channel = getOrCreateThread(event.getChannel());
		submitAndStore(channel.sendMessageEmbeds(createEmbedForExtraInfo(rarity, this::titleForRarity, this::createEmbedForRarity)));
		if (channel instanceof ThreadChannel threadChannel)
			threadChannel.addThreadMember(event.getUser()).queue();
	}
}
