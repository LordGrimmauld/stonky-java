package mod.grimmauld.stonky.discord.commands;

import mod.grimmauld.stonky.Main;
import mod.grimmauld.stonky.discord.GrimmSlashCommand;
import mod.grimmauld.stonky.util.StreamUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.cache.SortedSnowflakeCacheView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class UpdateBoardCommand extends GrimmSlashCommand {
	protected final String threadName;
	protected final String titleRegex;

	protected UpdateBoardCommand(String name, String threadName, String titleRegex, @Nullable String help) {
		super(name, help);
		this.threadName = threadName;
		this.titleRegex = titleRegex;
	}

	public static void updateBoards() {
		JDA jda = Main.DISCORD_BOT.getJda();
		if (jda == null)
			return;
		Set<UpdateBoardCommand> updateBoardCommands = Main.COMMAND_REGISTRY.stream()
			.filter(UpdateBoardCommand.class::isInstance)
			.map(UpdateBoardCommand.class::cast)
			.collect(Collectors.toSet());
		Stream.concat(jda.getPrivateChannelCache().parallelStream(),
				getAllThreads(jda)
					.filter(ThreadChannel::isOwner)
					.filter(threadChannel -> updateBoardCommands.stream()
						.map(UpdateBoardCommand::getThreadName)
						.anyMatch(threadChannel.getName()::equals))
					.filter(StreamUtils.distinctByKey(ThreadChannel::getName))
					.peek(threadChannel -> {
						if (threadChannel.isArchived())
							threadChannel.getManager().setArchived(false).complete();
					})
			)
			.map(MessageChannel::retrievePinnedMessages)
			.map(RestAction::complete)
			.flatMap(List::stream)
			.filter(message -> message.getAuthor().equals(jda.getSelfUser()))
			.forEach(message -> updateBoardCommands.forEach(updateBoardCommand -> updateBoardCommand.tryUpdateEmbeds(message)));
	}

	@NotNull
	private static Stream<ThreadChannel> getAllThreads(JDA jda) {
		return Stream.concat(
			jda.getThreadChannelCache().parallelStream(),
			jda.getGuildCache()
				.parallelStream()
				.map(Guild::getTextChannelCache)
				.flatMap(SortedSnowflakeCacheView::parallelStreamUnordered)
				.map(IThreadContainer::retrieveArchivedPublicThreadChannels)
				.map(RestAction::complete)
				.flatMap(List::stream)
		);
	}


	@Override
	public void execute(SlashCommandEvent event) {
		sendResponse(event, "Creating thread...", true);
		MessageChannel channel = getOrCreateThread(event.getChannel());
		sendMessages(channel);
		if (channel instanceof ThreadChannel threadChannel)
			threadChannel.addThreadMember(event.getUser()).queue();
	}

	protected MessageChannel getOrCreateThread(MessageChannel original) {
		if ((original instanceof ThreadChannel threadChannel1 ? threadChannel1.getParentMessageChannel() : original)
			instanceof IThreadContainer guildMessageChannel)
			return guildMessageChannel.getThreadChannels()
				.stream()
				.filter(threadChannel -> threadChannel.getName().equals(threadName))
				.findFirst()
				.orElseGet(() -> guildMessageChannel.createThreadChannel(threadName).complete());
		return original;
	}

	protected abstract void sendMessages(MessageChannel channel);

	public boolean matchesTitleRegex(MessageEmbed embed) {
		String title = embed.getTitle();
		return title != null && title.matches(titleRegex);
	}

	public MessageEmbed getUpdatedEmbed(MessageEmbed oldEmbed) {
		String title = oldEmbed.getTitle();
		if (title == null || !title.matches(titleRegex)) {
			Main.LOGGER.error("Updateable embed title does not match title regex {}", titleRegex);
			return new EmbedBuilder().build();
		}
		return createEmbedForTitle(title);
	}

	protected abstract MessageEmbed createEmbedForTitle(String title);

	public void tryUpdateEmbeds(Message message) {
		List<MessageEmbed> newEmbeds = message.getEmbeds()
			.stream()
			.filter(this::matchesTitleRegex)
			.map(this::getUpdatedEmbed)
			.toList();
		if (!newEmbeds.isEmpty())
			message.editMessageEmbeds(newEmbeds).submit();
	}

	public String getThreadName() {
		return threadName;
	}
}
