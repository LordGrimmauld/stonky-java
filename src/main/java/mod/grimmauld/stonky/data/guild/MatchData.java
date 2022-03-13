package mod.grimmauld.stonky.data.guild;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MatchData {
	public static final Gson GSON = new GsonBuilder()
			.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
			.setPrettyPrinting()
			.create();
	private long matchId;
	private String matchClassification;
	private String matchType;
	private String matchStart;
	private int kills;
	private String medalList;

	public long getMatchId() {
		return matchId;
	}

	public String getMatchClassification() {
		return matchClassification;
	}

	public String getMatchType() {
		return matchType;
	}

	public String getMatchStart() {
		return matchStart;
	}

	public int getKills() {
		return kills;
	}

	public int calculateScore() {
		if (!matchType.equals("8v8") || !matchClassification.equals("PvP"))
			return 0;
		return Math.max(0, kills - (medalList.contains("PvpMvpWin") ? 1 : 2));
	}
}
