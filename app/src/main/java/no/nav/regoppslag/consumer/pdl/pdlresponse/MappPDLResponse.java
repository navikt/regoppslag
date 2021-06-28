package no.nav.regoppslag.consumer.pdl.pdlresponse;

import no.nav.regoppslag.api.HentMottakerOgAdresseResponse;
import no.nav.regoppslag.consumer.pdl.PostadresseTo;
import no.nav.regoppslag.exceptions.UkjentAdresseException;
import no.nav.regoppslag.exceptions.UkjentAdressePersonErDoed;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import no.nav.regoppslag.service.LandkodeService;
import no.nav.regoppslag.service.PostnummerService;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.Optional;

import static java.lang.String.format;
import static java.time.LocalDate.now;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static no.nav.regoppslag.consumer.pdl.pdlresponse.PDLConstant.PERSONSTATUS_DOED;
import static no.nav.regoppslag.consumer.pdl.pdlresponse.PDLConstant.POSTADRESSE_INNLAND;
import static no.nav.regoppslag.consumer.pdl.pdlresponse.PDLConstant.POSTADRESSE_UTLAND;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class MappPDLResponse {

    private final PostnummerService postnummerService;
    private final LandkodeService landkodeService;
    private MicrometerMetrics metrics;

    private static final String LANDKODE_NORGE = "NO";
    private static final String ERROR_MELDING = "Feltet %s kan ikke være null eller tomt";
    private static final String ERROR_UTENLANDSKADRESSE = "Feltet %s kan ikke være null eller tomt for utenlandskAdresse";
    private static final String CARE_OF = "c/o";

    @Inject
    public MappPDLResponse(PostnummerService postnummerService, LandkodeService landkodeService,
                           MicrometerMetrics metrics) {
        this.postnummerService = postnummerService;
        this.landkodeService = landkodeService;
        this.metrics = metrics;
    }

    private boolean isDoed(LocalDate doedsdato, String personstatus) {
        return nonNull(doedsdato) && PERSONSTATUS_DOED.equals(personstatus);
    }


    public HentMottakerOgAdresseResponse mapHentPerson(HentPerson hentPerson, String serviceCode) {
        if (nonNull(hentPerson.getKontaktadresse())) {
            return HentMottakerOgAdresseResponse.builder()
                    .navn(hentPerson.getFulltnavn())
                    .adresse(mapKontaktadresse(hentPerson.getKontaktadresse())).build();
        } else if (nonNull(hentPerson.getOppholdsadresse())) {
            return HentMottakerOgAdresseResponse.builder()
                    .navn(hentPerson.getFulltnavn())
                    .adresse(mapOppholdsadresse(hentPerson.getOppholdsadresse(), serviceCode)).build();
        } else if (nonNull(hentPerson.getBostedsadresse())) {
            return HentMottakerOgAdresseResponse.builder()
                    .navn(hentPerson.getFulltnavn())
                    .adresse(mapBostedsadresse(hentPerson.getBostedsadresse(), serviceCode))
                    .build();
        } else if (nonNull(hentPerson.getDoedsfall()) && isDoed(hentPerson.getDoedsfall().getDoedsdato(), hentPerson.getFolkeregisterpersonstatus().getStatus())) {
            return HentMottakerOgAdresseResponse.builder()
                    .navn(hentPerson.getFulltnavn())
                    .adresse(mapKontaktinformasjonForDoedsbo(hentPerson.getKontaktinformasjonForDoedsbo()))
                    .build();
        }
        throw new UkjentAdresseException("Fant ikke adresse for personen i PDL");

    }

    public HentMottakerOgAdresseResponse.Adresse mapBostedsadresse(Bostedsadresse bostedsadresse, String serviceCode) {
        return (harBostedsadresse(bostedsadresse)) ? getRightAddress(bostedsadresse.getVegadresse(), bostedsadresse.getUtenlandskAdresse(),
                bostedsadresse.getMatrikkeladresse(),
                bostedsadresse.getUkjentBosted(), bostedsadresse.getCoAdressenavn(), serviceCode) : null;
    }

    public HentMottakerOgAdresseResponse.Adresse mapOppholdsadresse(Oppholdsadresse oppholdsadresse, String serviceCode) {
        return harOppholdsadresse(oppholdsadresse) ? getRightAddress(oppholdsadresse.getVegadresse(), oppholdsadresse.getUtenlandskAdresse(),
                oppholdsadresse.getMatrikkeladresse(),
                oppholdsadresse.getUkjentBosted(), oppholdsadresse.getCoAdressenavn(), serviceCode) : null;

    }

    public HentMottakerOgAdresseResponse.Adresse mapKontaktadresse(Kontaktadresse kontaktadresse) {
        HentMottakerOgAdresseResponse.Adresse.AdresseBuilder postadresseToBuilder = HentMottakerOgAdresseResponse.Adresse.builder();
        if (POSTADRESSE_INNLAND.equals(kontaktadresse.getType())) {
            if (nonNull(kontaktadresse.getPostboksadresse())) {
                var postboksadresse = kontaktadresse.getPostboksadresse();
                postadresseToBuilder
                        .adresselinje1("Postboks " + requireNonNull(postboksadresse.getPostboks()))
                        .postnummer(postboksadresse.getPostnummer())
                        .poststed(postnummerService.finnPoststed(postboksadresse.getPostnummer()));
            } else if (nonNull(kontaktadresse.getPostadresseIFrittFormat())) {
                Kontaktadresse.PostadresseIFrittFormat postadresse = kontaktadresse.getPostadresseIFrittFormat();
                postadresseToBuilder = isBlank(kontaktadresse.getCoAdressenavn()) ?
                        HentMottakerOgAdresseResponse.Adresse.builder()
                                .adresselinje1(requireNonNull(postadresse.getAdresselinje1(), format(ERROR_MELDING, "adresselinje1")))
                                .adresselinje2(postadresse.getAdresselinje2()).adresselinje3(postadresse.getAdresselinje3())
                                .postnummer(requireNonNull(postadresse.getPostnummer()))
                                .poststed(requireNonNull(postnummerService.finnPoststed(postadresse.getPostnummer()))) :
                        HentMottakerOgAdresseResponse.Adresse.builder()
                                .adresselinje1(kontaktadresse.getCoAdressenavn())
                                .adresselinje2(requireNonNull(postadresse.getAdresselinje1(), format(ERROR_MELDING, "adresselinje2")))
                                .adresselinje3(postadresse.getAdresselinje2())
                                .postnummer(requireNonNull(postadresse.getPostnummer(), format(ERROR_MELDING, "postnummer")))
                                .poststed(postnummerService.finnPoststed(postadresse.getPostnummer()));
            }
        } else if (POSTADRESSE_UTLAND.equals(kontaktadresse.getType())) {
            if (nonNull(kontaktadresse.getUtenlandskAdresse())) {
                UtenlandskAdresse utenlandskAdresse = kontaktadresse.getUtenlandskAdresse();
                mapUtenlandskAdresse(utenlandskAdresse, kontaktadresse.getCoAdressenavn());

            } else if (nonNull(kontaktadresse.getUtenlandskAdresseIFrittFormat())) {
                Kontaktadresse.UtenlandskAdresseIFrittFormat utenlandskAdresse = kontaktadresse.getUtenlandskAdresseIFrittFormat();
                postadresseToBuilder
                        .adresselinje1(requireNonNull(utenlandskAdresse.getAdresselinje1(), format(ERROR_UTENLANDSKADRESSE, "adresselinje1")))
                        .adresselinje2(isBlank(utenlandskAdresse.getAdresselinje2()) ? null : utenlandskAdresse.getAdresselinje2())
                        .adresselinje3(utenlandskAdresse.getAdresselinje3())
                        .postnummer(requireNonNull(utenlandskAdresse.getPostkode(), format(ERROR_UTENLANDSKADRESSE, "postnummer")))
                        .poststed(requireNonNull(utenlandskAdresse.getByEllerStedsnavn(), format(ERROR_UTENLANDSKADRESSE, "poststed")))
                        .landkode(landkodeService.finnLandnavn(utenlandskAdresse.getLandkode()));

            }
        }
        return postadresseToBuilder.build();
    }

    public HentMottakerOgAdresseResponse.Adresse mapKontaktinformasjonForDoedsbo(KontaktinformasjonForDoedsbo kontaktinformasjon) {
        if (now().isBefore(kontaktinformasjon.getAttestutstedelsesdato()) && (nonNull(kontaktinformasjon.getOrganisasjonSomKontakt()) ||
                nonNull(kontaktinformasjon.getAdvokatSomKontakt()) || nonNull(kontaktinformasjon.getPersonSomKontakt()))) {
            return Optional.of(mapAndValidateKontaktinformasjonForDoeds(kontaktinformasjon)).orElseThrow(
                    () -> new UkjentAdressePersonErDoed("Mottaker er registrert som død og har ugyldig postadresse"));

        }
        throw new UkjentAdressePersonErDoed("Mottaker er registrert som død og har ugyldig postadresse");

    }

    public HentMottakerOgAdresseResponse.Adresse mapAndValidateKontaktinformasjonForDoeds(KontaktinformasjonForDoedsbo kontaktinformasjonForDoedsbo) {
        KontaktinformasjonForDoedsbo.KontaktAdresse kontaktAdresse = nonNull(kontaktinformasjonForDoedsbo.getAdresse()) ? kontaktinformasjonForDoedsbo.getAdresse() : null;
        if (nonNull(kontaktinformasjonForDoedsbo.getAdvokatSomKontakt())) {
            KontaktinformasjonForDoedsbo.AdvokatSomKontakt advokatSomKontakt = kontaktinformasjonForDoedsbo.getAdvokatSomKontakt();
            HentMottakerOgAdresseResponse.Adresse postadresse = mapMidlertidigPostboksadresse(kontaktinformasjonForDoedsbo);
            postadresse.setAdresselinje1(CARE_OF + kontaktinformasjonForDoedsbo.getFulltnavn(advokatSomKontakt.getPersonnavn()));
            return postadresse;
        } else if (nonNull(kontaktinformasjonForDoedsbo.getPersonSomKontakt())) {
            KontaktinformasjonForDoedsbo.PersonSomKontakt personSomKontakt = kontaktinformasjonForDoedsbo.getPersonSomKontakt();
            HentMottakerOgAdresseResponse.Adresse postadresse = mapMidlertidigPostboksadresse(kontaktinformasjonForDoedsbo);
            postadresse.setAdresselinje1(CARE_OF + kontaktinformasjonForDoedsbo.getFulltnavn(personSomKontakt.getPersonnavn()));
            return postadresse;
        } else if (nonNull(kontaktinformasjonForDoedsbo.getOrganisasjonSomKontakt())) {
            KontaktinformasjonForDoedsbo.OrganisasjonSomKontakt organisasjonSomKontakt = kontaktinformasjonForDoedsbo.getOrganisasjonSomKontakt();
            return HentMottakerOgAdresseResponse.Adresse.builder()
                    .adresselinje1(CARE_OF + kontaktinformasjonForDoedsbo.getFulltnavn(organisasjonSomKontakt.getKontaktperson()))
                    .adresselinje2(requireNonNull(kontaktAdresse.getAdresselinje1(), format(ERROR_MELDING, "adresselinje1")))
                    .adresselinje3(isBlank(kontaktAdresse.getAdresselinje2()) ? null : kontaktAdresse.getAdresselinje2())
                    .postnummer(requireNonNull(kontaktAdresse.getPostnummer(), format(ERROR_MELDING, "postnummer")))
                    .poststed(requireNonNull(kontaktAdresse.getPoststedsnavn(), format(ERROR_MELDING, "poststed")))
                    .landkode(kontaktAdresse.getLandkode())
                    .build();

        }
        return null;
    }

    private boolean harBostedsadresse(Bostedsadresse bostedsadresse) {
        return (nonNull(bostedsadresse.getMatrikkeladresse()) || nonNull(bostedsadresse.getVegadresse())
                || nonNull(bostedsadresse.getUtenlandskAdresse()) || nonNull(bostedsadresse.getUkjentBosted()));
    }

    private boolean harOppholdsadresse(Oppholdsadresse oppholdsadresse) {
        return (nonNull(oppholdsadresse.getMatrikkeladresse()) || nonNull(oppholdsadresse.getVegadresse())
                || nonNull(oppholdsadresse.getUtenlandskAdresse()) || nonNull(oppholdsadresse.getUkjentBosted()));
    }

    private HentMottakerOgAdresseResponse.Adresse mapMidlertidigPostboksadresse(KontaktinformasjonForDoedsbo kontaktinformasjonForDoedsbo) {
        return nonNull(kontaktinformasjonForDoedsbo.getAdresse()) ?
                HentMottakerOgAdresseResponse.Adresse.builder()
                        .adresselinje2(kontaktinformasjonForDoedsbo.getAdresse().getAdresselinje1())
                        .adresselinje3(kontaktinformasjonForDoedsbo.getAdresse().getAdresselinje2())
                        .postnummer(requireNonNull(kontaktinformasjonForDoedsbo.getAdresse().getPostnummer(), format(ERROR_MELDING, "postnummer")))
                        .poststed(isBlank(kontaktinformasjonForDoedsbo.getAdresse().getPoststedsnavn()) ? postnummerService.finnPoststed(kontaktinformasjonForDoedsbo.getAdresse().getPostnummer())
                                : kontaktinformasjonForDoedsbo.getAdresse().getPoststedsnavn())
                        .landkode(landkodeService.finnLandnavn(kontaktinformasjonForDoedsbo.getAdresse().getLandkode()))
                        .build() : null;
    }

    private HentMottakerOgAdresseResponse.Adresse getRightAddress(Vegadresse vegadresse, UtenlandskAdresse utenlandskAdresse,
                                                                  Matrikkeladresse matrikkeladresse, UkjentBosted ukjentBosted,
                                                                  String coAdressenavn, String serviceCode) {
        PostadresseTo postadresseToBuilder = PostadresseTo.builder().build();
        if (nonNull(vegadresse)) {
            return mapVegadresse(vegadresse, coAdressenavn);
        } else if (nonNull(utenlandskAdresse)) {
            return mapUtenlandskAdresse(utenlandskAdresse, coAdressenavn);
        } else if (nonNull(matrikkeladresse)) {
            return mapMatrikkeladresse(matrikkeladresse);
        } else if (nonNull(ukjentBosted)) {
            throw new UkjentAdresseException(serviceCode + "Kunne ikke mappe postadresse for UkjentBosted mottaker");
        }
        return null;
    }

    private HentMottakerOgAdresseResponse.Adresse mapVegadresse(Vegadresse vegadresse, String coAdressenavn) {
        return isBlank(coAdressenavn) ?
                HentMottakerOgAdresseResponse.Adresse.builder().adresselinje1(Optional.ofNullable(vegadresse.getAdressenavn())
                        .orElse("") + " " + Optional.ofNullable(isNull(vegadresse.getHusnummer()) ? null : vegadresse.getHusnummer())
                        .orElse("") + Optional.ofNullable(vegadresse.getHusbokstav()).orElse(""))
                        .postnummer(requireNonNull(vegadresse.getPostnummer(), format(ERROR_MELDING, "postnummer")))
                        .poststed(requireNonNull(postnummerService.finnPoststed(vegadresse.getPostnummer()), format(ERROR_MELDING, "poststed")))
                        .landkode(LANDKODE_NORGE)
                        .build() :
                HentMottakerOgAdresseResponse.Adresse.builder()
                        .adresselinje1(coAdressenavn)
                        .adresselinje2(Optional.ofNullable(vegadresse.getAdressenavn())
                                .orElse("") + " " + Optional.ofNullable(isNull(vegadresse.getHusnummer()) ? null : vegadresse.getHusnummer())
                                .orElse("") + Optional.ofNullable(vegadresse.getHusbokstav()).orElse(""))
                        .postnummer(requireNonNull(vegadresse.getPostnummer(), format(ERROR_MELDING, "postnummer")))
                        .poststed(requireNonNull(postnummerService.finnPoststed(vegadresse.getPostnummer()), format(ERROR_MELDING, "poststed")))
                        .landkode(LANDKODE_NORGE)
                        .build();

    }

    private HentMottakerOgAdresseResponse.Adresse mapUtenlandskAdresse(UtenlandskAdresse utenlandskAdresse, String coAdressenav) {
        return isBlank(coAdressenav) ? HentMottakerOgAdresseResponse.Adresse.builder()
                .adresselinje1(requireNonNull(utenlandskAdresse.getAdressenavnNummer(), format(ERROR_UTENLANDSKADRESSE, "adresselinje1")))
                .postnummer(requireNonNull(utenlandskAdresse.getPostkode(), format(ERROR_UTENLANDSKADRESSE, "postnummer")))
                .poststed(requireNonNull(utenlandskAdresse.getBySted(), format(ERROR_UTENLANDSKADRESSE, "poststed")))
                .landkode(utenlandskAdresse.getLandkode())
                .build() :
                HentMottakerOgAdresseResponse.Adresse.builder()
                        .adresselinje1(coAdressenav)
                        .adresselinje2(requireNonNull(utenlandskAdresse.getAdressenavnNummer(), format(ERROR_UTENLANDSKADRESSE, "adresselinje1")))
                        .postnummer(requireNonNull(utenlandskAdresse.getPostkode(), format(ERROR_UTENLANDSKADRESSE, "postnummer")))
                        .poststed(requireNonNull(utenlandskAdresse.getBySted(), format(ERROR_UTENLANDSKADRESSE, "poststed")))
                        .landkode(utenlandskAdresse.getLandkode())
                        .build();
    }

    private HentMottakerOgAdresseResponse.Adresse mapMatrikkeladresse(Matrikkeladresse matrikkeladresse) {
        return HentMottakerOgAdresseResponse.Adresse.builder()
                .adresselinje1(matrikkeladresse.getTilleggsnavn())
                .postnummer(matrikkeladresse.getPostnummer())
                .poststed(postnummerService.finnPoststed(matrikkeladresse.getPostnummer()))
                .build();
    }
}