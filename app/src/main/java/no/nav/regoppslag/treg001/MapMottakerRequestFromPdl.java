package no.nav.regoppslag.treg001;

import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.navfelles.NorskPostadresse;
import no.nav.dok.brevdata.felles.v1.navfelles.Person;
import no.nav.dok.brevdata.felles.v1.navfelles.UtenlandskPostadresse;
import no.nav.regoppslag.consumer.pdl.PdlMottakerInfo;
import no.nav.regoppslag.consumer.pdl.PostadresseTo;
import no.nav.regoppslag.exceptions.RegoppslagIllegalArgumentException;
import no.nav.regoppslag.service.LandkodeService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static java.util.Objects.isNull;
import static no.nav.regoppslag.consumer.pdl.PDLConstant.POSTADRESSE_INNLAND;
import static no.nav.regoppslag.consumer.pdl.PDLConstant.POSTADRESSE_UTLAND;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Component
public class MapMottakerRequestFromPdl {

	private final LandkodeService landkodeService;

	@Inject
	public MapMottakerRequestFromPdl(LandkodeService landkodeService) {
		this.landkodeService = landkodeService;
	}

	public Mottaker mapMottakerFraPdl(PdlMottakerInfo pdlMottakerInfo) {
		Mottaker mottaker = new Person();

		if (isNull(pdlMottakerInfo.getPostadresse()) || isBlank(pdlMottakerInfo.getPostadresse().getAdresseType())) {
			throw new RegoppslagIllegalArgumentException("Mottaker adresse kan ikke bli null", BAD_REQUEST);
		}
		mottaker.setKortNavn(pdlMottakerInfo.getKortNavn());
		mottaker.setNavn(pdlMottakerInfo.getNavn());

		PostadresseTo postadresse = pdlMottakerInfo.getPostadresse();

		if (isNotBlank(postadresse.getAdresseType()) && POSTADRESSE_INNLAND.equalsIgnoreCase(postadresse.getAdresseType())) {
			NorskPostadresse norskPostadresse = new NorskPostadresse();
			norskPostadresse.setLand(isNotBlank(postadresse.getLandkode()) ? landkodeService.finnLandnavn(postadresse.getLandkode()) : null);
			norskPostadresse.setAdresselinje1(postadresse.getAdresselinje1());
			norskPostadresse.setAdresselinje2(postadresse.getAdresselinje2());
			norskPostadresse.setAdresselinje3(postadresse.getAdresselinje3());
			norskPostadresse.setPostnummer(postadresse.getPostnummer());
			norskPostadresse.setPoststed(postadresse.getPoststed());
			mottaker.setMottakeradresse(norskPostadresse);
		} else if (isNotBlank(postadresse.getAdresseType()) && POSTADRESSE_UTLAND.equalsIgnoreCase(postadresse.getAdresseType())) {
			UtenlandskPostadresse utenlandskPostadresse = new UtenlandskPostadresse();
			utenlandskPostadresse.setLand(isNotBlank(postadresse.getLandkode()) ? landkodeService.finnLandnavn(postadresse.getLandkode()) : null);
			utenlandskPostadresse.setAdresselinje1(postadresse.getAdresselinje1());
			utenlandskPostadresse.setAdresselinje2(postadresse.getAdresselinje2());
			utenlandskPostadresse.setAdresselinje3(postadresse.getAdresselinje3());
			mottaker.setMottakeradresse(utenlandskPostadresse);
		}
		return mottaker;
	}

}
