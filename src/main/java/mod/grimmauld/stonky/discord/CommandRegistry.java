package mod.grimmauld.stonky.discord;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class CommandRegistry {
	private final Set<GrimmSlashCommand> commands = new HashSet<>();

	public CommandRegistry register(GrimmSlashCommand command) {
		commands.add(command);
		return this;
	}

	public Stream<GrimmSlashCommand> stream() {
		return commands.stream();
	}

	public <T extends GrimmSlashCommand> Stream<T> getCommandsOfType(Class<T> tClass) {
		return stream()
			.filter(tClass::isInstance)
			.map(tClass::cast);
	}
}
