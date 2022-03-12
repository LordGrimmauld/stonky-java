package mod.grimmauld.stonky.discord;

import mod.grimmauld.stonky.BuildConfig;
import mod.grimmauld.stonky.Main;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.security.auth.login.LoginException;
import java.util.Optional;
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
public class DiscordBot extends ListenerAdapter {
	private final CommandRegistry commandRegistry;
	private JDA jda;

	public DiscordBot(CommandRegistry commandRegistry) {
		this.commandRegistry = commandRegistry;
		try {
			jda = JDABuilder.createDefault(BuildConfig.DISCORD_TOKEN).addEventListeners(this).build();
			jda.awaitReady();
		} catch (LoginException | InterruptedException e) {
			Main.LOGGER.error("Can't start discord bot", e);
			e.printStackTrace();
			System.exit(0);
		}
	}

	@Nullable
	public JDA getJda() {
		return jda;
	}

	public Optional<JDA> getJDAOptional() {
		return Optional.ofNullable(jda);
	}

	@Override
	public void onReady(@NotNull ReadyEvent event) {
		super.onReady(event);
		commandRegistry.stream()
				.filter(GrimmSlashCommand::isPublic)
				.filter(GrimmSlashCommand::canRegister)
				.forEach(grimmSlashCommand -> event.getJDA().upsertCommand(grimmSlashCommand.getCommandData()).submit());
		Optional.ofNullable(event.getJDA().getGuildById(BuildConfig.DEBUG_GUILD))
				.ifPresent(debugGuild -> commandRegistry.stream().filter(((Predicate<GrimmSlashCommand>) GrimmSlashCommand::isPublic).negate())
						.forEach(grimmSlashCommand -> debugGuild.upsertCommand(grimmSlashCommand.getCommandData()).submit()));
	}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		commandRegistry
				.stream()
				.filter(grimmSlashCommand -> grimmSlashCommand.getName().equals(event.getName()))
				.filter(grimmSlashCommand -> grimmSlashCommand.canExecuteFor(event.getUser()))
				.forEach(grimmSlashCommand -> grimmSlashCommand.execute(event));
	}
}
