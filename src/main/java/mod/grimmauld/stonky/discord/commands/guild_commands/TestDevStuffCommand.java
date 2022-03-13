package mod.grimmauld.stonky.discord.commands.guild_commands;

import com.google.gson.JsonParser;
import mod.grimmauld.stonky.Main;
import mod.grimmauld.stonky.data.guild.Guild;
import mod.grimmauld.stonky.data.guild.GuildManager;
import mod.grimmauld.stonky.data.guild.MatchData;
import mod.grimmauld.stonky.discord.GrimmSlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.text.StrBuilder;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class TestDevStuffCommand extends GrimmSlashCommand {
	public TestDevStuffCommand(String name) {
		super(name, "Test the in-dev stuff");
	}

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		if (event.getGuild() == null) {
			sendResponse(event, "This command is only available in a guild", true);
			return;
		}

		CompletableFuture<InteractionHook> response = event.deferReply().setEphemeral(true).submit();
		// TODO: add player name

		Guild guild = Main.GUILD_MANAGER.getGuildFor(event.getGuild());
		Map<Long, Integer> scores = calculateTournamentScores(guild);

		StrBuilder scoreTable = new StrBuilder();
		scores.forEach((l, s) -> scoreTable.appendln(l + ": " + s));

		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Clan tournament scores for " + event.getGuild().getName());
		eb.setTimestamp(Instant.now());
		eb.addField("Scores", scoreTable.isEmpty() ? "No players being tracked, can't display a score" : scoreTable.toString(), true);
		sendEmbedResponse(event, eb, true);
		try {
			response.get().sendMessageEmbeds(eb.build()).queue();
		} catch (InterruptedException | ExecutionException e) {
			Main.LOGGER.error("Error occurred while fetching deferred reply hook: {}", e.getMessage());
		}
	}

	private Set<MatchData> downloadMatchHistory(long id) {
		String data;
		try {
			data = IOUtils.toString(new URL("https://beta.crossoutdb.com/data/profile/match_history/" + id), StandardCharsets.UTF_8);
		} catch (IOException e) {
			Main.LOGGER.error("Error reading data from crossoutdb: ", e);
			return new HashSet<>();
		}
		return StreamSupport.stream(JsonParser.parseString(data)
						.getAsJsonObject()
						.get("match_history")
						.getAsJsonArray()
						.spliterator(), false)
				.map(je -> MatchData.GSON.fromJson(je, MatchData.class))
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
	}

	private Map<Long, Integer> calculateTournamentScores(Guild guild) {
		Main.LOGGER.info("Started match history download");
		Map<Long, Set<MatchData>> playedMatches = guild.getTrackedProfiles().stream().collect(Collectors.toMap(t -> t, this::downloadMatchHistory));
		Main.LOGGER.info("Finished match history download");
		Set<Long> distinctGames = new HashSet<>();
		Set<Long> teamGames = playedMatches.values()
				.stream()
				.flatMap(Set::stream)
				.map(MatchData::getMatchId)
				.filter(n -> !distinctGames.add(n))
				.collect(Collectors.toSet());
		return guild.getTrackedProfiles().stream().collect(Collectors.toMap(l -> l,
				l -> playedMatches.get(l).stream()
						.filter(matchData -> teamGames.contains(matchData.getMatchId()))
						.map(MatchData::calculateScore)
						.mapToInt(Integer::intValue)
						.sum()));
	}

	@Override
	public boolean canRegister() {
		return super.canRegister() && GuildManager.ENABLE_GUILD_CONTENT;
	}
}
