package mod.grimmauld.stonky.discord.commands.guild_commands;

import mod.grimmauld.stonky.Main;
import mod.grimmauld.stonky.data.guild.Guild;
import mod.grimmauld.stonky.data.guild.GuildManager;
import mod.grimmauld.stonky.discord.GrimmSlashCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class UntrackCommand extends GrimmSlashCommand {
	private static final String PLAYER_PROFILE_KEY = "profile";

	public UntrackCommand(String name) {
		super(name, "Track progress of a player based on their CO_Driver profile");
	}

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		if (event.getGuild() == null || event.getMember() == null) {
			sendResponse(event, "This command is only available in a guild", true);
			return;
		}

		if (!event.getMember().hasPermission(event.getGuildChannel(), Permission.MANAGE_ROLES)) {
			sendResponse(event, "This command requires the manage roles permission", true);
			return;
		}

		String profile = event.getOptions()
				.stream()
				.filter(optionMapping -> optionMapping.getName().equals(PLAYER_PROFILE_KEY) && optionMapping.getType() == OptionType.STRING)
				.map(OptionMapping::getAsString)
				.findFirst().orElse("_")
				.replaceAll("((https?://)?(www.)?(beta.)?crossoutdb.com/profile/)?([0-9]+)", "$5")
				.strip();
		if (!profile.matches("[0-9]+")) {
			sendResponse(event, "Can not read symbol: " + profile, true);
			return;
		}
		event.deferReply();
		// TODO: add player name
		Guild guild = Main.GUILD_MANAGER.getGuildFor(event.getGuild());
		if (guild.getTrackedProfiles().remove(Long.decode(profile))) {
			guild.updateManager();
			sendResponse(event, "stopped tracking player " + profile, true);
		} else {
			sendResponse(event, "can not stop tracking player " + profile + " as that player was not being tracked in the first place", true);
		}
	}

	@Override
	protected SlashCommandData attachExtraData(SlashCommandData data) {
		OptionData optionData = new OptionData(OptionType.STRING, PLAYER_PROFILE_KEY, "Specify a profile to be tracked", true);
		return super.attachExtraData(data).addOptions(optionData);
	}

	@Override
	public boolean canRegister() {
		return super.canRegister() && GuildManager.ENABLE_GUILD_CONTENT;
	}
}
