package no.nav.regoppslag.consumer.norg2.support;

import no.nav.dok.brevdata.felles.v1.navfelles.AdresseEnhet;
import no.nav.dok.brevdata.felles.v1.navfelles.NavEnhet;
import no.nav.dok.brevdata.felles.v1.navfelles.NorskPostadresse;
import no.nav.regoppslag.service.PostnummerService;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.informasjon.Gateadresse;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.informasjon.Organisasjonsenhet;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.informasjon.PostboksadresseNorsk;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.informasjon.Stedsadresse;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Optional;

/**
 * @author Ketill Fenne, Visma Consulting AS
 */
@Component
public class Norg2Mapper {

	@Inject
	private final PostnummerService postnummerService;

	public Norg2Mapper(PostnummerService postnummerService) {
		this.postnummerService = postnummerService;
	}

	public void mapPostadresse(Organisasjonsenhet enhet, AdresseEnhet adresse) {
		if (enhet != null) {
			adresse.setEnhetsNavn(enhet.getEnhetNavn());
			if (enhet.getKontaktinformasjon() != null) {
				adresse.setKontaktTelefonnummer(enhet.getKontaktinformasjon().getTelefonnummer());
				NorskPostadresse postadresse = new NorskPostadresse();
				if (enhet.getKontaktinformasjon().getPostadresse() != null) {
					if (enhet.getKontaktinformasjon().getPostadresse() instanceof Stedsadresse) {
						Gateadresse gateadresse = (Gateadresse) enhet.getKontaktinformasjon().getPostadresse();
						postadresse.setAdresselinje1(Optional.ofNullable(gateadresse.getGatenavn()).orElse("") + " " + Optional.ofNullable(gateadresse.getHusnummer()).orElse("") + Optional.ofNullable(gateadresse.getHusbokstav()).orElse(""));
						postadresse.setPostnummer(gateadresse.getPoststed().getKodeRef());
						postadresse.setPoststed(postnummerService.finnPoststed(gateadresse.getPoststed().getKodeRef()));
					} else {
						PostboksadresseNorsk postboksadresseNorsk = (PostboksadresseNorsk) enhet.getKontaktinformasjon().getPostadresse();
						postadresse.setAdresselinje1(Optional.ofNullable(postboksadresseNorsk.getPostboksnummer()).orElse("") + " " + Optional.ofNullable(postboksadresseNorsk.getPostboksanlegg()).orElse(""));
						postadresse.setPostnummer(postboksadresseNorsk.getPoststed().getValue());
						postadresse.setPoststed(postnummerService.finnPoststed(postboksadresseNorsk.getPoststed().getValue()));
					}
				}
				adresse.setAdresse(postadresse);
			}
		}
	}

	public void mapBesokadresse(Organisasjonsenhet enhet, AdresseEnhet adresse) {
		if (enhet != null) {
			adresse.setEnhetsNavn(enhet.getEnhetNavn());
			if (enhet.getKontaktinformasjon() != null) {
				adresse.setKontaktTelefonnummer(enhet.getKontaktinformasjon().getTelefonnummer());
				adresse.setEnhetsNavn(enhet.getEnhetNavn());
				NorskPostadresse postadresse = new NorskPostadresse();
				if (enhet.getKontaktinformasjon().getBesoeksadresse() != null) {
					Gateadresse gateadresse = enhet.getKontaktinformasjon().getBesoeksadresse();
					if (gateadresse != null) {
						postadresse.setAdresselinje1(Optional.ofNullable(gateadresse.getGatenavn()).orElse("") + " " + Optional.ofNullable(gateadresse.getHusnummer()).orElse("") + Optional.ofNullable(gateadresse.getHusbokstav()).orElse(""));

						if (gateadresse.getPoststed() != null) {
							postadresse.setPostnummer(gateadresse.getPoststed().getValue());
							postadresse.setPoststed(postnummerService.finnPoststed(gateadresse.getPoststed().getValue()));
						}
					}
					adresse.setAdresse(postadresse);
				}
			}
		}
	}

	public void mapEnhetNavn(Organisasjonsenhet wsEnhet, NavEnhet navEnhet) {
		if (wsEnhet != null) {
			navEnhet.setEnhetsNavn(wsEnhet.getEnhetNavn());
		}
	}
}
