package mod.grimmauld.stonky.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.IThreadContainer;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

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

	protected void trySendInThread(MessageChannel original, Consumer<MessageChannel> forChannel) {
		if (original instanceof IThreadContainer guildMessageChannel) {
			guildMessageChannel.createThreadChannel("Crafting").queue(forChannel);
		} else {
			forChannel.accept(original);
		}
	}
}
