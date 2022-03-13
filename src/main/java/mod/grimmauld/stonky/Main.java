package mod.grimmauld.stonky;

import mod.grimmauld.stonky.data.DataManager;
import mod.grimmauld.stonky.data.guild.GuildManager;
import mod.grimmauld.stonky.discord.CommandRegistry;
import mod.grimmauld.stonky.discord.DiscordBot;
import mod.grimmauld.stonky.discord.commands.*;
import mod.grimmauld.stonky.discord.commands.guild_commands.TestDevStuffCommand;
import mod.grimmauld.stonky.discord.commands.guild_commands.TrackCommand;
import mod.grimmauld.stonky.discord.commands.guild_commands.TrackedCommand;
import mod.grimmauld.stonky.discord.commands.guild_commands.UntrackCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class Main {
	public static final Logger LOGGER = LoggerFactory.getLogger(BuildConfig.APPID);
	public static final GuildManager GUILD_MANAGER = GuildManager.readFromDisk();
	public static final DataManager DATA_MANAGER = new DataManager();
	public static final CommandRegistry COMMAND_REGISTRY = new CommandRegistry()
			.register(new GithubCommand("github"))
			.register(new VersionCommand("version"))
			.register(new InviteCommand("invite"))
			.register(new CraftCommand("craft", DATA_MANAGER))
			.register(new SalvageCommand("salvage", DATA_MANAGER))
			.register(new CrateCommand("crate", DATA_MANAGER))
			.register(new TrackCommand("track"))
			.register(new TrackedCommand("tracked"))
			.register(new UntrackCommand("untrack"))
			.register(new TestDevStuffCommand("testdev"))
			.register(new StatusCommand("status"));
	public static final DiscordBot DISCORD_BOT = new DiscordBot(COMMAND_REGISTRY);
	private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();

	public static void main(String[] args) {
		// DATA_MANAGER.registerRefreshCallback(dataManager -> dataManager.getTradeElements().stream().map(TradeElement::toString).forEach(LOGGER::info));
		DISCORD_BOT.getJDAOptional().ifPresent(GUILD_MANAGER::populateFromJDA);
		DATA_MANAGER.registerRefreshCallback(dataManager -> UpdateBoardCommand.updateBoards());
		SCHEDULER.scheduleAtFixedRate(DATA_MANAGER::refreshCache, 0, 5, TimeUnit.MINUTES);
	}
}
