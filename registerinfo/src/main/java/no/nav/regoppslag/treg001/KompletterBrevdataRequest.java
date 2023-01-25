package no.nav.regoppslag.treg001;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KompletterBrevdataRequest {

	private static final String exampleBrevdata = "<ns:brevdata xmlns:ns=\"http://nav.no/dok/urbrev/000093\" xmlns:nav=\"http://nav.no/dok/brevdata/felles/v1/NAVFelles\"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"  xmlns:urfelles=\"http://nav.no/dok/urbrev/felles/ur_felles\"> <ns:NAVFelles> <urfelles:sakspart> <nav:id>889640782</nav:id> <nav:typeKode>ORGANISASJON</nav:typeKode> <nav:navn>string</nav:navn> </urfelles:sakspart> <urfelles:mottaker xsi:type=\"nav:Organisasjon\"> <nav:id>889640782</nav:id> <nav:typeKode>ORGANISASJON</nav:typeKode> <nav:navn>string</nav:navn> <nav:kortNavn>string</nav:kortNavn> <nav:spraakkode>NN</nav:spraakkode> <nav:mottakeradresse xsi:type=\"nav:NorskPostadresse\"> <nav:adresselinje1>jsfasjfø</nav:adresselinje1> <nav:postnummer>0123</nav:postnummer> <nav:poststed>Oslo</nav:poststed> <nav:land>no</nav:land> </nav:mottakeradresse> </urfelles:mottaker> <urfelles:behandlendeEnhet> <nav:enhetsId>0136</nav:enhetsId> <nav:enhetsNavn>string</nav:enhetsNavn> </urfelles:behandlendeEnhet> </ns:NAVFelles> <ns:fag> <ns:utbetalingsMelding>  <ns:fDato>2018-06-08</ns:fDato> <ns:belopUtbetalt>1000</ns:belopUtbetalt> <ns:datoUtbetalt>2018-06-08</ns:datoUtbetalt> <ns:tekstFelt>TestTekstFelt</ns:tekstFelt> <ns:fastTekst>TestFastTekst</ns:fastTekst> <ns:oppgaveType>PENSJONSUTBETALINGSMELDING</ns:oppgaveType> <ns:kontonr>1231231231231</ns:kontonr> <ns:utbetalingsMnd>Januar</ns:utbetalingsMnd> <ns:navRefId>123</ns:navRefId> <ns:kontering> <ns:kontoTekst>TestKontoTekst</ns:kontoTekst> <ns:belop>100</ns:belop> <ns:periode> <ns:periodeFom>2018-06-01</ns:periodeFom> <ns:periodeTom>2018-06-08</ns:periodeTom> </ns:periode> </ns:kontering> </ns:utbetalingsMelding> </ns:fag> </ns:brevdata>";

	@Schema(example = "000093", description = "Benyttes for å hente tilknyttede Språkinfo fra Dokumentkatalogen.", required = true)
	private String dokumentTypeId;
	@Schema(example = exampleBrevdata, description = "Brevdata fra felles mastermal. XML'en kan være utfylt med data eller ikke.", required = true)
	private String brevdata;
	@Schema(example = "FOR", description = "Temaet som forsendelsen tilhører, for eksempel \"FOR\" (foreldrepenger).", allowableValues = "DAG, FOR, PEN, FRI ....")
	private String tema;
}
