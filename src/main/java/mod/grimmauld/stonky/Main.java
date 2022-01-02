package mod.grimmauld.stonky;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
	public static final Logger LOGGER = Logger.getLogger(BuildConfig.APPID);

	public static void main(String[] args) {
		LOGGER.log(Level.INFO, "test");
	}
}
