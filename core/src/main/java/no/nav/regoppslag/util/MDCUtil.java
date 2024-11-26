package no.nav.regoppslag.util;

import org.slf4j.MDC;

import java.util.UUID;

import static no.nav.regoppslag.util.MDCConstants.CALL_ID;
import static org.apache.commons.lang3.StringUtils.isBlank;

public final class MDCUtil {

	private MDCUtil() {
	}

	public static String getCallId() {
		return isBlank(MDC.get(CALL_ID)) ? UUID.randomUUID().toString() : MDC.get(CALL_ID);
	}

}
