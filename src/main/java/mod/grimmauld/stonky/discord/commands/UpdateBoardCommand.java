package mod.grimmauld.stonky.discord.commands;

import mod.grimmauld.stonky.Main;
import mod.grimmauld.stonky.discord.GrimmSlashCommand;
import mod.grimmauld.stonky.util.StreamUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.requests.restaction.pagination.PaginationAction;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
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
		Stream.concat(jda.getPrivateChannels().stream(),
				Stream.concat(
						jda.getThreadChannels().stream(),
						jda.getGuilds()
							.stream()
							.map(Guild::getTextChannels)
							.flatMap(Collection::stream)
							.map(IThreadContainer::retrieveArchivedPublicThreadChannels)
							.flatMap(PaginationAction::stream)
					)
					.filter(ThreadChannel::isOwner)
					.filter(StreamUtils.distinctByKey(ThreadChannel::getName))
					.filter(threadChannel -> Main.COMMAND_REGISTRY.stream()
						.filter(UpdateBoardCommand.class::isInstance)
						.map(UpdateBoardCommand.class::cast)
						.map(UpdateBoardCommand::getThreadName)
						.anyMatch(threadChannel.getName()::equals))
					.peek(threadChannel -> {
						if (threadChannel.isArchived())
							threadChannel.getManager().setArchived(false).queue();
					})
			)
			.map(MessageChannel::retrievePinnedMessages)
			.forEach(listRestAction -> listRestAction.queue(collection -> collection.stream().filter(message -> message.getAuthor().equals(jda.getSelfUser()))
				.forEach(message -> Main.COMMAND_REGISTRY.stream()
					.filter(UpdateBoardCommand.class::isInstance)
					.map(UpdateBoardCommand.class::cast)
					.forEach(updateBoardCommand -> updateBoardCommand.tryUpdateEmbeds(message)))));
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
		Collection<MessageEmbed> newEmbeds = message.getEmbeds()
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
