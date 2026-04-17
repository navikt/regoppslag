package no.nav.regoppslag.rreg003;

import no.nav.regoppslag.consumer.pdl.to.PdlMottakerInfo;
import no.nav.regoppslag.exceptions.RegoppslagIllegalArgumentException;

import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static no.nav.regoppslag.pdl.MapPDLResponse.FORTROLIG;
import static no.nav.regoppslag.pdl.MapPDLResponse.STRENGT_FORTROLIG;
import static no.nav.regoppslag.pdl.MapPDLResponse.STRENGT_FORTROLIG_UTLAND;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.util.CollectionUtils.isEmpty;

public class PostadresseServiceValidator {

	public static final Set<String> ADRESSEBESKYTTELSE_TYPE = Set.of(FORTROLIG, STRENGT_FORTROLIG, STRENGT_FORTROLIG_UTLAND);
	public static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");
	public static final Pattern BEHANDLINGSNUMMER_PATTERN = Pattern.compile("^[A-Z]\\d{3}$");

	public static final String UGYLDIG_INPUT = "Ugyldig input med feilmelding=%s";

	public static void validateBehandlingsnummer(String behandlingsnummer) {
		if (isEmpty(behandlingsnummer)) {
			return;
		}

		for (String entry : behandlingsnummer.split(",", -1)) {
			String trimmed = entry.trim();
			if (!BEHANDLINGSNUMMER_PATTERN.matcher(trimmed).matches()) {
				throw new RegoppslagIllegalArgumentException(UGYLDIG_INPUT.formatted("Hvert behandlingsnummer må bestå av én stor bokstav med tre etterfølgende siffer. F.eks. B123."), BAD_REQUEST);
			}
		}
	}

	public static void validateInput(PostadresseRequest request) {
		if (request == null) {
			throw new RegoppslagIllegalArgumentException(UGYLDIG_INPUT.formatted("Request body er tom."), BAD_REQUEST);
		}

		if (request.getIdent() == null) {
			throw new RegoppslagIllegalArgumentException(UGYLDIG_INPUT.formatted("Ident kan ikke være null."), BAD_REQUEST);
		}

		if (!NUMBER_PATTERN.matcher(request.getIdent()).matches()) {
			throw new RegoppslagIllegalArgumentException(UGYLDIG_INPUT.formatted("Ident kan kun bestå av tall."), BAD_REQUEST);
		}

		if (!asList(9, 11, 13).contains(request.getIdent().length())) {
			throw new RegoppslagIllegalArgumentException(UGYLDIG_INPUT.formatted("Ident må ha lengde på 9, 11 eller 13 siffer."), BAD_REQUEST);
		}

		if (!isEmpty(request.getFiltrerAdressebeskyttelse()) && validateAdressebeskyttelseInput(request)) {
			throw new RegoppslagIllegalArgumentException(UGYLDIG_INPUT.formatted(getInvalidFilterAdressebeskyttelseInput(request)), BAD_REQUEST);
		}
	}

	private static boolean validateAdressebeskyttelseInput(PostadresseRequest request) {
		return request.getFiltrerAdressebeskyttelse().size() > 3 || !isValidAdressebeskyttelseGradering(request);
	}

	private static String getInvalidFilterAdressebeskyttelseInput(PostadresseRequest request) {
		String invalidInput = request.getFiltrerAdressebeskyttelse().stream()
				.filter(beskyttelse -> !ADRESSEBESKYTTELSE_TYPE.contains(beskyttelse))
				.collect(Collectors.joining(","));

		return format("filtrerAdressebeskyttelse må inneholde en eller flere av %s. Fikk ugyldig filtrerAdressebeskyttelse=[%s]", ADRESSEBESKYTTELSE_TYPE, invalidInput);
	}

	private static boolean isValidAdressebeskyttelseGradering(PostadresseRequest request) {
		return ADRESSEBESKYTTELSE_TYPE.containsAll(request.getFiltrerAdressebeskyttelse());
	}

	public static boolean validateFiltrerAdressebeskyttelse(PostadresseRequest postadresseRequest, PdlMottakerInfo pdlMottakerInfo) {
		if (isEmpty(postadresseRequest.getFiltrerAdressebeskyttelse()) || isEmpty(pdlMottakerInfo.getAdressebeskyttelseType())) {
			return false;
		}

		Set<String> adressebeskyttelse = postadresseRequest.getFiltrerAdressebeskyttelse();
		return pdlMottakerInfo.getAdressebeskyttelseType().stream()
				.anyMatch(adressebeskyttelse::contains);
	}

}