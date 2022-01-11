package no.nav.regoppslag.consumer.map;

import no.nav.dok.brevdata.felles.v1.navfelles.NorskPostadresse;
import no.nav.dok.brevdata.felles.v1.navfelles.UtenlandskPostadresse;
import no.nav.regoppslag.exceptions.FeilVedMappingAvPostadresseException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class PostadresseMapper {

    private static final String INGEN_TOM_ADRESSELINJE = "Kan ikke mappe poststed til hverken adresselinje2 eller adresselinje3 ettersom de har innehold.";

    private PostadresseMapper(){}

    public static NorskPostadresse mapPostadresseToNorskpostadresse(Postadresse postadresse) {

        NorskPostadresse norskPostadresse = new NorskPostadresse();
        norskPostadresse.setLand(postadresse.getLand());
        norskPostadresse.setAdresselinje1(postadresse.getAdresselinje1());
        norskPostadresse.setAdresselinje2(postadresse.getAdresselinje2());
        norskPostadresse.setAdresselinje3(postadresse.getAdresselinje3());
        norskPostadresse.setPostnummer(postadresse.getPostnummer());
        norskPostadresse.setPoststed(postadresse.getPoststed());

        return norskPostadresse;
    }

    public static UtenlandskPostadresse mapPostadresseToUtenlandskadresse(Postadresse postadresse) {
        UtenlandskPostadresse utenlandskPostadresse = new UtenlandskPostadresse();
        utenlandskPostadresse.setLand(postadresse.getLand());
        utenlandskPostadresse.setAdresselinje1(postadresse.getAdresselinje1());
        utenlandskPostadresse.setAdresselinje2(postadresse.getAdresselinje2());
        utenlandskPostadresse.setAdresselinje3(postadresse.getAdresselinje3());

        if (postadresse.getPoststed() != null) {
            if(utenlandskPostadresse.getAdresselinje2() == null) {
                utenlandskPostadresse.setAdresselinje2(postadresse.getPoststed());
            }
            else if(utenlandskPostadresse.getAdresselinje3() == null) {
                utenlandskPostadresse.setAdresselinje3(postadresse.getPoststed());
            }
            else {
                throw new FeilVedMappingAvPostadresseException(INGEN_TOM_ADRESSELINJE, BAD_REQUEST);
            }
        }
        return utenlandskPostadresse;
    }
}
