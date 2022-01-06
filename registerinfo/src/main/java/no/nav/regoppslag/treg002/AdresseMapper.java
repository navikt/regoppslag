package no.nav.regoppslag.treg002;

import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_INNLAND;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_UTLAND;
import static no.nav.regoppslag.metrics.MetricLabels.ADRESSETYPE;
import static no.nav.regoppslag.metrics.MetricLabels.SERVICE_CODE_TREG002;
import static no.nav.regoppslag.metrics.MetricLabels.TREG002_ADRESSE_MAPPER;
import static no.nav.regoppslag.metrics.MetricLabels.UNKNOWN_LANDKODE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.extern.slf4j.Slf4j;
import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.navfelles.NorskPostadresse;
import no.nav.dok.brevdata.felles.v1.navfelles.UtenlandskPostadresse;
import no.nav.regoppslag.api.HentMottakerOgAdresseResponse;
import no.nav.regoppslag.consumer.pdl.to.PdlMottakerInfo;
import no.nav.regoppslag.consumer.pdl.to.PostadresseTo;
import no.nav.regoppslag.exceptions.UkjentAdresseException;
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

	private final LandkodeService landkodeService;
	private final MicrometerMetrics metrics;

	private static final String LANDKODE_NORGE = "NO";
	private static final String NORSK_ADRESSE = "NORSK_ADRESSE";
	private static final String UTENLANDSK_ADRESSE = "UTENLANDSK_ADRESSE";

	@Inject
	public AdresseMapper(LandkodeService landkodeService, MicrometerMetrics metrics) {
		this.landkodeService = landkodeService;
		this.metrics = metrics;
	}

	public HentMottakerOgAdresseResponse.Adresse map(Mottaker mottaker){

		if (mottaker.getMottakeradresse() instanceof NorskPostadresse){
			metrics.meter(MetricLabels.SERVICE_CODE_TREG002, MetricLabels.TREG002_ADRESSE_MAPPER, MetricLabels.ADRESSETYPE, NORSK_ADRESSE);

			NorskPostadresse norskPostadresse = (NorskPostadresse) mottaker.getMottakeradresse();
			return HentMottakerOgAdresseResponse.Adresse.builder()
				.adresselinje1(norskPostadresse.getAdresselinje1())
				.adresselinje2(norskPostadresse.getAdresselinje2())
				.adresselinje3(norskPostadresse.getAdresselinje3())
					.landkode(getLandkode(norskPostadresse.getLand()))
				.postnummer(norskPostadresse.getPostnummer())
				.poststed(norskPostadresse.getPoststed()).build();
		} else {
			metrics.meter(MetricLabels.SERVICE_CODE_TREG002, MetricLabels.TREG002_ADRESSE_MAPPER, MetricLabels.ADRESSETYPE, UTENLANDSK_ADRESSE);
			UtenlandskPostadresse utenlandskPostadresse = (UtenlandskPostadresse) mottaker.getMottakeradresse();
			return HentMottakerOgAdresseResponse.Adresse.builder()
					.adresselinje1(utenlandskPostadresse.getAdresselinje1())
					.adresselinje2(utenlandskPostadresse.getAdresselinje2())
					.adresselinje3(utenlandskPostadresse.getAdresselinje3())
					.landkode(getLandkode(utenlandskPostadresse.getLand())).build();
		}
	}

	public HentMottakerOgAdresseResponse.Adresse mapFraPdl(PdlMottakerInfo mottaker) {
		if (PDLConstant.POSTADRESSE_INNLAND.equals(mottaker.getPostadresse().getAdresseType())) {
			metrics.meter(MetricLabels.SERVICE_CODE_TREG002, MetricLabels.TREG002_ADRESSE_MAPPER, MetricLabels.ADRESSETYPE, NORSK_ADRESSE);
			PostadresseTo norskPostadresse = mottaker.getPostadresse();
			return HentMottakerOgAdresseResponse.Adresse.builder()
					.adresselinje1(norskPostadresse.getAdresselinje1())
					.adresselinje2(norskPostadresse.getAdresselinje2())
					.adresselinje3(norskPostadresse.getAdresselinje3())
					.postnummer(norskPostadresse.getPostnummer())
					.poststed(norskPostadresse.getPoststed())
					.landkode(StringUtils.isNotBlank(norskPostadresse.getLandkode()) ? norskPostadresse.getLandkode() : LANDKODE_NORGE)
					.build();
		} else if(PDLConstant.POSTADRESSE_UTLAND.equals(mottaker.getPostadresse().getAdresseType())){
			metrics.meter(MetricLabels.SERVICE_CODE_TREG002, MetricLabels.TREG002_ADRESSE_MAPPER, MetricLabels.ADRESSETYPE, UTENLANDSK_ADRESSE);
			PostadresseTo utenlandskPostadresse = mottaker.getPostadresse();
			return HentMottakerOgAdresseResponse.Adresse.builder()
					.adresselinje1(StringUtils.isNotBlank(utenlandskPostadresse.getAdresselinje1()) ? utenlandskPostadresse.getAdresselinje1() :null)
					.adresselinje2(StringUtils.isNotBlank(utenlandskPostadresse.getAdresselinje2()) ? utenlandskPostadresse.getAdresselinje2() : null)
					.adresselinje3(StringUtils.isNotBlank(utenlandskPostadresse.getAdresselinje3())  ? utenlandskPostadresse.getAdresselinje3() : null)
					.landkode(utenlandskPostadresse.getLandkode())
					.build();
		}

		throw new UkjentAdresseException("TREG002: Kunne ikke mappe postadresse for postadresseType", HttpStatus.NOT_FOUND);
	}

	private String getLandkode(String land) {
		String landkode = landkodeService.finnLandkode(land);
		if (landkode == null) {
			metrics.meter(MetricLabels.SERVICE_CODE_TREG002, MetricLabels.TREG002_ADRESSE_MAPPER, "LANDKODE", "UKJENT");
			log.info(String.format("TREG002 Mottaker har ingen landkode registert. Setter landkode til \"%s\"", MetricLabels.UNKNOWN_LANDKODE));
			return MetricLabels.UNKNOWN_LANDKODE;
		}
		return landkode;
	}
}
