package mod.grimmauld.stonky.data;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class Crate {
	private int id;
	private int reroll;
	private Set<Integer> loot;

	@Nullable
	public TradeElement getAsTradeElement(DataManager dataManager) {
		return dataManager.getById(id);
	}

	public List<TradeElement> getContents(DataManager dataManager) {
		return dataManager.getTradeElements()
				.stream()
				.filter(tradeElement -> loot.contains(tradeElement.getId()))
				.toList();
	}

	public Integer getId() {
		return id;
	}

	public double getRerollCost(DataManager dataManager) {
		return dataManager.getTradeElements()
				.stream()
				.filter(TradeElement.matchesById(57))
				.findFirst()
				.map(TradeElement::getEffectiveCost)
				.orElse(0D) * reroll;
	}

	public int getRerollAmount() {
		return reroll;
	}

	public int lootSize() {
		return loot.size();
	}
}
