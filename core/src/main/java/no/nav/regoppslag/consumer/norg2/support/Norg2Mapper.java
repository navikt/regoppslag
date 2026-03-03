package no.nav.regoppslag.consumer.norg2.support;

import no.nav.dok.brevdata.felles.v1.navfelles.AdresseEnhet;
import no.nav.dok.brevdata.felles.v1.navfelles.NavEnhet;
import no.nav.dok.brevdata.felles.v1.navfelles.NorskPostadresse;
import no.nav.regoppslag.consumer.norg2.to.Adresse;
import no.nav.regoppslag.consumer.norg2.to.EnhetKontaktinformasjon;
import no.nav.regoppslag.consumer.norg2.to.EnhetNavn;
import no.nav.regoppslag.consumer.norg2.to.Stedsadresse;
import no.nav.regoppslag.service.PostnummerService;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class Norg2Mapper {

	public static final String POSTBOKSADRESSE = "postboksadresse";
	public static final String STEDSADRESSE = "stedsadresse";

	public static void mapPostadresse(EnhetNavn organisasjonsenhetNavn,
							   EnhetKontaktinformasjon kontaktinformasjon, AdresseEnhet adresseEnhet) {
		if (nonNull(organisasjonsenhetNavn)) {
			adresseEnhet.setEnhetsNavn(organisasjonsenhetNavn.getNavn());
		}

		if (nonNull(kontaktinformasjon)) {
			adresseEnhet.setKontaktTelefonnummer(kontaktinformasjon.getTelefonnummer());
			NorskPostadresse norskPostadresse = mapEnhetKontaktinformasjon(kontaktinformasjon);
			if (nonNull(norskPostadresse)) {
				adresseEnhet.setAdresse(norskPostadresse);
			}
		}
	}

	private static NorskPostadresse mapEnhetKontaktinformasjon(EnhetKontaktinformasjon kontaktinformasjon) {
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
					PostnummerService.finnPoststed(stedsadresse.getPostnummer()));
			return norskPostadresse;
		} else {
			Adresse postadresse = kontaktinformasjon.getPostadresse();
			norskPostadresse.setAdresselinje1(Optional.of("Postboks " + postadresse.getPostboksnummer()).orElse("") + " " +
											  ofNullable(postadresse.getPostboksanlegg()).orElse(""));
			norskPostadresse.setPostnummer(postadresse.getPostnummer());
			norskPostadresse.setPoststed(isNotBlank(postadresse.getPoststed()) ? postadresse.getPoststed() :
					PostnummerService.finnPoststed(postadresse.getPostnummer()));
			return norskPostadresse;
		}
	}

	public static void mapBesokadresse(EnhetNavn organisasjonsenhetNavn, EnhetKontaktinformasjon kontaktinformasjon, AdresseEnhet adresseEnhet) {
		if (nonNull(organisasjonsenhetNavn)) {
			adresseEnhet.setEnhetsNavn(organisasjonsenhetNavn.getNavn());
		}

		if (nonNull(kontaktinformasjon)) {
			adresseEnhet.setKontaktTelefonnummer(kontaktinformasjon.getTelefonnummer());
			NorskPostadresse norskPostadresse = mapEnhetBesokadresse(kontaktinformasjon.getBesoeksadresse(), kontaktinformasjon.getPostadresse());
			if (nonNull(norskPostadresse)) {
				adresseEnhet.setAdresse(norskPostadresse);
			}
		}
	}

	private static NorskPostadresse mapEnhetBesokadresse(Stedsadresse besoeksadresse, Adresse adresse) {
		NorskPostadresse postadresse = new NorskPostadresse();

		if (nonNull(adresse) && STEDSADRESSE.equals(adresse.getType())) {
			return getNorskPostadresse(postadresse, adresse.getGatenavn(), adresse.getHusnummer(), adresse.getHusbokstav(),
					adresse.getPostnummer(), adresse.getPoststed());
		} else if (nonNull(besoeksadresse)) {
			return getNorskPostadresse(postadresse, besoeksadresse.getGatenavn(), besoeksadresse.getHusnummer(),
					besoeksadresse.getHusbokstav(), besoeksadresse.getPostnummer(), besoeksadresse.getPoststed());
		}
		return null;
	}

	private static NorskPostadresse getNorskPostadresse(NorskPostadresse postadresse, String gatenavn, String husnummer, String husbokstav, String postnummer, String poststed) {
		postadresse.setAdresselinje1(ofNullable(gatenavn)
				.orElse("") + " " + ofNullable(husnummer).orElse("") + ofNullable(husbokstav).orElse(""));
		if (isNotBlank(postnummer)) {
			postadresse.setPostnummer(postnummer);
			postadresse.setPoststed(isNotBlank(poststed) ? poststed : PostnummerService.finnPoststed(postnummer));
		}
		return postadresse;
	}

	public static void mapEnhetNavn(EnhetNavn rsEnhetNavn, NavEnhet navEnhet) {
		if (rsEnhetNavn != null) {
			navEnhet.setEnhetsNavn(rsEnhetNavn.getNavn());
		}
	}
}
