package mod.grimmauld.stonky.data;

import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

@SuppressWarnings("unused")
public class TradeElement {
	private int id;
	private String name;
	private String localizedName;
	private String availableName;
	@Nullable
	private String description;
	private int sellOffers;
	private float sellPrice;
	private int buyOrders;
	private float buyPrice;
	private int popularity;
	private int workbenchRarity;
	private float craftingSellSum;
	private float craftingBuySum;
	private int amount;
	private double demandSupplyRatio;
	private float margin;
	private double roi;
	private float craftingMargin;
	private String formatDemandSupplyRatio;
	private String formatMargin;
	private String formatRoi;
	private String formatCraftingMargin;
	private String craftVsBuy;
	private String rarityName;
	private String categoryName;
	private String typeName;
	private int rarityId;
	private String formatBuyPrice;
	private int categoryId;
	private String formatSellPrice;
	private int typeId;
	private int recipeId;
	private String formatCraftingSellSum;
	private Integer factionNumber;
	@Nullable
	private String faction;
	private String formatCraftingBuySum;
	private int craftingResultAmount;
	private String timestamp;
	private String lastUpdateTime;
	private String imagePath;
	private String image;
	@Nullable
	private String sortedStats;
	private int meta;
	private int removed;
	private int craftable;

	public static Predicate<TradeElement> matchesById(int id) {
		return tradeElement -> tradeElement.id == id;
	}

	public static Predicate<TradeElement> matchesByLocalizedName(String localizedName) {
		return tradeElement -> localizedName.equals(tradeElement.localizedName);
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getLocalizedName() {
		return localizedName;
	}

	public String getAvailableName() {
		return availableName;
	}

	public @Nullable String getDescription() {
		return description;
	}

	public int getSellOffers() {
		return sellOffers;
	}

	public float getSellPrice() {
		return sellPrice;
	}

	public int getBuyOrders() {
		return buyOrders;
	}

	public float getBuyPrice() {
		return buyPrice;
	}

	public int getPopularity() {
		return popularity;
	}

	public int getWorkbenchRarity() {
		return workbenchRarity;
	}

	public float getCraftingSellSum() {
		return craftingSellSum;
	}

	public float getCraftingBuySum() {
		return craftingBuySum;
	}

	public int getAmount() {
		return amount;
	}

	public double getDemandSupplyRatio() {
		return demandSupplyRatio;
	}

	public float getMargin() {
		return margin;
	}

	public double getRoi() {
		return roi;
	}

	public float getCraftingMargin() {
		return craftingMargin;
	}

	public String getFormatDemandSupplyRatio() {
		return formatDemandSupplyRatio;
	}

	public String getFormatMargin() {
		return formatMargin;
	}

	public String getFormatRoi() {
		return formatRoi;
	}

	public String getFormatCraftingMargin() {
		return formatCraftingMargin;
	}

	public String getCraftVsBuy() {
		return craftVsBuy;
	}

	public String getRarityName() {
		return rarityName;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public String getTypeName() {
		return typeName;
	}

	public int getRarityId() {
		return rarityId;
	}

	public String getFormatBuyPrice() {
		return formatBuyPrice;
	}

	public int getCategoryId() {
		return categoryId;
	}

	public String getFormatSellPrice() {
		return formatSellPrice;
	}

	public int getTypeId() {
		return typeId;
	}

	public int getRecipeId() {
		return recipeId;
	}

	public String getFormatCraftingSellSum() {
		return formatCraftingSellSum;
	}

	public int getFactionNumber() {
		return factionNumber == null ? -1 : factionNumber;
	}

	@Nullable
	public String getFaction() {
		return faction;
	}

	public String getFormatCraftingBuySum() {
		return formatCraftingBuySum;
	}

	public int getCraftingResultAmount() {
		return craftingResultAmount;
	}

	public @Nullable String getSortedStats() {
		return sortedStats;
	}

	public boolean isMeta() {
		return meta != 0;
	}

	public boolean isRemoved() {
		return removed != 0;
	}

	public boolean isCraftable() {
		return craftable != 0;
	}

	@Override
	public String toString() {
		return "TradeElement{" +
			"id=" + id +
			", name='" + name + '\'' +
			", faction='" + faction + '\'' +
			'}';
	}

	public String getStrippedName() {
		return getLocalizedName().replaceAll(" x[0-9]+", "");
	}

	public double getEffectiveCost() {
		return sellPrice / (amount * 100);
	}
}
