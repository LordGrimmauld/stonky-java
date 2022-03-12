package mod.grimmauld.stonky.discord.commands;

import mod.grimmauld.stonky.BuildConfig;
import mod.grimmauld.stonky.discord.GrimmSlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class GithubCommand extends GrimmSlashCommand {
	public GithubCommand(String name) {
		super(name, "View sourcecode of this bot");
	}

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		sendResponse(event, BuildConfig.GITHUB, true);
	}
}
