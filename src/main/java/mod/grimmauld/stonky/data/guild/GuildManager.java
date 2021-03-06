package mod.grimmauld.stonky.data.guild;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mod.grimmauld.stonky.Main;
import net.dv8tion.jda.api.JDA;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class GuildManager {
	public static final Gson GSON = new GsonBuilder()
			.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
			.setPrettyPrinting()
			.create();
	public static final File GUILD_FILE_PATH = new File("run/guilds.json");
	private static final File ROOT = new File("./");
	private static final File RUN = new File("run");
	public static final boolean ENABLE_GUILD_CONTENT = checkFileStructure();

	public Map<Long, Guild> guilds = new HashMap<>();

	public static GuildManager readFromDisk() {
		GuildManager manager = null;
		try (Reader reader = Files.newBufferedReader(GUILD_FILE_PATH.toPath())) {
			manager = GSON.fromJson(reader, GuildManager.class);
		} catch (IOException e) {
			Main.LOGGER.error("Error loading guild data: {}", e.getMessage());
		}
		return manager != null ? manager : new GuildManager();
	}

	private static boolean checkFileStructure() {
		boolean successful = true;
		if (ROOT.canWrite()) {
			if (!RUN.exists())
				successful = RUN.mkdir();
			if (!GUILD_FILE_PATH.exists()) {
				try (BufferedWriter writer = new BufferedWriter(new FileWriter(GUILD_FILE_PATH))) {
					writer.write("");
				} catch (IOException e) {
					Main.LOGGER.warn("Could not validate or create file structure: {}", e.getMessage());
					successful = false;
				}
			}
		} else {
			successful = false;
			Main.LOGGER.warn("Could not validate or create file structure: root is not writeable");
		}
		return successful;
	}

	public void onUpdateGuilds() {
		if (!ENABLE_GUILD_CONTENT)
			return;
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(GUILD_FILE_PATH))) {
			writer.write(GSON.toJson(this));
		} catch (IOException e) {
			Main.LOGGER.error("Error saving guild data: {}", e.getMessage());
		}
		guilds.values().forEach(guild -> guild.setGuildManager(this));
	}

	public void populateFromJDA(JDA jda) {
		for (var guild : jda.getGuilds()) {
			if (!this.guilds.containsKey(guild.getIdLong())) {
				this.guilds.put(guild.getIdLong(), new Guild(guild));
			}
		}
		onUpdateGuilds();
	}

	public Guild getGuildFor(net.dv8tion.jda.api.entities.Guild guild) {
		if (!this.guilds.containsKey(guild.getIdLong())) {
			this.guilds.put(guild.getIdLong(), new Guild(guild));
			onUpdateGuilds();
		}
		return guilds.get(guild.getIdLong());
	}
}
