package no.nav.regoppslag.rreg003;

import lombok.extern.slf4j.Slf4j;
import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.navfelles.NorskPostadresse;
import no.nav.dok.brevdata.felles.v1.navfelles.UtenlandskPostadresse;
import no.nav.regoppslag.consumer.pdl.to.PdlMottakerInfo;
import no.nav.regoppslag.consumer.pdl.to.PostadresseTo;
import no.nav.regoppslag.exceptions.UkjentAdresseException;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import no.nav.regoppslag.service.LandkodeService;
import no.nav.regoppslag.service.LandkodeServiceNorsk;
import no.nav.regoppslag.to.MottakerTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_INNLAND;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_UTLAND;
import static no.nav.regoppslag.metrics.MetricLabels.ADRESSETYPE;
import static no.nav.regoppslag.metrics.MetricLabels.SERVICE_CODE_TREG002;
import static no.nav.regoppslag.metrics.MetricLabels.TREG002_ADRESSE_MAPPER;
import static no.nav.regoppslag.metrics.MetricLabels.UNKNOWN_LANDKODE;
import static no.nav.regoppslag.rreg003.PostadresseType.NORSKPOSTADRESSE;
import static no.nav.regoppslag.rreg003.PostadresseType.UTENLANDSKPOSTADRESSE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Component
@Slf4j
public class AdresseMapper {

	private final LandkodeService landkodeService;
	private final MicrometerMetrics metrics;
	private final LandkodeServiceNorsk landkodeServiceNorsk;

	private static final String LANDKODE_NORGE = "NO";
	private static final String NAVN_NORGE = "NORGE";
	private static final String NORSK_ADRESSE = "NORSK_ADRESSE";
	private static final String UTENLANDSK_ADRESSE = "UTENLANDSK_ADRESSE";

	@Autowired
	public AdresseMapper(LandkodeService landkodeService, MicrometerMetrics metrics, LandkodeServiceNorsk landkodeServiceNorsk) {
		this.landkodeService = landkodeService;
		this.metrics = metrics;
		this.landkodeServiceNorsk = landkodeServiceNorsk;
	}

	public Adresse map(MottakerTo mottakerTo) {
		Mottaker mottaker = mottakerTo.getMottaker();

		if (mottaker.getMottakeradresse() instanceof NorskPostadresse) {
			metrics.meter(SERVICE_CODE_TREG002, TREG002_ADRESSE_MAPPER, ADRESSETYPE, NORSK_ADRESSE);

			NorskPostadresse norskPostadresse = (NorskPostadresse) mottaker.getMottakeradresse();

			return Adresse.builder()
					.adresseKilde(mottakerTo.getAdresseKilde())
					.type(NORSKPOSTADRESSE)
					.adresselinje1(norskPostadresse.getAdresselinje1())
					.adresselinje2(norskPostadresse.getAdresselinje2())
					.adresselinje3(norskPostadresse.getAdresselinje3())
					.land(norskPostadresse.getLand())
					.landkode(getLandkode(norskPostadresse.getLand()))
					.postnummer(norskPostadresse.getPostnummer())
					.poststed(norskPostadresse.getPoststed()).build();
		} else {
			metrics.meter(SERVICE_CODE_TREG002, TREG002_ADRESSE_MAPPER, ADRESSETYPE, UTENLANDSK_ADRESSE);
			UtenlandskPostadresse utenlandskPostadresse = (UtenlandskPostadresse) mottaker.getMottakeradresse();

			return Adresse.builder()
					.adresseKilde(mottakerTo.getAdresseKilde())
					.type(UTENLANDSKPOSTADRESSE)
					.adresselinje1(utenlandskPostadresse.getAdresselinje1())
					.adresselinje2(utenlandskPostadresse.getAdresselinje2())
					.adresselinje3(utenlandskPostadresse.getAdresselinje3())
					.land(landkodeServiceNorsk.finnLand(getLandkode(utenlandskPostadresse.getLand())))
					.landkode(getLandkode(utenlandskPostadresse.getLand())).build();
		}
	}

	public Adresse mapFraPdl(PdlMottakerInfo mottaker) {
		if (POSTADRESSE_INNLAND.equals(mottaker.getPostadresse().getAdresseType())) {
			metrics.meter(SERVICE_CODE_TREG002, TREG002_ADRESSE_MAPPER, ADRESSETYPE, NORSK_ADRESSE);
			PostadresseTo norskPostadresse = mottaker.getPostadresse();
			return Adresse.builder()
					.adresseKilde(norskPostadresse.getAdressekilde())
					.type(NORSKPOSTADRESSE)
					.adresselinje1(norskPostadresse.getAdresselinje1())
					.adresselinje2(norskPostadresse.getAdresselinje2())
					.adresselinje3(norskPostadresse.getAdresselinje3())
					.postnummer(norskPostadresse.getPostnummer())
					.poststed(norskPostadresse.getPoststed())
					.land(isNotBlank(norskPostadresse.getLandkode()) ? landkodeServiceNorsk.finnLand(norskPostadresse.getLandkode()) : NAVN_NORGE)
					.landkode(isNotBlank(norskPostadresse.getLandkode()) ? norskPostadresse.getLandkode() : LANDKODE_NORGE)
					.build();
		} else if (POSTADRESSE_UTLAND.equals(mottaker.getPostadresse().getAdresseType())) {
			metrics.meter(SERVICE_CODE_TREG002, TREG002_ADRESSE_MAPPER, ADRESSETYPE, UTENLANDSK_ADRESSE);
			PostadresseTo utenlandskPostadresse = mottaker.getPostadresse();
			return Adresse.builder()
					.adresseKilde(utenlandskPostadresse.getAdressekilde())
					.type(UTENLANDSKPOSTADRESSE)
					.adresselinje1(isNotBlank(utenlandskPostadresse.getAdresselinje1()) ? utenlandskPostadresse.getAdresselinje1() : null)
					.adresselinje2(isNotBlank(utenlandskPostadresse.getAdresselinje2()) ? utenlandskPostadresse.getAdresselinje2() : null)
					.adresselinje3(isNotBlank(utenlandskPostadresse.getAdresselinje3()) ? utenlandskPostadresse.getAdresselinje3() : null)
					.land(landkodeServiceNorsk.finnLand(utenlandskPostadresse.getLandkode()))
					.landkode(utenlandskPostadresse.getLandkode())
					.build();
		}

		throw new UkjentAdresseException("RREG003: Kunne ikke mappe postadresse for postadresseType", NOT_FOUND);
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
