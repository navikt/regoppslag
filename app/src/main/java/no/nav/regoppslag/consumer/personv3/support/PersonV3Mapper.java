package no.nav.regoppslag.consumer.personv3.support;

import static no.nav.regoppslag.metrics.PrometheusLabels.LAND;
import static no.nav.regoppslag.metrics.PrometheusLabels.PERSONV3_MAPPER;
import static no.nav.regoppslag.metrics.PrometheusLabels.UKJENT_LAND;
import static no.nav.regoppslag.metrics.PrometheusLabels.UKJENT_POSTNUMMER;
import static no.nav.regoppslag.metrics.PrometheusMetrics.getConsumerId;
import static no.nav.regoppslag.metrics.PrometheusMetrics.requestCounter;
import static org.apache.commons.lang.StringUtils.isBlank;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;
import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.navfelles.NorskPostadresse;
import no.nav.dok.brevdata.felles.v1.navfelles.Sakspart;
import no.nav.dok.brevdata.felles.v1.simpletypes.Spraakkode;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.service.LandkodeService;
import no.nav.regoppslag.service.PostnummerService;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Gateadresse;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Matrikkeladresse;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.MidlertidigPostadresseNorge;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.MidlertidigPostadresseUtland;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Postboksadresse;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PostboksadresseNorsk;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.StedsadresseNorge;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Ketill Fenne, Visma Consulting AS
 */
@Component
@Slf4j
public class PersonV3Mapper {
	@Inject
	private final PostnummerService postnummerService;
	@Inject
	private final LandkodeService landkodeService;

	private static final Pattern pattern = Pattern.compile("(\\d{4})");

	private static final String LAND_NORGE = "Norge";
	private static final String POSTNUMMER_0000 = "0000";
	private static final String POSTSTED_UKJENT = "UKJENT/UNKNOWN";

	public PersonV3Mapper(PostnummerService postnummerService, LandkodeService landkodeService) {
		this.landkodeService = landkodeService;
		this.postnummerService = postnummerService;
	}

	public void map(Bruker person, Sakspart sakspart) {
		if (person.getPersonnavn().getMellomnavn() == null) {
			sakspart.setNavn(person.getPersonnavn().getFornavn() + " " + person.getPersonnavn().getEtternavn());
		} else {
			sakspart.setNavn(person.getPersonnavn().getFornavn() + " " + person.getPersonnavn()
					.getMellomnavn() + " " + person.getPersonnavn().getEtternavn());
		}
	}

	public void map(Bruker person, Mottaker mottaker, String serviceCode) throws RegOppslagFunctionalException, RegOppslagTechnicalException {
		if (person.getMaalform() != null) {
			if ("NO".equalsIgnoreCase(person.getMaalform().getValue())) {
				mottaker.setSpraakkode(Spraakkode.NB);
			} else {
				mottaker.setSpraakkode(Spraakkode.valueOf(person.getMaalform().getValue()));
			}
		}
		mottaker.setKortNavn(person.getPersonnavn().getSammensattNavn());
		if (person.getPersonnavn().getMellomnavn() == null) {
			mottaker.setNavn(person.getPersonnavn().getFornavn() + " " + person.getPersonnavn().getEtternavn());
		} else {
			mottaker.setNavn(person.getPersonnavn().getFornavn() + " " + person.getPersonnavn()
					.getMellomnavn() + " " + person.getPersonnavn().getEtternavn());
		}

		/*
		 @TODO noen grunn til at man alltid mapper om til NorskPostadresse?
		 Modellen har støtte for utenlandsk adresse også,
		 og da slipper man å populere postnr/poststed med fake verdier og valideringen av brevdata blir mer presis.
		  */
		NorskPostadresse norskPostadresse = new NorskPostadresse();
		if (person.getGjeldendePostadressetype() != null) {
			if ("BOSTEDSADRESSE".equals(person.getGjeldendePostadressetype()
					.getValue()) && person.getBostedsadresse() != null) {
				mapBostedadresse(person, norskPostadresse);
			} else if ("POSTADRESSE".equals(person.getGjeldendePostadressetype().getValue()) && person.getPostadresse()
					.getUstrukturertAdresse() != null) {
				mapPostadresse(person, norskPostadresse);
			} else if ("MIDLERTIDIG_POSTADRESSE_UTLAND".equals(person.getGjeldendePostadressetype()
					.getValue()) && person.getMidlertidigPostadresse() != null) {
				mapMidlertidigUtland(person, norskPostadresse);
			} else if ("MIDLERTIDIG_POSTADRESSE_NORGE".equals(person.getGjeldendePostadressetype()
					.getValue()) && person.getMidlertidigPostadresse() != null) {
				mapMidlertidigNorge(person, norskPostadresse);
			}
		}

		if (StringUtils.isEmpty(norskPostadresse.getPostnummer())) {
			log.info(String.format("%s Mottaker med type=PERSON mangler postnummer. Setter postnummer til \"0000\" og poststed til \"UKJENT/UNKNOWN\". land=%s, poststed=%s, gjeldendePostadresse=%s", serviceCode, norskPostadresse
					.getLand(), norskPostadresse.getPoststed(), person.getGjeldendePostadressetype()));
			norskPostadresse.setPostnummer(POSTNUMMER_0000);
			norskPostadresse.setPoststed(POSTSTED_UKJENT);

			if (norskPostadresse.getLand()==null){
				requestCounter.labels(serviceCode, PERSONV3_MAPPER, UKJENT_POSTNUMMER, getConsumerId(), "UKJENT.UKJENT_LAND").inc();
			} else if (LAND_NORGE.equals(norskPostadresse.getLand())) {
				requestCounter.labels(serviceCode, PERSONV3_MAPPER, UKJENT_POSTNUMMER, getConsumerId(), "UKJENT.NORGE").inc();
			} else {
				requestCounter.labels(serviceCode, PERSONV3_MAPPER, UKJENT_POSTNUMMER, getConsumerId(), "UKJENT.UTLAND").inc();
			}
		}

		if (norskPostadresse.getLand()==null) {
			requestCounter.labels(serviceCode, PERSONV3_MAPPER, UKJENT_LAND, getConsumerId(), UKJENT_LAND).inc();
		}
		requestCounter.labels(serviceCode, PERSONV3_MAPPER, PERSONV3_MAPPER, getConsumerId(), PERSONV3_MAPPER).inc();

		validateAdresse(norskPostadresse, person);

		mottaker.setMottakeradresse(norskPostadresse);
	}

