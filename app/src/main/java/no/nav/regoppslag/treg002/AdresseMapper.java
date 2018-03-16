package no.nav.regoppslag.treg002;

import no.nav.dok.metaforcemal.jaxb2.gen.Mottaker;
import no.nav.dok.metaforcemal.jaxb2.gen.NorskPostadresse;
import no.nav.dok.metaforcemal.jaxb2.gen.UtenlandskPostadresse;
import no.nav.regoppslag.common.Adresse;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class AdresseMapper {
	
	
	
	public static Adresse map(Mottaker mottaker){
		
		
		if (mottaker.getAdresse() instanceof NorskPostadresse){
			NorskPostadresse norskPostadresse = (NorskPostadresse) mottaker.getAdresse();
			return Adresse.builder()
				.adresselinje1(norskPostadresse.getAdresselinje1())
				.adresselinje2(norskPostadresse.getAdresselinje2())
				.adresselinje3(norskPostadresse.getAdresselinje3())
				.landkode(norskPostadresse.getLand())
				.postnummer(norskPostadresse.getPostnummer())
				.poststed(norskPostadresse.getPoststed()).build();
		} else {
			UtenlandskPostadresse utenlandskPostadresse = (UtenlandskPostadresse) mottaker.getAdresse();
			return Adresse.builder()
					.adresselinje1(utenlandskPostadresse.getAdresselinje1())
					.adresselinje2(utenlandskPostadresse.getAdresselinje2())
					.adresselinje3(utenlandskPostadresse.getAdresselinje3())
					.landkode(utenlandskPostadresse.getLand()).build();
		}
	
	
	}
}
