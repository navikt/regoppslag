package no.nav.regoppslag.consumer.pdl.map;

import no.nav.regoppslag.consumer.pdl.to.PDLHentNavnResponse;
import no.nav.regoppslag.consumer.pdl.to.PersonNavn;
import no.nav.regoppslag.exceptions.RegoppslagIllegalArgumentException;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

public final class HentNavnMapper {

	private HentNavnMapper() {
	}

	public static String mapNavn(PDLHentNavnResponse response) {
		if (isNull(response) || isNull(response.data()) || isNull(response.data().hentPerson())) {
			throw new RegoppslagIllegalArgumentException("Personnavn kan ikke være null", BAD_REQUEST);
		}
		PDLHentNavnResponse.HentPerson hentPerson = response.data().hentPerson();

		if (isNull(hentPerson.navn()) || hentPerson.navn().isEmpty()) {
			throw new RegoppslagIllegalArgumentException("Personnavn kan ikke være null", BAD_REQUEST);
		}

		return hentPerson.navn().stream()
				.filter(HentNavnMapper::harFoerOgEtternavn)
				.map(HentNavnMapper::mapPersonNavn)
				.findFirst().orElseThrow(() -> new RegoppslagIllegalArgumentException("Fornavn eller etternav kan ikke være null", BAD_REQUEST));
	}

	public static String mapNavnForDoedsbo(PDLHentNavnResponse response) {
		if (isNull(response) || isNull(response.data()) || isNull(response.data().hentPerson())) {
			return null;
		}
		PDLHentNavnResponse.HentPerson hentPerson = response.data().hentPerson();

		if (isNull(hentPerson.navn()) || hentPerson.navn().isEmpty()) {
			return null;
		}

		return hentPerson.navn().stream()
				.filter(HentNavnMapper::harFoerOgEtternavn)
				.map(HentNavnMapper::mapPersonNavn)
				.findFirst().orElse(null);
	}

	private static String mapPersonNavn(PersonNavn personNavn) {
		return Stream.of(personNavn.fornavn(), personNavn.mellomnavn(), personNavn.etternavn())
				.filter(StringUtils::isNotBlank)
				.map(String::trim)
				.collect(Collectors.joining(" "))
				.trim();
	}

	private static boolean harFoerOgEtternavn(PersonNavn personNavn) {
		return isNotBlank(personNavn.fornavn()) && isNotBlank(personNavn.etternavn());
	}

}