	private void validateAdresse(NorskPostadresse norskPostadresse, Bruker person) throws RegOppslagTechnicalException {

		if (isNorskOgTomPostnummer(norskPostadresse) || isTomPostadresse(norskPostadresse)) {
			throw new RegOppslagTechnicalException(String.format("Ugyldig postadresse. Norsk adresse mangler Postnummer. GjeldenePostadresseType=%s", person.getGjeldendePostadressetype()==null?"Ukjent":person.getGjeldendePostadressetype().getValue()));
		}
	}

	private boolean isTomPostadresse(NorskPostadresse norskPostadresse) {
		return isBlank(norskPostadresse.getAdresselinje1()) && isBlank(norskPostadresse.getLand()) && POSTNUMMER_0000.equals(norskPostadresse.getPostnummer()) && POSTSTED_UKJENT.equals(norskPostadresse.getPoststed());
	}

	private boolean isNorskOgTomPostnummer(NorskPostadresse norskPostadresse) {
		return LAND_NORGE.equals(norskPostadresse.getLand()) && POSTNUMMER_0000.equals(norskPostadresse.getPostnummer());
	}


	private void mapBostedadresse(Bruker person, NorskPostadresse norskPostadresse) {
		if (person.getBostedsadresse().getStrukturertAdresse() instanceof Gateadresse) {
			Gateadresse gateadresse = (Gateadresse) person.getBostedsadresse().getStrukturertAdresse();
			norskPostadresse.setAdresselinje1(Optional.ofNullable(gateadresse.getGatenavn())
					.orElse("") + " " + Optional.ofNullable(gateadresse.getHusnummer() == null ? null : gateadresse.getHusnummer()
					.toString()).orElse("") + Optional.ofNullable(gateadresse.getHusbokstav()).orElse(""));
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
			PostboksadresseNorsk postboksadresseNorsk = (PostboksadresseNorsk) person.getBostedsadresse()
					.getStrukturertAdresse();
			if (postboksadresseNorsk.getPoststed() != null) {
				norskPostadresse.setPostnummer(postboksadresseNorsk.getPoststed().getValue());
				norskPostadresse.setPoststed(postnummerService.finnPoststed(postboksadresseNorsk.getPoststed().getValue()));
			}
		}
		if (person.getBostedsadresse().getStrukturertAdresse().getLandkode() != null) {
			norskPostadresse.setLand(landkodeService.finnLandnavn(person.getBostedsadresse()
					.getStrukturertAdresse()
					.getLandkode()
					.getValue()));
		}
	}

	private void mapPostadresse(Bruker person, NorskPostadresse norskPostadresse) {
		norskPostadresse.setAdresselinje1(person.getPostadresse().getUstrukturertAdresse().getAdresselinje1());
		norskPostadresse.setAdresselinje2(person.getPostadresse().getUstrukturertAdresse().getAdresselinje2());
		norskPostadresse.setAdresselinje3(person.getPostadresse().getUstrukturertAdresse().getAdresselinje3());

		if (person.getPostadresse().getUstrukturertAdresse().getLandkode() != null) {
			norskPostadresse.setLand(landkodeService.finnLandnavn(person.getPostadresse()
					.getUstrukturertAdresse()
					.getLandkode()
					.getValue()));
		}

		String postnummer = getPostnummerFromAdresselinje(person.getPostadresse().getUstrukturertAdresse().getAdresselinje4());
		if (postnummer != null && LAND_NORGE.equals(norskPostadresse.getLand())) {
			norskPostadresse.setPostnummer(postnummer);
			norskPostadresse.setPoststed(postnummerService.finnPoststed(postnummer));
		}

	}

