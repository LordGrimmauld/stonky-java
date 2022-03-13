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

	public int calculateScore() {
		if (!"8v8".equals(matchType) || !"PvP".equals(matchClassification))
			return 0;
		return Math.max(0, kills - (medalList != null && medalList.contains("PvpMvpWin") ? 1 : 2));
	}
}
