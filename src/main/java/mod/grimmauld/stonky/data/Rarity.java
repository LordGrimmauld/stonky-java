package mod.grimmauld.stonky.data;

import java.awt.*;
import java.util.Arrays;
import java.util.Optional;

public enum Rarity {
	RARE("Rare", 0, 102, 204),
	SPECIAL("Special", 51, 202, 204),
	EPIC("Epic", 185, 0, 255),
	LEGENDARY("Legendary", 255, 158, 64),
	RELIC("Relic", 255, 102, 0),
	COMMON("Common", 255, 255, 255);

	public final String rarityName;
	public final Color color;

	Rarity(String name, int r, int g, int b) {
		this.rarityName = name;
		color = new Color(r, g, b);
	}

	public static Rarity byName(String name) {
		return byNameOptional(name).orElse(COMMON);
	}

	public static Optional<Rarity> byNameOptional(String name) {
		for (Rarity rarity : Rarity.values()) {
			if (rarity.rarityName.equals(name))
				return Optional.of(rarity);
		}
		return Optional.empty();
	}

	public static Rarity fromTradeElement(TradeElement tradeElement) {
		return Arrays.stream(values()).filter(rarity -> rarity.contains(tradeElement)).findFirst().orElse(COMMON);
	}

	public boolean contains(TradeElement tradeElement) {
		return rarityName.equals(tradeElement.getRarityName());
	}
}
