package no.nav.regoppslag.util;

import java.util.regex.Pattern;

public final class SafeLoggingUtil {
	private static final Pattern EVERYTHING_EXCEPT_SAFE_CHARS_REGEX = Pattern.compile("[^a-zA-Z0-9]");

	private SafeLoggingUtil() {
		// noop
	}

	public static String removeUnsafeChars(String input) {
		return EVERYTHING_EXCEPT_SAFE_CHARS_REGEX.matcher(input).replaceAll("_");
	}
}
