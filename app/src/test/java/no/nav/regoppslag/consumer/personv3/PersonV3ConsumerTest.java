package no.nav.regoppslag.consumer.personv3;

import io.micrometer.core.instrument.MeterRegistry;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import no.nav.tjeneste.virksomhet.person.v3.feil.PersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.feil.Sikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Personnavn;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PersonV3Consumer.class, PersonV3ConsumerTest.Config.class})
public class PersonV3ConsumerTest {

    private static final String FNR = "99999999999";
    private static final String FORNAVN = "TOM";
    private static final String MELLOMNAVN = "MARVOLO";
    private static final String ETTERNAVN = "RIDDLE";
    private static final String PRINCIPAL = "RIDDLE";

    @Inject
    private PersonV3Consumer personV3Consumer;

    @Inject
    private PersonV3 personV3;

    @Mock
    private MeterRegistry registry;

    @BeforeEach
    public void setUp() {
        reset(personV3);
    }

    @Test
    public void shouldHentPersonnavn() throws Exception {
        when(personV3.hentPerson(any(HentPersonRequest.class))).thenReturn(defaultResponse());

        Bruker person = personV3Consumer.hentPerson(FNR, "");

        assertThat(person.getPersonnavn().getSammensattNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
    }

    @Test
    public void shouldHentPersonNavnWhenMissingMellomnavn() throws Exception {
        when(personV3.hentPerson(any(HentPersonRequest.class))).thenReturn(createResponse(FORNAVN, null, ETTERNAVN));

        Bruker person = personV3Consumer.hentPerson(FNR, "");

        assertThat(person.getPersonnavn().getSammensattNavn(), is(FORNAVN + " " + ETTERNAVN));
    }

    @Test
    public void shouldReturnNullWhenNavnInResponse() throws Exception {
        HentPersonResponse response = defaultResponse();
        response.setPerson(null);
        when(personV3.hentPerson(any(HentPersonRequest.class))).thenReturn(response);

        Bruker person = personV3Consumer.hentPerson(FNR, "");

        assertThat(person, nullValue());
    }

    @Test
    public void shouldReturnNullWhenNameNotInResponse() throws Exception {
        HentPersonResponse response = defaultResponse();
        response.getPerson().setPersonnavn(null);
        when(personV3.hentPerson(any(HentPersonRequest.class))).thenReturn(response);

        Bruker person = personV3Consumer.hentPerson(FNR, "");

        assertThat(person.getPersonnavn(), nullValue());
    }

    @Test
    public void shouldThrowFunctionalExceptionWhenPersonIkkeFunnet() throws Exception {
        when(personV3.hentPerson(any(HentPersonRequest.class))).thenThrow(new HentPersonPersonIkkeFunnet("Fant ikke person", new PersonIkkeFunnet()));

        RegOppslagFunctionalException e = assertThrows(RegOppslagFunctionalException.class,
                () -> personV3Consumer.hentPerson(FNR, ""),
                "Should throw exception");

        assertThat(e.getMessage(), is(equalTo("PersonV3.hentPerson fant ikke person med ident=" + FNR + ", message=Fant ikke person")));
        verify(personV3, times(1)).hentPerson(any(HentPersonRequest.class));

    }

    @Test
    public void shouldThrowFunctionalExceptionWhenSikkerhetsbegrensning() throws Exception {
        when(personV3.hentPerson(any(HentPersonRequest.class))).thenThrow(new HentPersonSikkerhetsbegrensning("Ingen adgang", new Sikkerhetsbegrensning()));

        RegOppslagSecurityException e = assertThrows(RegOppslagSecurityException.class,
                () -> personV3Consumer.hentPerson(FNR, ""),
                "Should throw exception");

        assertThat(e.getMessage(), is(equalTo("PersonV3.hentPerson feiler på grunn av sikkerhetsbegresning. Message=Ingen adgang")));
        verify(personV3, times(1)).hentPerson(any(HentPersonRequest.class));
    }

    @Test
    public void shouldRetryWhenTechnicalExceptionThrown() throws Exception {
        when(personV3.hentPerson(any(HentPersonRequest.class))).thenThrow(new RuntimeException());
        RegOppslagTechnicalException e = assertThrows(RegOppslagTechnicalException.class,
                () -> personV3Consumer.hentPerson(FNR, ""),
                "Should throw exception");
        verify(personV3, times(5)).hentPerson(any(HentPersonRequest.class));

    }


    private HentPersonResponse defaultResponse() {
        return createResponse(FORNAVN, MELLOMNAVN, ETTERNAVN);
    }

    private HentPersonResponse createResponse(String fornavn, String mellomnavn, String etternavn) {
        HentPersonResponse response = new HentPersonResponse();
        Personnavn personnavn = new Personnavn();
        personnavn.setFornavn(fornavn);
        if (mellomnavn != null) {
            personnavn.setMellomnavn(mellomnavn);
            personnavn.setSammensattNavn(fornavn + " " + mellomnavn + " " + etternavn);
        } else {
            personnavn.setSammensattNavn(fornavn + " " + etternavn);
        }
        personnavn.setEtternavn(etternavn);
        Bruker person = new Bruker();
        person.setPersonnavn(personnavn);
        response.setPerson(person);
        return response;
    }

    @EnableRetry
    @Configuration
    static class Config {

        @Bean
        public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
            return new PropertySourcesPlaceholderConfigurer();
        }

        @Bean
        public PersonV3 personV3() {
            return mock(PersonV3.class);
        }

        @Bean
        public MicrometerMetrics metrics() {
            return mock(MicrometerMetrics.class);
        }

        @Bean
        public MeterRegistry registry() {
            return mock(MeterRegistry.class);
        }
    }
}
