package mod.grimmauld.stonky.discord.commands;

import mod.grimmauld.stonky.BuildConfig;
import mod.grimmauld.stonky.discord.GrimmSlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class InviteCommand extends GrimmSlashCommand {
	public InviteCommand(String name) {
		super(name, "Get an invite link for this bot");
	}

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		sendResponse(event, BuildConfig.INVITE, true);
	}
}
