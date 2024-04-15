package no.nav.regoppslag.rreg003;

import no.nav.regoppslag.consumer.pdl.to.Gradering;
import no.nav.regoppslag.exceptions.RegOppslagIkkeFunnetException;
import no.nav.regoppslag.exceptions.RegoppslagIllegalArgumentException;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;
import static no.nav.regoppslag.consumer.pdl.to.Gradering.values;
import static org.apache.commons.lang3.EnumUtils.getEnumIgnoreCase;
import static org.apache.commons.lang3.EnumUtils.isValidEnumIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.util.CollectionUtils.isEmpty;

public class ValidatePostadresse {

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

		if (!isEmpty(request.getFiltrerAdressebeskyttelse()) && !isValidAdressebeskyttelseGradering(request)) {
			throw new RegoppslagIllegalArgumentException(getInvalidFilterAdressebeskyttelse(request), BAD_REQUEST);
		}
	}

	public static void validateFiltrerAdressebeskyttelse(PostadresseRequest postadresseRequest, Gradering beskyttelsesgrad) {
		if (!isEmpty(postadresseRequest.getFiltrerAdressebeskyttelse()) && nonNull(beskyttelsesgrad)) {
			Set<Gradering> adressebeskyttelse = getValidGradering(postadresseRequest);
			if (adressebeskyttelse.contains(beskyttelsesgrad))
				throw new RegOppslagIkkeFunnetException("Adresse finnes ikke", NOT_FOUND);
		}
	}
	public static Set<Gradering> getValidGradering(PostadresseRequest request) {
		if(!isEmpty(request.getFiltrerAdressebeskyttelse())) {
			return request.getFiltrerAdressebeskyttelse().stream()
					.filter(gradering -> isValidEnumIgnoreCase(Gradering.class, gradering))
					.map(adressebeskyttelse -> getEnumIgnoreCase(Gradering.class, adressebeskyttelse))
					.collect(Collectors.toSet());
		}
		return Collections.emptySet();
	}

	public static String getInvalidFilterAdressebeskyttelse(PostadresseRequest request) {
		if(!isEmpty(request.getFiltrerAdressebeskyttelse())) {
			String invalidInput = request.getFiltrerAdressebeskyttelse().stream()
					.filter(gradering -> !isValidEnumIgnoreCase(Gradering.class, gradering))
					.collect(Collectors.joining(","));

			return format("%s {%s} og filtrerAdressebeskyttelse må være en av {%s}", UGYLDIG_INPUT, invalidInput, Arrays.stream(values())
					.map(Enum::name)
					.collect(Collectors.joining(",")));
		}
		return null;
	}

	public static boolean isValidAdressebeskyttelseGradering(PostadresseRequest request) {
		return request.getFiltrerAdressebeskyttelse().stream()
				.allMatch(gradering -> isValidEnumIgnoreCase(Gradering.class, gradering));
	}
}
