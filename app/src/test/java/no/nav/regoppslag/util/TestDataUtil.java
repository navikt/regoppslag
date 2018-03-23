package no.nav.regoppslag.util;

import no.nav.dok.metaforcemal.jaxb2.gen.Mottaker;
import no.nav.dok.metaforcemal.jaxb2.gen.NorskPostadresse;
import no.nav.dok.metaforcemal.jaxb2.gen.UtenlandskPostadresse;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class TestDataUtil {
	
	
	public static String ADRESSELINJE1="linje1";
	public static String ADRESSELINJE2="linje2";
	public static String ADRESSELINJE3="linje3";
	public static String LANDKODE="NO";
	public static String POSTNUMMER="3000";
	public static String POSTSTED="HER";
	
	public static Mottaker createMottaker( ) {
		return createMottaker(true);
	}
	
	public static Mottaker createMottaker(boolean withNorskPostedAdresse){
		
		Mottaker mottaker = new Mottaker();
		if(!withNorskPostedAdresse) {
			mottaker.setAdresse(createUtenlandsPostadresse());
			
		} else {
			mottaker.setAdresse(createNorskPostadresse());
			
		}
		
		return mottaker;
	}
	
	public static NorskPostadresse createNorskPostadresse(){
		NorskPostadresse norskPostadresse = new NorskPostadresse();
		norskPostadresse.setAdresselinje1(ADRESSELINJE1);
		norskPostadresse.setAdresselinje2(ADRESSELINJE2);
		norskPostadresse.setAdresselinje3(ADRESSELINJE3);
		norskPostadresse.setLand(LANDKODE);
		norskPostadresse.setPostnummer(POSTNUMMER);
		norskPostadresse.setPoststed(POSTSTED);
		return norskPostadresse;
	}
	
	public static UtenlandskPostadresse createUtenlandsPostadresse(){
		UtenlandskPostadresse utenlandskPostadresse = new UtenlandskPostadresse();
		utenlandskPostadresse.setAdresselinje1(ADRESSELINJE1);
		utenlandskPostadresse.setAdresselinje2(ADRESSELINJE2);
		utenlandskPostadresse.setAdresselinje3(ADRESSELINJE3);
		utenlandskPostadresse.setLand(LANDKODE);
		return utenlandskPostadresse;
	}
	
	
}
