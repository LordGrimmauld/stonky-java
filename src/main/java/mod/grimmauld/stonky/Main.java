package mod.grimmauld.stonky;

import mod.grimmauld.stonky.data.DataManager;
import mod.grimmauld.stonky.data.TradeElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {
	public static final Logger LOGGER = LoggerFactory.getLogger(BuildConfig.APPID);
	public static final DataManager DATA_MANAGER = new DataManager();

	public static void main(String[] args) {
		DATA_MANAGER.refreshCache();
		DATA_MANAGER.getTradeElements()
			.stream()
			.map(TradeElement::toString)
			.forEach(LOGGER::info);
	}
}
