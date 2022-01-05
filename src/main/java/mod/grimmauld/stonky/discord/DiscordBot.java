package mod.grimmauld.stonky.discord;

import mod.grimmauld.stonky.BuildConfig;
import mod.grimmauld.stonky.Main;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.security.auth.login.LoginException;

@ParametersAreNonnullByDefault
public class DiscordBot extends ListenerAdapter {
	private final CommandRegistry commandRegistry;

	public DiscordBot(CommandRegistry commandRegistry) {
		this.commandRegistry = commandRegistry;
		try {
			JDABuilder.createDefault(BuildConfig.DISCORD_TOKEN).addEventListeners(this).build();
		} catch (LoginException e) {
			Main.LOGGER.error("Can't start discord bot", e);
			e.printStackTrace();
			System.exit(0);
		}
	}

	@Override
	public void onReady(@NotNull ReadyEvent event) {
		super.onReady(event);
		commandRegistry.stream()
			.forEach(grimmSlashCommand -> event.getJDA().upsertCommand(grimmSlashCommand.getCommandData()).submit());
	}

	@Override
	public void onSlashCommand(SlashCommandEvent event) {
		commandRegistry
			.stream()
			.filter(grimmSlashCommand -> grimmSlashCommand.getName().equals(event.getName()))
			.forEach(grimmSlashCommand -> grimmSlashCommand.execute(event));
	}
}
