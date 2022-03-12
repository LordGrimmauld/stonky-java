package mod.grimmauld.stonky.data.guild;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
public class Guild implements Serializable {
	@Nullable
	private transient GuildManager guildManager = null;
	private final Set<Long> trackedProfiles = new HashSet<>();

	public Guild(net.dv8tion.jda.api.entities.Guild guild) {

	}

	public Guild() {
		// empty because gson requires this
	}


	public void setGuildManager(@Nullable GuildManager guildManager) {
		this.guildManager = guildManager;
	}

	public Set<Long> getTrackedProfiles() {
		return trackedProfiles;
	}

	public void updateManager() {
		if (guildManager != null)
			guildManager.onUpdateGuilds();
	}
}
