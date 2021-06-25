package no.nav.regoppslag.consumer.pdl.pdlresponse;

import lombok.SneakyThrows;
import no.nav.regoppslag.consumer.pdl.MottakerPostInfo;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;

import java.time.LocalDate;

import static java.time.LocalDate.now;
import static java.util.Objects.nonNull;
import static no.nav.regoppslag.consumer.pdl.pdlresponse.PDLConstant.PERSONSTATUS_DOED;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class MappPDLResponse {


    private boolean isDoed(LocalDate doedsdato, String personstatus) {
        return nonNull(doedsdato) && PERSONSTATUS_DOED.equals(personstatus);
    }

    public HentPerson hentPerson(PDLHentPersonResponse hentPersonResponse) {
        PDLHentPersonResponse.PDLHentPerson pdlHentPerson = nonNull(hentPersonResponse) ? hentPersonResponse.getData() : null;
        return nonNull(pdlHentPerson) ? pdlHentPerson.getHentPerson() : null;
    }

    public MottakerPostInfo mapHentPerson(HentPerson hentPerson) {
        return null;
    }


    @SneakyThrows
    public MottakerPostInfo mapKontaktinformasjonForDoedsbo(KontaktinformasjonForDoedsbo kontaktinformasjon) {
        MottakerPostInfo.MottakerPostInfoBuilder mottakerPostInfo = MottakerPostInfo.builder();
        if (now().isBefore(kontaktinformasjon.getAttestutstedelsesdato()) && (nonNull(kontaktinformasjon.getOrganisasjonSomKontakt()) ||
                nonNull(kontaktinformasjon.getAdvokatSomKontakt()) || nonNull(kontaktinformasjon.getPersonSomKontakt())))
        {
            return mapAndValidateKontaktinformasjonForDoeds(kontaktinformasjon);

        } else {
            throw new RegOppslagFunctionalException("");
        }
    }


    public MottakerPostInfo mapAndValidateKontaktinformasjonForDoeds(KontaktinformasjonForDoedsbo kontaktinformasjonForDoedsbo) {
        MottakerPostInfo.MottakerPostInfoBuilder mottakerPostInfo = MottakerPostInfo.builder();
        KontaktinformasjonForDoedsbo.KontaktAdresse kontaktAdresse = nonNull(kontaktinformasjonForDoedsbo.getAdresse()) ? kontaktinformasjonForDoedsbo.getAdresse() : null;
        if (nonNull(kontaktinformasjonForDoedsbo.getAdvokatSomKontakt())) {
            KontaktinformasjonForDoedsbo.AdvokatSomKontakt advokatSomKontakt = kontaktinformasjonForDoedsbo.getAdvokatSomKontakt();
            mottakerPostInfo.mottakerNavn(kontaktinformasjonForDoedsbo.getFulltnavn(advokatSomKontakt.getPersonnavn()))
                    .postadresse(MottakerPostInfo.NorskPostadresse.builder()
                            .adresselinje1(isBlank(kontaktAdresse.getAdresselinje1()) ? null : kontaktAdresse.getAdresselinje1())
                            .adresselinje2(isBlank(kontaktAdresse.getAdresselinje2()) ? null : kontaktAdresse.getAdresselinje2())
                            .poststed(kontaktAdresse.getPoststedsnavn())
                            .postnummer(kontaktAdresse.getPostnummer())
                            .land(kontaktAdresse.getLandkode())
                            .build());
        } else if (nonNull(kontaktinformasjonForDoedsbo.getPersonSomKontakt())) {
            KontaktinformasjonForDoedsbo.PersonSomKontakt personSomKontakt = kontaktinformasjonForDoedsbo.getPersonSomKontakt();
            mottakerPostInfo.mottakerNavn(kontaktinformasjonForDoedsbo.getFulltnavn(personSomKontakt.getPersonnavn()))
                    .postadresse(MottakerPostInfo.NorskPostadresse.builder()
                            .adresselinje1(isBlank(kontaktAdresse.getAdresselinje1()) ? null : kontaktAdresse.getAdresselinje1())
                            .adresselinje2(isBlank(kontaktAdresse.getAdresselinje2()) ? null : kontaktAdresse.getAdresselinje2())
                            .poststed(kontaktAdresse.getPoststedsnavn())
                            .postnummer(kontaktAdresse.getPostnummer())
                            .land(kontaktAdresse.getLandkode())
                            .build());
        } else if (nonNull(kontaktinformasjonForDoedsbo.getOrganisasjonSomKontakt())) {
            KontaktinformasjonForDoedsbo.OrganisasjonSomKontakt organisasjonSomKontakt = kontaktinformasjonForDoedsbo.getOrganisasjonSomKontakt();
            mottakerPostInfo.mottakerNavn(kontaktinformasjonForDoedsbo.getFulltnavn(organisasjonSomKontakt.getKontaktperson()))
                    .postadresse(MottakerPostInfo.NorskPostadresse.builder()
                            .adresselinje1(isBlank(kontaktAdresse.getAdresselinje1()) ? null : kontaktAdresse.getAdresselinje1())
                            .adresselinje2(isBlank(kontaktAdresse.getAdresselinje2()) ? null : kontaktAdresse.getAdresselinje2())
                            .poststed(kontaktAdresse.getPoststedsnavn())
                            .postnummer(kontaktAdresse.getPostnummer())
                            .land(kontaktAdresse.getLandkode())
                            .build());

        }
        return mottakerPostInfo.build();
    }

}
