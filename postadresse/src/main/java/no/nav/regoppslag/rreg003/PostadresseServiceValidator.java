package no.nav.regoppslag.rreg003;

import no.nav.regoppslag.exceptions.RegoppslagIllegalArgumentException;

import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.util.CollectionUtils.isEmpty;

public class PostadresseServiceValidator {

	private static final String FORTROLIG = "fortrolig";
	private static final String STRENGT_FORTROLIG = "strengt_fortrolig";
	private static final String STRENGT_FORTROLIG_UTLAND = "strengt_fortrolig_utland";
	public static final Set<String> ADRESSEBESKYTTELSE_TYPE = Set.of(FORTROLIG, STRENGT_FORTROLIG, STRENGT_FORTROLIG_UTLAND);
	public static final String UGYLDIG_INPUT = "Ugyldig input";
	public static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");
	public static final Pattern BEHANDLINGSNUMMER_PATTERN = Pattern.compile("^[A-Z]\\d{3}$");

	public static void validateInput(PostadresseRequest request, String behandlingsnummer) {

		if (request == null) {
			throw new RegoppslagIllegalArgumentException(UGYLDIG_INPUT + " Request body er tom.", BAD_REQUEST);
		}

		if (request.getIdent() == null) {
			throw new RegoppslagIllegalArgumentException(UGYLDIG_INPUT + " Ident kan ikke være null.", BAD_REQUEST);
		}

		if (!NUMBER_PATTERN.matcher(request.getIdent()).matches()) {
			throw new RegoppslagIllegalArgumentException(UGYLDIG_INPUT + " Ident kan kun bestå av tall.", BAD_REQUEST);
		}

		if (!asList(9, 11, 13).contains(request.getIdent().length())) {
			throw new RegoppslagIllegalArgumentException(UGYLDIG_INPUT + " Ident må ha lengde på 9, 11 eller 13 siffer.", BAD_REQUEST);
		}

		if (!isEmpty(behandlingsnummer) && !BEHANDLINGSNUMMER_PATTERN.matcher(behandlingsnummer).matches()) {
			throw new RegoppslagIllegalArgumentException(UGYLDIG_INPUT + " Behandlingsnummer må bestå av en stor bokstav og tre etterfølgende siffer. Eks B123 ", BAD_REQUEST);
		}

		if (!isEmpty(request.getFiltrerAdressebeskyttelse()) && validateAdressebeskyttelseInput(request)) {
			throw new RegoppslagIllegalArgumentException(getInvalidFilterAdressebeskyttelseInput(request), BAD_REQUEST);
		}
	}

	public static boolean validateFiltrerAdressebeskyttelse(PostadresseRequest postadresseRequest, PostadresseResponse response) {
		if (isEmpty(postadresseRequest.getFiltrerAdressebeskyttelse()) || isBlank(response.getAdressebeskyttelseType())) {
			return false;
		}
		Set<String> adressebeskyttelse = postadresseRequest.getFiltrerAdressebeskyttelse();
		return adressebeskyttelse.contains(response.getAdressebeskyttelseType());
	}

	private static boolean validateAdressebeskyttelseInput(PostadresseRequest request) {
		return request.getFiltrerAdressebeskyttelse().size() > 3 || !isValidAdressebeskyttelseGradering(request);
	}

	public static String getInvalidFilterAdressebeskyttelseInput(PostadresseRequest request) {
		String invalidInput = request.getFiltrerAdressebeskyttelse().stream()
				.filter(beskyttelse -> !ADRESSEBESKYTTELSE_TYPE.contains(beskyttelse))
				.collect(Collectors.joining(","));

		return format("filtrerAdressebeskyttelse må inneholde en eller flere av %s. Fikk ugyldig filtrerAdressebeskyttelse=[%s]", ADRESSEBESKYTTELSE_TYPE, invalidInput);
	}

	public static boolean isValidAdressebeskyttelseGradering(PostadresseRequest request) {
		return request.getFiltrerAdressebeskyttelse().stream()
				.allMatch(adressebeskyttelse -> ADRESSEBESKYTTELSE_TYPE.contains(adressebeskyttelse));
	}
}
