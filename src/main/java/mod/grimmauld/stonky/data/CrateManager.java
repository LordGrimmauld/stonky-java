package mod.grimmauld.stonky.data;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.Nullable;

import java.io.InputStreamReader;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CrateManager {
	private static final Gson gson = new Gson();
	public final Set<Crate> crates;

	public CrateManager() {
		crates = FileManager.getResourceListing("data/crates")
			.map(FileManager::getFileFromResourceAsStream)
			.map(InputStreamReader::new)
			.map(JsonParser::parseReader)
			.map(je -> gson.fromJson(je, Crate.class))
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
	}

	public static Predicate<Crate> matchesById(int id) {
		return crate -> crate.getId() == id;
	}

	@Nullable
	public Crate getCrateByName(String name, DataManager dataManager) {
		TradeElement tradeElement = dataManager.getByName(name);
		if (tradeElement == null)
			return null;
		return crates.stream()
			.filter(matchesById(tradeElement.getId()))
			.findFirst()
			.orElse(null);
	}
}
