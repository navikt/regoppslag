package no.nav.regoppslag.treg002;

import static no.nav.regoppslag.metrics.PrometheusLabels.ADRESSETYPE;
import static no.nav.regoppslag.metrics.PrometheusLabels.SERVICE_CODE_TREG002;
import static no.nav.regoppslag.metrics.PrometheusLabels.TREG002_ADRESSE_MAPPER;
import static no.nav.regoppslag.metrics.PrometheusMetrics.getConsumerId;
import static no.nav.regoppslag.metrics.PrometheusMetrics.requestCounter;

import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.navfelles.NorskPostadresse;
import no.nav.dok.brevdata.felles.v1.navfelles.UtenlandskPostadresse;
import no.nav.regoppslag.common.Adresse;
import no.nav.regoppslag.service.LandkodeService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
public class AdresseMapper {
	
	@Inject
	private LandkodeService landkodeService;
	
	public Adresse map(Mottaker mottaker){
		
		
		if (mottaker.getMottakeradresse() instanceof NorskPostadresse){
			requestCounter.labels(SERVICE_CODE_TREG002, TREG002_ADRESSE_MAPPER, ADRESSETYPE, getConsumerId(), "NORSK_ADRESSE").inc();
			NorskPostadresse norskPostadresse = (NorskPostadresse) mottaker.getMottakeradresse();
			return Adresse.builder()
				.adresselinje1(norskPostadresse.getAdresselinje1())
				.adresselinje2(norskPostadresse.getAdresselinje2())
				.adresselinje3(norskPostadresse.getAdresselinje3())
				.landkode(landkodeService.finnLandkode(norskPostadresse.getLand()))
				.postnummer(norskPostadresse.getPostnummer())
				.poststed(norskPostadresse.getPoststed()).build();
		} else {
			requestCounter.labels(SERVICE_CODE_TREG002, TREG002_ADRESSE_MAPPER, ADRESSETYPE, getConsumerId(), "UTENLANDSK_ADRESSE").inc();
			UtenlandskPostadresse utenlandskPostadresse = (UtenlandskPostadresse) mottaker.getMottakeradresse();
			return Adresse.builder()
					.adresselinje1(utenlandskPostadresse.getAdresselinje1())
					.adresselinje2(utenlandskPostadresse.getAdresselinje2())
					.adresselinje3(utenlandskPostadresse.getAdresselinje3())
					.landkode(landkodeService.finnLandkode(utenlandskPostadresse.getLand())).build();
		}
	
	
	}
}
