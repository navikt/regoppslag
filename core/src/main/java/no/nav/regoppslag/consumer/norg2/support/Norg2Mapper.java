package no.nav.regoppslag.consumer.norg2.support;

import no.nav.dok.brevdata.felles.v1.navfelles.AdresseEnhet;
import no.nav.dok.brevdata.felles.v1.navfelles.NavEnhet;
import no.nav.dok.brevdata.felles.v1.navfelles.NorskPostadresse;
import no.nav.regoppslag.consumer.norg2.to.Adresse;
import no.nav.regoppslag.consumer.norg2.to.EnhetKontaktinformasjon;
import no.nav.regoppslag.consumer.norg2.to.EnhetNavn;
import no.nav.regoppslag.consumer.norg2.to.Stedsadresse;
import no.nav.regoppslag.service.PostnummerService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
public class Norg2Mapper {

	public static final String POSTBOKSADRESSE = "postboksadresse";
	public static final String STEDSADRESSE = "stedsadresse";

	@Autowired
	private final PostnummerService postnummerService;

	public Norg2Mapper(PostnummerService postnummerService) {
		this.postnummerService = postnummerService;
	}

	public void mapPostadresse(EnhetNavn organisasjonsenhetNavn,
							   EnhetKontaktinformasjon kontaktinformasjon, AdresseEnhet adresseEnhet) {
		if (nonNull(organisasjonsenhetNavn)) {
			adresseEnhet.setEnhetsNavn(organisasjonsenhetNavn.getNavn());
		}

		if (nonNull(kontaktinformasjon)) {
			adresseEnhet.setKontaktTelefonnummer(kontaktinformasjon.getTelefonnummer());
			adresseEnhet.setAdresse(mapEnhetKontaktinformasjon(kontaktinformasjon));
		}
	}

	private NorskPostadresse mapEnhetKontaktinformasjon(EnhetKontaktinformasjon kontaktinformasjon) {
		NorskPostadresse norskPostadresse = new NorskPostadresse();
		if (isNull(kontaktinformasjon.getPostadresse())) {
			return null;
		}

		if (STEDSADRESSE.equals(kontaktinformasjon.getPostadresse().getType())) {
			Adresse stedsadresse = kontaktinformasjon.getPostadresse();
			norskPostadresse.setAdresselinje1(ofNullable(stedsadresse.getGatenavn()).orElse("") + " " +
					ofNullable(stedsadresse.getHusnummer()).orElse("") +
					ofNullable(stedsadresse.getHusbokstav()).orElse(""));
			norskPostadresse.setPostnummer(stedsadresse.getPostnummer());
			norskPostadresse.setPoststed(isNotBlank(stedsadresse.getPoststed()) ? stedsadresse.getPoststed() :
					postnummerService.finnPoststed(stedsadresse.getPostnummer()));
			return norskPostadresse;
		} else {
			Adresse postadresse = kontaktinformasjon.getPostadresse();
			norskPostadresse.setAdresselinje1(ofNullable("Postboks " + postadresse.getPostboksnummer()).orElse("") + " " +
					ofNullable(postadresse.getPostboksanlegg()).orElse(""));
			norskPostadresse.setPostnummer(postadresse.getPostnummer());
			norskPostadresse.setPoststed(isNotBlank(postadresse.getPoststed()) ? postadresse.getPoststed() :
					postnummerService.finnPoststed(postadresse.getPostnummer()));
			return norskPostadresse;
		}
	}


	public void mapBesokadresse(EnhetNavn organisasjonsenhetNavn, EnhetKontaktinformasjon kontaktinformasjon, AdresseEnhet adresseEnhet) {
		if (nonNull(organisasjonsenhetNavn)) {
			adresseEnhet.setEnhetsNavn(organisasjonsenhetNavn.getNavn());
		}

		if (nonNull(kontaktinformasjon)) {
			adresseEnhet.setKontaktTelefonnummer(kontaktinformasjon.getTelefonnummer());
			adresseEnhet.setAdresse(mapEnhetBesokadresse(kontaktinformasjon.getBesoeksadresse()));
		}
	}

	private NorskPostadresse mapEnhetBesokadresse(Stedsadresse besoeksadresse) {
		if (isNull(besoeksadresse)) {
			return null;
		}
		NorskPostadresse postadresse = new NorskPostadresse();

		postadresse.setAdresselinje1(ofNullable(besoeksadresse.getGatenavn()).orElse("") + " " + ofNullable(besoeksadresse.getHusnummer()).orElse("") + ofNullable(besoeksadresse.getHusbokstav()).orElse(""));
		if (StringUtils.isNotBlank(besoeksadresse.getPostnummer())) {
			postadresse.setPostnummer(besoeksadresse.getPostnummer());
			postadresse.setPoststed(isNotBlank(besoeksadresse.getPoststed()) ? besoeksadresse.getPoststed() : postnummerService.finnPoststed(besoeksadresse.getPostnummer()));
		}
		return postadresse;
	}

	public void mapEnhetNavn(EnhetNavn rsEnhetNavn, NavEnhet navEnhet) {
		if (rsEnhetNavn != null) {
			navEnhet.setEnhetsNavn(rsEnhetNavn.getNavn());
		}
	}
}
