package no.nav.regoppslag.pdl;

import no.nav.regoppslag.consumer.pdl.to.HentPerson;
import no.nav.regoppslag.exceptions.RegoppslagIllegalArgumentException;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class MapPDLUtils {

	private static final String ERROR_MELDING = "Feltet %s kan ikke være null eller tomt";
	private static final String FORNAVN = "Fornavn";
	private static final String ETTERNAVN = "Etternavn";

	public static String getFulltnavn(List<HentPerson.PersonNavn> navns) {
		return navns.stream().filter(Objects::nonNull)
				.map(MapPDLUtils::mapPersonnavn)
				.filter(Objects::nonNull)
				.findFirst().orElseThrow(() -> new RegoppslagIllegalArgumentException(format(ERROR_MELDING, "Personnavn"), BAD_REQUEST));

	}

	private static String mapPersonnavn(HentPerson.PersonNavn personNavn) {
		if (isBlank(personNavn.getFornavn()) || isBlank(personNavn.getEtternavn())) {
			throw new RegoppslagIllegalArgumentException(format(ERROR_MELDING, isBlank(personNavn.getFornavn()) ? FORNAVN : ETTERNAVN), BAD_REQUEST);
		}
		return trim(getNavn(personNavn.getFornavn()) + getNavn(personNavn.getMellomnavn()) + getNavn(personNavn.getEtternavn()));
	}

	public static String getForkortetNavn(List<HentPerson.PersonNavn> navns) {
		return navns.stream()
				.map(HentPerson.PersonNavn::getForkortetNavn)
				.filter(Objects::nonNull)
				.findFirst().orElse(null);
	}

	public static LocalDate getFoedselsdato(HentPerson hentPerson) {
		return hentPerson.getFoedsel().stream()
				.map(HentPerson.Foedsel::getFoedselsdato)
				.filter(Objects::nonNull)
				.findAny()
				.orElse(null);
	}

	public static String getFolkeregisterstatus(HentPerson hentPerson) {
		return hentPerson.getFolkeregisterpersonstatus().stream()
				.filter(Objects::nonNull)
				.map(HentPerson.Folkeregisterpersonstatus::getStatus)
				.filter(Objects::nonNull)
				.findAny().orElse(null);
	}

	public static String getNavn(String navn) {
		return isBlank(navn) ? "" : navn + " ";
	}

	public static <T> T requireNonNull(T obj, String message) {
		if (obj == null)
			throw new RegoppslagIllegalArgumentException(message, BAD_REQUEST);
		return obj;
	}

	public static String getIdentifikasjonsnummer(List<HentPerson.Folkeregisteridentifikator> folkeregisteridentifikator) {
		return folkeregisteridentifikator.stream()
				.filter(Objects::nonNull)
				.map(HentPerson.Folkeregisteridentifikator::getIdentifikasjonsnummer)
				.filter(Objects::nonNull)
				.findAny()
				.orElse(null);
	}
}
