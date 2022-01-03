package mod.grimmauld.stonky;

import mod.grimmauld.stonky.data.DataManager;
import mod.grimmauld.stonky.discord.CommandRegistry;
import mod.grimmauld.stonky.discord.DiscordBot;
import mod.grimmauld.stonky.discord.commands.CraftCommand;
import mod.grimmauld.stonky.discord.commands.GithubCommand;
import mod.grimmauld.stonky.discord.commands.InviteCommand;
import mod.grimmauld.stonky.discord.commands.VersionCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class Main {
	public static final Logger LOGGER = LoggerFactory.getLogger(BuildConfig.APPID);
	public static final DataManager DATA_MANAGER = new DataManager();
	public static final CommandRegistry COMMAND_REGISTRY = new CommandRegistry()
		.register(new GithubCommand("github"))
		.register(new VersionCommand("version"))
		.register(new InviteCommand("invite"))
		.register(new CraftCommand("craft", DATA_MANAGER));
	public static final DiscordBot DISCORD_BOT = new DiscordBot(COMMAND_REGISTRY);
	private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();

	public static void main(String[] args) {
		// DATA_MANAGER.registerRefreshCallback(dataManager -> dataManager.getTradeElements().stream().map(TradeElement::toString).forEach(LOGGER::info));
		SCHEDULER.scheduleAtFixedRate(DATA_MANAGER::refreshCache, 0, 5, TimeUnit.MINUTES);
	}
}
