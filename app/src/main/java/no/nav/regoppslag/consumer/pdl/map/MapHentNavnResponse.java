package no.nav.regoppslag.consumer.pdl.map;

import no.nav.regoppslag.consumer.pdl.PDLHentNavnResponse;
import no.nav.regoppslag.exceptions.RegoppslagIllegalArgumentException;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;


public class MapHentNavnResponse {

	public String mapNavn(PDLHentNavnResponse response) {
		if (isNull(response) || isNull(response.getData()) || isNull(response.getData().getHentPerson())) {
			throw new RegoppslagIllegalArgumentException("Personal kan ikke vær null");
		}
		PDLHentNavnResponse.HentPerson hentPerson = response.getData().getHentPerson();

		return hentPerson.getNavn().stream().filter(Objects::nonNull)
				.map(personNavn -> nonNull(personNavn.getFornavn()) ? personNavn.getFornavn() + " " +
				(isBlank(personNavn.getMellomnavn()) ? "" : personNavn.getMellomnavn() + " ") +
				personNavn.getEtternavn() : null).filter(Objects::nonNull)
				.findFirst().orElse(null);
	}

}
