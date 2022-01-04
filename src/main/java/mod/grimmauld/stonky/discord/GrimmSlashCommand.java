package mod.grimmauld.stonky.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

import static mod.grimmauld.stonky.util.FunctionUtils.asPredicate;
import static mod.grimmauld.stonky.util.FunctionUtils.bundle;

public abstract class GrimmSlashCommand {
	@Nullable
	protected final String help;
	protected final String name;

	protected GrimmSlashCommand(String name, @Nullable String help) {
		this.help = help;
		this.name = name;
	}

	protected void sendResponse(SlashCommandEvent event, String msg, boolean ephemeral) {
		if (msg.isEmpty())
			return;

		while (msg.length() > 1990) {
			event.reply(msg.substring(0, 1990)).setEphemeral(ephemeral).submit();
			msg = msg.substring(1990);
		}
		event.reply(msg).setEphemeral(ephemeral).submit();
	}

	protected void sendEmbedResponse(SlashCommandEvent event, EmbedBuilder eb, boolean ephermal) {
		MessageEmbed msg = eb.build();
		if (msg.isEmpty())
			return;

		event.replyEmbeds(msg).setEphemeral(ephermal).submit();
	}

	@Nonnull
	public String getHelp() {
		return help != null ? help : "no help message provided";
	}

	public CommandData getCommandData() {
		return attachExtraData(new CommandData(getName(), getHelp()));
	}

	public String getName() {
		return name;
	}

	protected CommandData attachExtraData(CommandData data) {
		return data;
	}

	public abstract void execute(SlashCommandEvent event);

	protected void trySendInThread(String threadName, MessageChannel original, Member member, Consumer<? super MessageChannel> forChannel) {
		if (original instanceof IThreadContainer guildMessageChannel) {
			// Try to find an appropriate thread
			if (guildMessageChannel.getThreadChannels()
				.stream()
				.filter(threadChannel -> threadChannel.getName().equals(threadName))
				.limit(1)
				.noneMatch(asPredicate(bundle(forChannel, threadChannel -> threadChannel.addThreadMember(member).queue()))))
				// If no thread found, make a new one
				guildMessageChannel.createThreadChannel(threadName)
					.queue(bundle(forChannel, threadChannel -> threadChannel.addThreadMember(member).queue()));
		} else {
			forChannel.accept(original);
		}
	}

	public void storeForUpdates(Message message) {
		// message.pin().queue();
	}
}
