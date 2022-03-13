package mod.grimmauld.stonky.discord.commands.guild_commands;

import mod.grimmauld.stonky.Main;
import mod.grimmauld.stonky.data.guild.GuildManager;
import mod.grimmauld.stonky.discord.GrimmSlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.apache.commons.lang3.text.StrBuilder;

import java.time.Instant;
import java.util.Set;

public class TrackedCommand extends GrimmSlashCommand {
	public TrackedCommand(String name) {
		super(name, "Track progress of a player based on their CO_Driver profile");
	}

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		if (event.getGuild() == null) {
			sendResponse(event, "This command is only available in a guild", true);
			return;
		}

		// event.deferReply().setEphemeral(true).submit();
		// TODO: add player names

		Set<Long> trackedProfiles = Main.GUILD_MANAGER.getGuildFor(event.getGuild()).getTrackedProfiles();

		StrBuilder builder = new StrBuilder();
		trackedProfiles.forEach(trackedProfile -> builder.appendln("Player " + "*NAME*" + ": https://beta.crossoutdb.com/profile/" + trackedProfile));
		String players = builder.isEmpty() ? "No players are being tracked" : builder.toString();
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTimestamp(Instant.now());
		eb.addField("Tracked players", players, true);
		sendEmbedResponse(event, eb, true);
	}

	@Override
	public boolean canRegister() {
		return super.canRegister() && GuildManager.ENABLE_GUILD_CONTENT;
	}
}
