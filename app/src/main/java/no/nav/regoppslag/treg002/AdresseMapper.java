package no.nav.regoppslag.treg002;

import static no.nav.regoppslag.metrics.MetricLabels.ADRESSETYPE;
import static no.nav.regoppslag.metrics.MetricLabels.SERVICE_CODE_TREG002;
import static no.nav.regoppslag.metrics.MetricLabels.TREG002_ADRESSE_MAPPER;

import lombok.extern.slf4j.Slf4j;
import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.navfelles.NorskPostadresse;
import no.nav.dok.brevdata.felles.v1.navfelles.UtenlandskPostadresse;
import no.nav.regoppslag.api.HentMottakerOgAdresseResponse;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import no.nav.regoppslag.service.LandkodeService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
@Slf4j
public class AdresseMapper {
	
	@Inject
	private LandkodeService landkodeService;

	@Inject
	private MicrometerMetrics metrics;

	private static final String UNKNOWN_LANDKODE = "???";
	
	public HentMottakerOgAdresseResponse.Adresse map(Mottaker mottaker){

		if (mottaker.getMottakeradresse() instanceof NorskPostadresse){
			metrics.meter(SERVICE_CODE_TREG002, TREG002_ADRESSE_MAPPER, ADRESSETYPE, "NORSK_ADRESSE");

			NorskPostadresse norskPostadresse = (NorskPostadresse) mottaker.getMottakeradresse();
			return HentMottakerOgAdresseResponse.Adresse.builder()
				.adresselinje1(norskPostadresse.getAdresselinje1())
				.adresselinje2(norskPostadresse.getAdresselinje2())
				.adresselinje3(norskPostadresse.getAdresselinje3())
					.landkode(getLandkode(norskPostadresse.getLand()))
				.postnummer(norskPostadresse.getPostnummer())
				.poststed(norskPostadresse.getPoststed()).build();
		} else {
			metrics.meter(SERVICE_CODE_TREG002, TREG002_ADRESSE_MAPPER, ADRESSETYPE, "UTENLANDSK_ADRESSE");
			UtenlandskPostadresse utenlandskPostadresse = (UtenlandskPostadresse) mottaker.getMottakeradresse();
			return HentMottakerOgAdresseResponse.Adresse.builder()
					.adresselinje1(utenlandskPostadresse.getAdresselinje1())
					.adresselinje2(utenlandskPostadresse.getAdresselinje2())
					.adresselinje3(utenlandskPostadresse.getAdresselinje3())
					.landkode(getLandkode(utenlandskPostadresse.getLand())).build();
		}
	}

	private String getLandkode(String land) {
		String landkode = landkodeService.finnLandkode(land);
		if (landkode == null) {
			metrics.meter(SERVICE_CODE_TREG002, TREG002_ADRESSE_MAPPER, "LANDKODE", "UKJENT");
			log.info(String.format("TREG002 Mottaker har ingen landkode registert. Setter landkode til \"%s\"", UNKNOWN_LANDKODE));
			return UNKNOWN_LANDKODE;
		}
		return landkode;
	}
}