	private String getPostnummerFromAdresselinje(String adresselinje) {
		if (adresselinje == null) {
			return null;
		}

		Matcher matcher = pattern.matcher(adresselinje);
		if (matcher.find()) {
			return matcher.group();
		} else {
			return null;
		}
	}

	private void mapMidlertidigUtland(Bruker person, NorskPostadresse norskPostadresse) {
		MidlertidigPostadresseUtland midlertidigPostadresseUtland = (MidlertidigPostadresseUtland) person.getMidlertidigPostadresse();
		if (midlertidigPostadresseUtland.getUstrukturertAdresse() != null) {
			norskPostadresse.setAdresselinje1(midlertidigPostadresseUtland.getUstrukturertAdresse().getAdresselinje1());
			norskPostadresse.setAdresselinje2(midlertidigPostadresseUtland.getUstrukturertAdresse().getAdresselinje2());
			norskPostadresse.setAdresselinje3(midlertidigPostadresseUtland.getUstrukturertAdresse().getAdresselinje3());

			String postnummer = getPostnummerFromAdresselinje(midlertidigPostadresseUtland.getUstrukturertAdresse()
					.getAdresselinje4());
			if (postnummer != null) {
				norskPostadresse.setPostnummer(postnummer);
			}
		}
		if (midlertidigPostadresseUtland.getUstrukturertAdresse().getLandkode() != null) {
			norskPostadresse.setLand(landkodeService.finnLandnavn(midlertidigPostadresseUtland.getUstrukturertAdresse()
					.getLandkode()
					.getValue()));
		}
	}

	private void mapMidlertidigNorge(Bruker person, NorskPostadresse norskPostadresse) {
		if (((MidlertidigPostadresseNorge) person.getMidlertidigPostadresse()).getStrukturertAdresse() instanceof Gateadresse) {
			Gateadresse gateadresse = (Gateadresse) ((MidlertidigPostadresseNorge) person.getMidlertidigPostadresse()).getStrukturertAdresse();
			norskPostadresse.setAdresselinje1(Optional.ofNullable(gateadresse.getGatenavn())
					.orElse("") + " " + Optional.ofNullable(gateadresse.getHusnummer() == null ? null : gateadresse.getHusnummer()
					.toString()).orElse("") + Optional.ofNullable(gateadresse.getHusbokstav()).orElse(""));
		} else if (((MidlertidigPostadresseNorge) person.getMidlertidigPostadresse()).getStrukturertAdresse() instanceof Matrikkeladresse) {
			Matrikkeladresse matrikkeladresse = (Matrikkeladresse) ((MidlertidigPostadresseNorge) person.getMidlertidigPostadresse())
					.getStrukturertAdresse();
			norskPostadresse.setAdresselinje1(matrikkeladresse.getEiendomsnavn());
		} else if (((MidlertidigPostadresseNorge) person.getMidlertidigPostadresse()).getStrukturertAdresse() instanceof Postboksadresse) {
			Postboksadresse postboksadresse = (Postboksadresse) ((MidlertidigPostadresseNorge) person.getMidlertidigPostadresse())
					.getStrukturertAdresse();
			norskPostadresse.setAdresselinje1(postboksadresse.getPostboksnummer());
		}
		if (((MidlertidigPostadresseNorge) person.getMidlertidigPostadresse()).getStrukturertAdresse() instanceof StedsadresseNorge) {
			StedsadresseNorge stedsadresseNorge = (StedsadresseNorge) ((MidlertidigPostadresseNorge) person.getMidlertidigPostadresse())
					.getStrukturertAdresse();
			if (stedsadresseNorge.getPoststed() != null) {
				norskPostadresse.setPostnummer(stedsadresseNorge.getPoststed().getValue());
				norskPostadresse.setPoststed(postnummerService.finnPoststed(stedsadresseNorge.getPoststed().getValue()));
			}
		} else if (((MidlertidigPostadresseNorge) person.getMidlertidigPostadresse()).getStrukturertAdresse() instanceof PostboksadresseNorsk) {
			PostboksadresseNorsk postboksadresseNorsk = (PostboksadresseNorsk) ((MidlertidigPostadresseNorge) person.getMidlertidigPostadresse())
					.getStrukturertAdresse();
			if (postboksadresseNorsk.getPoststed() != null) {
				norskPostadresse.setPostnummer(postboksadresseNorsk.getPoststed().getValue());
				norskPostadresse.setPoststed(postnummerService.finnPoststed(postboksadresseNorsk.getPoststed().getValue()));
			}
		}
		if (((MidlertidigPostadresseNorge) person.getMidlertidigPostadresse()).getStrukturertAdresse().getLandkode() != null) {
			norskPostadresse.setLand(landkodeService.finnLandnavn(((MidlertidigPostadresseNorge) person.getMidlertidigPostadresse())
					.getStrukturertAdresse()
					.getLandkode()
					.getValue()));
		}
	}

}