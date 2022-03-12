package mod.grimmauld.stonky.discord.commands;

import mod.grimmauld.stonky.Main;
import mod.grimmauld.stonky.discord.CommandRegistry;
import mod.grimmauld.stonky.discord.GrimmSlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.apache.commons.lang3.text.StrBuilder;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.stream.Collectors;

public class StatusCommand extends GrimmSlashCommand {
	private static final RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();
	public StatusCommand(String name) {
		super(name, "get status and debug information");
	}

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		StrBuilder msg = new StrBuilder();
		msg.appendln("**Debug information about the Bot**");
		msg.appendln("Member in " + event.getJDA().getGuilds().size() + " servers");
		msg.appendln("Uptime: " + DurationFormatUtils.formatDuration(rb.getUptime(), "HH:mm:ss,SSS"));
		msg.appendln("Registered commands: " + Main.COMMAND_REGISTRY.stream().map(GrimmSlashCommand::getName).collect(Collectors.joining(", ")));
		sendResponse(event, msg.toString(), true);
	}

	@Override
	public boolean isPublic() {
		return false;
	}
}
