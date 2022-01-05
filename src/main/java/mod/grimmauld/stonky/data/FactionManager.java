package mod.grimmauld.stonky.data;


import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class FactionManager {
	private Map<Integer, String> cachedFactions = new HashMap<>();

	public FactionManager(DataManager dataManager) {
		dataManager.registerRefreshCallback(this::refreshAvailableFactions);
	}

	private void refreshAvailableFactions(DataManager dataManager) {
		cachedFactions = dataManager.getTradeElements().stream().filter(tradeElement -> tradeElement.getFaction() != null)
			.map(te -> ImmutablePair.of(te.getFactionNumber(), te.getFaction()))
			.distinct()
			.collect(Collectors.toMap(ImmutablePair::getLeft, ImmutablePair::getRight));
	}

	public Map<Integer, String> getFactions() {
		return this.cachedFactions;
	}
}
