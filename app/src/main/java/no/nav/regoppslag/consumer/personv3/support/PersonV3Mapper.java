package no.nav.regoppslag.consumer.personv3.support;

import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;
import no.nav.dok.metaforcemal.jaxb2.gen.Mottaker;
import no.nav.dok.metaforcemal.jaxb2.gen.NorskPostadresse;
import no.nav.dok.metaforcemal.jaxb2.gen.Spraakkode;
import no.nav.regoppslag.service.LandkodeService;
import no.nav.regoppslag.service.PostnummerService;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Gateadresse;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Matrikkeladresse;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.MidlertidigPostadresseNorge;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.MidlertidigPostadresseUtland;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Postboksadresse;
import org.springframework.stereotype.Component;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PostboksadresseNorsk;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.StedsadresseNorge;

import java.util.Optional;

/**
 * @author Ketill Fenne, Visma Consulting AS
 */
@Component
public class PersonV3Mapper {

	PostnummerService postnummerService;
	LandkodeService landkodeService;

	public void map(Bruker person, Mottaker mottaker) {
		if (person.getMaalform() != null) {
			if (person.getMaalform().getValue() == "NO") {
				mottaker.setSpraakkode(Spraakkode.NB);
			} else {
				mottaker.setSpraakkode(Spraakkode.valueOf(person.getMaalform().getValue()));
			}
		}
		mottaker.setKortNavn(person.getPersonnavn().getSammensattNavn());
		if (person.getPersonnavn().getMellomnavn() == null) {
			mottaker.setNavn(person.getPersonnavn().getFornavn() + " " + person.getPersonnavn().getEtternavn());
		} else {
			mottaker.setNavn(person.getPersonnavn().getFornavn() + " " + person.getPersonnavn().getMellomnavn() + " " + person.getPersonnavn().getEtternavn());
		}
		NorskPostadresse norskPostadresse = new NorskPostadresse();
		if (person.getGjeldendePostadressetype() != null && "BOSTEDSADRESSE".equals(person.getGjeldendePostadressetype().getKodeverksRef()) && person.getBostedsadresse() != null) {
			if (person.getBostedsadresse().getStrukturertAdresse() instanceof Gateadresse) {
				Gateadresse gateadresse = (Gateadresse) person.getBostedsadresse().getStrukturertAdresse();
				norskPostadresse.setAdresselinje1(Optional.ofNullable(gateadresse.getGatenavn()).orElse("") + " " + Optional.ofNullable(gateadresse.getHusnummer().toString()).orElse("") + Optional.ofNullable(gateadresse.getHusbokstav()).orElse(""));
			} else if (person.getBostedsadresse().getStrukturertAdresse() instanceof Matrikkeladresse) {
				Matrikkeladresse matrikkeladresse = (Matrikkeladresse) person.getBostedsadresse().getStrukturertAdresse();
				norskPostadresse.setAdresselinje1(matrikkeladresse.getEiendomsnavn());
			} else if (person.getBostedsadresse().getStrukturertAdresse() instanceof Postboksadresse) {
				Postboksadresse postboksadresse = (Postboksadresse) person.getBostedsadresse().getStrukturertAdresse();
				norskPostadresse.setAdresselinje1(postboksadresse.getPostboksnummer());
			}
			if (person.getBostedsadresse().getStrukturertAdresse() instanceof StedsadresseNorge) {
				StedsadresseNorge stedsadresseNorge = (StedsadresseNorge) person.getBostedsadresse().getStrukturertAdresse();
				if (stedsadresseNorge.getPoststed() != null) {
					norskPostadresse.setPostnummer(stedsadresseNorge.getPoststed().getValue());
					norskPostadresse.setPoststed(postnummerService.finnPoststed(stedsadresseNorge.getPoststed().getValue()));
				}
			} else if (person.getBostedsadresse().getStrukturertAdresse() instanceof PostboksadresseNorsk) {
				PostboksadresseNorsk postboksadresseNorsk = (PostboksadresseNorsk) person.getBostedsadresse().getStrukturertAdresse();
				if (postboksadresseNorsk.getPoststed() != null) {
					norskPostadresse.setPostnummer(postboksadresseNorsk.getPoststed().getKodeverksRef());
					norskPostadresse.setPoststed(postnummerService.finnPoststed(postboksadresseNorsk.getPoststed().getValue()));
				}
			}
			//TODO lookup
			if (person.getBostedsadresse().getStrukturertAdresse().getLandkode() != null) {
				norskPostadresse.setLand(landkodeService.finnLandnavn(person.getBostedsadresse().getStrukturertAdresse().getLandkode().getValue()));
			}
		} else if (person.getGjeldendePostadressetype() != null && "POSTADRESSE".equals(person.getGjeldendePostadressetype().getKodeverksRef()) && person.getPostadresse().getUstrukturertAdresse() != null) {
			norskPostadresse.setAdresselinje1(person.getPostadresse().getUstrukturertAdresse().getAdresselinje1());
			norskPostadresse.setAdresselinje2(person.getPostadresse().getUstrukturertAdresse().getAdresselinje2());
			norskPostadresse.setAdresselinje3(person.getPostadresse().getUstrukturertAdresse().getAdresselinje3());

			if (person.getPostadresse().getUstrukturertAdresse().getAdresselinje4() != null && person.getPostadresse().getUstrukturertAdresse().getAdresselinje4().length() == 4) {
				if (StringUtils.isNumeric(person.getPostadresse().getUstrukturertAdresse().getAdresselinje4())) {
					norskPostadresse.setPostnummer(person.getPostadresse().getUstrukturertAdresse().getAdresselinje4());
					norskPostadresse.setPoststed(postnummerService.finnPoststed(person.getPostadresse().getUstrukturertAdresse().getAdresselinje4()));
				}
			}
			if (person.getPostadresse().getUstrukturertAdresse().getLandkode() != null) {
				norskPostadresse.setLand(landkodeService.finnLandnavn(person.getPostadresse().getUstrukturertAdresse().getLandkode().getValue()));
			}
		} else if (person.getGjeldendePostadressetype() != null && "MIDLERTIDIG_POSTADRESSE_UTLAND".equals(person.getGjeldendePostadressetype().getKodeverksRef()) && person.getMidlertidigPostadresse() != null) {
			MidlertidigPostadresseUtland midlertidigPostadresseUtland = (MidlertidigPostadresseUtland) person.getMidlertidigPostadresse();
			if (midlertidigPostadresseUtland.getUstrukturertAdresse() != null) {
				norskPostadresse.setAdresselinje1(midlertidigPostadresseUtland.getUstrukturertAdresse().getAdresselinje1());
				norskPostadresse.setAdresselinje2(midlertidigPostadresseUtland.getUstrukturertAdresse().getAdresselinje2());
				norskPostadresse.setAdresselinje3(midlertidigPostadresseUtland.getUstrukturertAdresse().getAdresselinje3());
				if (midlertidigPostadresseUtland.getUstrukturertAdresse().getAdresselinje4() != null && midlertidigPostadresseUtland.getUstrukturertAdresse().getAdresselinje4().length() == 4) {
					if (StringUtils.isNumeric(midlertidigPostadresseUtland.getUstrukturertAdresse().getAdresselinje4())) {
						norskPostadresse.setPostnummer(midlertidigPostadresseUtland.getUstrukturertAdresse().getAdresselinje4());
						norskPostadresse.setPoststed(postnummerService.finnPoststed(midlertidigPostadresseUtland.getUstrukturertAdresse().getAdresselinje4()));
					}
				}
			}
			if (midlertidigPostadresseUtland.getUstrukturertAdresse().getLandkode() != null) {
				norskPostadresse.setLand(landkodeService.finnLandnavn(midlertidigPostadresseUtland.getUstrukturertAdresse().getLandkode().getValue()));
			}
		} else if (person.getGjeldendePostadressetype() != null && "MIDLERTIDIG_POSTADRESSE_NORGE".equals(person.getGjeldendePostadressetype().getKodeverksRef()) && person.getMidlertidigPostadresse() != null) {
			if (((MidlertidigPostadresseNorge) person.getMidlertidigPostadresse()).getStrukturertAdresse() instanceof Gateadresse) {
				Gateadresse gateadresse = (Gateadresse) ((MidlertidigPostadresseNorge) person.getMidlertidigPostadresse()).getStrukturertAdresse();
				norskPostadresse.setAdresselinje1(Optional.ofNullable(gateadresse.getGatenavn()).orElse("") + " " + Optional.ofNullable(gateadresse.getHusnummer().toString()).orElse("") + Optional.ofNullable(gateadresse.getHusbokstav()).orElse(""));
			} else if (((MidlertidigPostadresseNorge) person.getMidlertidigPostadresse()).getStrukturertAdresse() instanceof Matrikkeladresse) {
				Matrikkeladresse matrikkeladresse = (Matrikkeladresse) ((MidlertidigPostadresseNorge) person.getMidlertidigPostadresse()).getStrukturertAdresse();
				norskPostadresse.setAdresselinje1(matrikkeladresse.getEiendomsnavn());
			} else if (((MidlertidigPostadresseNorge) person.getMidlertidigPostadresse()).getStrukturertAdresse() instanceof Postboksadresse) {
				Postboksadresse postboksadresse = (Postboksadresse) ((MidlertidigPostadresseNorge) person.getMidlertidigPostadresse()).getStrukturertAdresse();
				norskPostadresse.setAdresselinje1(postboksadresse.getPostboksnummer());
			}
			if (((MidlertidigPostadresseNorge) person.getMidlertidigPostadresse()).getStrukturertAdresse() instanceof StedsadresseNorge) {
				StedsadresseNorge stedsadresseNorge = (StedsadresseNorge) ((MidlertidigPostadresseNorge) person.getMidlertidigPostadresse()).getStrukturertAdresse();
				if (stedsadresseNorge.getPoststed() != null) {
					norskPostadresse.setPostnummer(stedsadresseNorge.getPoststed().getValue());
					norskPostadresse.setPoststed(postnummerService.finnPoststed(stedsadresseNorge.getPoststed().getValue()));
				}
			} else if (((MidlertidigPostadresseNorge) person.getMidlertidigPostadresse()).getStrukturertAdresse() instanceof PostboksadresseNorsk) {
				PostboksadresseNorsk postboksadresseNorsk = (PostboksadresseNorsk) ((MidlertidigPostadresseNorge) person.getMidlertidigPostadresse()).getStrukturertAdresse();
				if (postboksadresseNorsk.getPoststed() != null) {
					norskPostadresse.setPostnummer(postboksadresseNorsk.getPoststed().getValue());
					norskPostadresse.setPoststed(postnummerService.finnPoststed(postboksadresseNorsk.getPoststed().getValue()));
				}
			}
			if (((MidlertidigPostadresseNorge) person.getMidlertidigPostadresse()).getStrukturertAdresse().getLandkode() != null) {
				norskPostadresse.setLand(landkodeService.finnLandnavn(((MidlertidigPostadresseNorge) person.getMidlertidigPostadresse()).getStrukturertAdresse().getLandkode().getValue()));
			}
		}
		if (StringUtils.isEmpty(norskPostadresse.getPostnummer())){
			norskPostadresse.setPostnummer("0000");
			norskPostadresse.setPoststed("UKJENT/UNKNOWN");
		}
		mottaker.setAdresse(norskPostadresse);
	}
}