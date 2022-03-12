package mod.grimmauld.stonky.discord;

import mod.grimmauld.stonky.BuildConfig;
import mod.grimmauld.stonky.util.StreamUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class GrimmSlashCommand {
	@Nullable
	protected final String help;
	protected final String name;

	protected GrimmSlashCommand(String name, @Nullable String help) {
		this.help = help;
		this.name = name;
	}

	protected static void executeInAllThreads(JDA jda, Consumer<? super ThreadChannel> consumer) {
		jda.getThreadChannelCache().stream().filter(ThreadChannel::isOwner).forEach(consumer);
		jda.getTextChannelCache().stream().filter(hasPermissionIn(Permission.MESSAGE_HISTORY))
				.map(IThreadContainer::retrieveArchivedPublicThreadChannels).forEach(c -> c.queue(l -> l.stream().filter(ThreadChannel::isOwner).filter(StreamUtils.distinctByKey(ThreadChannel::getName)).forEach(consumer)));
	}

	protected static Predicate<GuildChannel> hasPermissionIn(Permission permission) {
		return channel -> channel.getGuild().getSelfMember().hasPermission(channel.getPermissionContainer(), permission);
	}

	public static void storeForUpdates(Message message) {
		message.pin().queue();
	}

	public static void submitAndStore(MessageAction messageAction) {
		messageAction.queue(GrimmSlashCommand::storeForUpdates);
	}

	protected void sendResponse(SlashCommandInteractionEvent event, String msg, boolean ephemeral) {
		if (msg.isEmpty())
			return;

		while (msg.length() > 1990) {
			event.reply(msg.substring(0, 1990)).setEphemeral(ephemeral).submit();
			msg = msg.substring(1990);
		}
		event.reply(msg).setEphemeral(ephemeral).submit();
	}

	protected void sendEmbedResponse(SlashCommandInteractionEvent event, EmbedBuilder eb, boolean ephermal) {
		MessageEmbed msg = eb.build();
		if (msg.isEmpty())
			return;

		event.replyEmbeds(msg).setEphemeral(ephermal).submit();
	}

	@Nonnull
	public String getHelp() {
		return help != null ? help : "no help message provided";
	}

	public SlashCommandData getCommandData() {
		return attachExtraData(new CommandDataImpl(getName(), getHelp()));
	}

	public String getName() {
		return name;
	}

	protected SlashCommandData attachExtraData(SlashCommandData data) {
		return data;
	}

	public abstract void execute(SlashCommandInteractionEvent event);

	public boolean isPublic() {
		return true;
	}

	public boolean canExecuteFor(User user) {
		return isPublic() || user.getId().equals(BuildConfig.OWNER);
	}
}
