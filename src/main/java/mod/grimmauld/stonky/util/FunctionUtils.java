package mod.grimmauld.stonky.util;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class FunctionUtils {
	private FunctionUtils() {
		// no instances
	}

	public static <T> Predicate<T> asPredicate(Consumer<T> consumer) {
		return t -> {
			consumer.accept(t);
			return true;
		};
	}

	@SafeVarargs
	public static <T> Consumer<T> bundle(Consumer<? super T>... consumers) {
		return t -> Arrays.stream(consumers).forEach(tConsumer -> tConsumer.accept(t));
	}
}
