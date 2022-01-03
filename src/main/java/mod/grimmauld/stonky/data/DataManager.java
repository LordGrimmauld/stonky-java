package mod.grimmauld.stonky.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import mod.grimmauld.stonky.Main;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class DataManager {
	private static final Gson gson = new Gson();
	public final FactionManager factionManager;
	private final Set<Consumer<DataManager>> refreshCallbacks = new HashSet<>();
	private Set<TradeElement> tradeElements = new HashSet<>();

	public DataManager() {
		factionManager = new FactionManager(this);
	}

	public void refreshCache() {
		String data = "{'data':[]}";
		try {
			data = IOUtils.toString(new URL("https://crossoutdb.com/data/search"), StandardCharsets.UTF_8);
		} catch (IOException e) {
			Main.LOGGER.error("Error reading data from crossoutdb: ", e);
		}
		JsonElement json = JsonParser.parseString(data);
		tradeElements = StreamSupport.stream(json.getAsJsonObject()
				.get("data")
				.getAsJsonArray()
				.spliterator(), false)
			.map(je -> gson.fromJson(je, TradeElement.class))
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
		refreshCallbacks.forEach(setConsumer -> setConsumer.accept(this));
	}

	public Set<TradeElement> getTradeElements() {
		return tradeElements;
	}

	public void registerRefreshCallback(Consumer<DataManager> callback) {
		refreshCallbacks.add(callback);
	}
}
