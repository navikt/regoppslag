package no.nav.regoppslag.treg002;

import static no.nav.regoppslag.consumer.pdl.pdlresponse.PDLConstant.POSTADRESSE_INNLAND;
import static no.nav.regoppslag.metrics.MetricLabels.ADRESSETYPE;
import static no.nav.regoppslag.metrics.MetricLabels.SERVICE_CODE_TREG002;
import static no.nav.regoppslag.metrics.MetricLabels.TREG002_ADRESSE_MAPPER;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import lombok.extern.slf4j.Slf4j;
import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.navfelles.NorskPostadresse;
import no.nav.dok.brevdata.felles.v1.navfelles.UtenlandskPostadresse;
import no.nav.regoppslag.api.HentMottakerOgAdresseResponse;
import no.nav.regoppslag.consumer.pdl.PdlMottakerInfo;
import no.nav.regoppslag.consumer.pdl.PostadresseTo;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import no.nav.regoppslag.service.LandkodeService;
import org.apache.commons.lang3.StringUtils;
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
	
	public HentMottakerOgAdresseResponse.Adresse mapFraOrg(Mottaker mottaker){

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

	public HentMottakerOgAdresseResponse.Adresse mapFraPdl(PdlMottakerInfo mottaker) {
		if (POSTADRESSE_INNLAND.equals(mottaker.getPostadresse().getAdresseType())) {
			metrics.meter(SERVICE_CODE_TREG002, TREG002_ADRESSE_MAPPER, ADRESSETYPE, "NORSK_ADRESSE");
			PostadresseTo norskPostadresse = mottaker.getPostadresse();
			return HentMottakerOgAdresseResponse.Adresse.builder()
					.adresselinje1(norskPostadresse.getAdresselinje1())
					.adresselinje2(norskPostadresse.getAdresselinje2())
					.adresselinje3(norskPostadresse.getAdresselinje3())
					.postnummer(norskPostadresse.getPostnummer())
					.poststed(norskPostadresse.getPoststed())
					.landkode(isNotBlank(norskPostadresse.getLandkode()) ? norskPostadresse.getLandkode() : null)
					.build();
		} else {
			metrics.meter(SERVICE_CODE_TREG002, TREG002_ADRESSE_MAPPER, ADRESSETYPE, "UTENLANDSK_ADRESSE");
			PostadresseTo utenlandskPostadresse = mottaker.getPostadresse();
			return HentMottakerOgAdresseResponse.Adresse.builder()
					.adresselinje1(isNotBlank(utenlandskPostadresse.getAdresselinje1()) ? utenlandskPostadresse.getAdresselinje1() :null)
					.adresselinje2(isNotBlank(utenlandskPostadresse.getAdresselinje2()) ? utenlandskPostadresse.getAdresselinje2() : null)
					.adresselinje3(isNotBlank(utenlandskPostadresse.getAdresselinje3())  ? utenlandskPostadresse.getAdresselinje3() : null)
					.landkode(utenlandskPostadresse.getLandkode())
					.build();
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
