package mod.grimmauld.stonky.discord.commands;

import mod.grimmauld.stonky.BuildConfig;
import mod.grimmauld.stonky.discord.GrimmSlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class VersionCommand extends GrimmSlashCommand {
	public VersionCommand(String name) {
		super(name, "Get version info");
	}

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		sendResponse(event, "Running version " + BuildConfig.VERSION + " on commit " + BuildConfig.GITHASH, true);
	}
}
