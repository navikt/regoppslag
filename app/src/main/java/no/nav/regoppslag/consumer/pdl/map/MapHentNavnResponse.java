package no.nav.regoppslag.consumer.pdl.map;

import no.nav.regoppslag.consumer.pdl.to.PDLHentNavnResponse;
import no.nav.regoppslag.consumer.pdl.to.PersonNavn;
import no.nav.regoppslag.exceptions.RegoppslagIllegalArgumentException;

import java.util.Objects;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class MapHentNavnResponse {

	public String mapNavn(PDLHentNavnResponse response) {
		if (isNull(response) || isNull(response.getData()) || isNull(response.getData().getHentPerson())) {
			throw new RegoppslagIllegalArgumentException("Personnavn kan ikke være null", BAD_REQUEST);
		}
		PDLHentNavnResponse.HentPerson hentPerson = response.getData().getHentPerson();

		if (isNull(hentPerson.getNavn()) || hentPerson.getNavn().isEmpty()) {
			throw new RegoppslagIllegalArgumentException("Personnavn kan ikke være null", BAD_REQUEST);
		}

		return hentPerson.getNavn().stream()
				.filter(this::isBlankForogEtternavn)
				.map(personNavn -> nonNull(personNavn.getFornavn()) ? trim(personNavn.getFornavn() + " " +
						(isBlank(personNavn.getMellomnavn()) ? "" : personNavn.getMellomnavn() + " ") +
						personNavn.getEtternavn()) : null).filter(Objects::nonNull)
				.findFirst().orElseThrow(() -> new RegoppslagIllegalArgumentException("Fornavn eller etternav kan ikke være null", BAD_REQUEST));
	}

	private boolean isBlankForogEtternavn(PersonNavn personNavn) {
		return isNotBlank(personNavn.getFornavn()) && isNotBlank(personNavn.getEtternavn());
	}

}
