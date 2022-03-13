package mod.grimmauld.stonky.discord.commands;

import mod.grimmauld.stonky.Main;
import mod.grimmauld.stonky.discord.GrimmSlashCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class UpdateBoardCommand extends GrimmSlashCommand {
	private static final Map<String, MessageEmbed> bufferedEmbeds = new ConcurrentHashMap<>();
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
		jda.getPrivateChannelCache().forEach(UpdateBoardCommand::updateMessagesInChannel);
		executeInAllThreads(jda, threadChannel -> {
			if (getUpdateBoardCommandStream().map(UpdateBoardCommand::getThreadName).noneMatch(threadChannel.getName()::equals))
				return;
			if (threadChannel.isArchived())
				threadChannel.getManager().setArchived(false).queue();
			updateMessagesInChannel(threadChannel);
		});
	}

	private static void updateMessagesInChannel(MessageChannel channel) {
		bufferedEmbeds.clear();
		channel.retrievePinnedMessages().queue(
				messages -> messages.stream()
						.filter(message -> message.getAuthor().equals(channel.getJDA().getSelfUser()))
						.forEach(message -> getUpdateBoardCommandStream()
								.forEach(updateBoardCommand -> updateBoardCommand.tryUpdateEmbeds(message)))
		);
	}

	private static Stream<UpdateBoardCommand> getUpdateBoardCommandStream() {
		return Main.COMMAND_REGISTRY.getCommandsOfType(UpdateBoardCommand.class);
	}

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		sendResponse(event, "Creating thread...", true);
		MessageChannel channel = getOrCreateThread(event.getChannel());
		createEmbedsForEvent(event)
				.filter(Objects::nonNull)
				.map(event.getMessageChannel()::sendMessageEmbeds)
				.forEach(GrimmSlashCommand::submitAndStore);
		if (channel instanceof ThreadChannel threadChannel)
			threadChannel.addThreadMember(event.getUser()).queue();
	}

	protected abstract Stream<MessageEmbed> createEmbedsForEvent(SlashCommandInteractionEvent event);

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

	protected abstract MessageEmbed createEmbedForTitle(String title);

	protected MessageEmbed lazyCreateEmbedForTitle(String title) {
		return bufferedEmbeds.computeIfAbsent(title, this::createEmbedForTitle);
	}

	protected MessageEmbed getOrCreateEmbedForTitle(String title, Supplier<MessageEmbed> fallback) {
		return bufferedEmbeds.computeIfAbsent(title, assignedTitle -> fallback.get());
	}

	protected <T> Function<T, MessageEmbed> getOrCreateEmbedForExtraInfo(Function<? super T, String> title, Function<? super T, MessageEmbed> fallback) {
		return t -> createEmbedForExtraInfo(t, title, fallback);
	}

	protected <T> MessageEmbed createEmbedForExtraInfo(T t, Function<? super T, String> title, Function<? super T, MessageEmbed> fallback) {
		return bufferedEmbeds.computeIfAbsent(title.apply(t), assignedTitle -> fallback.apply(t));
	}

	public void tryUpdateEmbeds(Message message) {
		List<MessageEmbed> newEmbeds = message.getEmbeds()
				.stream()
				.map(MessageEmbed::getTitle)
				.filter(Objects::nonNull)
				.filter(title -> title.matches(titleRegex))
				.map(this::lazyCreateEmbedForTitle)
				.filter(Objects::nonNull)
				.toList();
		if (!newEmbeds.isEmpty())
			message.editMessageEmbeds(newEmbeds).submit();
	}

	public String getThreadName() {
		return threadName;
	}
}
