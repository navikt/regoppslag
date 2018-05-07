package no.nav.regoppslag.common;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * TO object used in POST
 *
 * @author Ketill Fenne, Visma Consulting
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KompletterBrevdataRequest {
	
	private static final String exampleBrevdata = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><brevdata><felles xmlns:felles=\"http://nav.no/dok/pesysbrev/felles/v1/PesysFelles\" xmlns:kontaktinformasjon=\"http://nav.no/dok/pesysbrev/felles/v1/Kontaktinformasjon\" xmlns:aktoer=\"http://nav.no/dok/pesysbrev/felles/v1/Aktoer\" xmlns:mottaker=\"http://nav.no/dok/pesysbrev/felles/v1/Mottaker\" xmlns:navEnhet=\"http://nav.no/dok/pesysbrev/felles/v1/NavEnhet\" xmlns:saksbehandler=\"http://nav.no/dok/pesysbrev/felles/v1/Saksbehandler\"><felles:mottaker><aktoer:id>889640782</aktoer:id><aktoer:typeKode>ORGANISASJON</aktoer:typeKode></felles:mottaker><felles:signerendeSaksbehandler><saksbehandler:navAnsatt><ansattId>Z999990</ansattId></saksbehandler:navAnsatt><saksbehandler:navEnhet><enhetsId>0136</enhetsId></saksbehandler:navEnhet></felles:signerendeSaksbehandler><felles:signerendeBeslutter><saksbehandler:navAnsatt><ansattId>Z999990</ansattId></saksbehandler:navAnsatt><saksbehandler:navEnhet><enhetsId>0136</enhetsId></saksbehandler:navEnhet></felles:signerendeBeslutter><felles:kontaktinformasjon><kontaktinformasjon:postadresse><navEnhet:enhetsId>0136</navEnhet:enhetsId></kontaktinformasjon:postadresse></felles:kontaktinformasjon></felles></brevdata>";
	
	@ApiModelProperty(example = "I000003", notes = "Benyttes for å hente tilknyttede Språkinfo fra Dokumentkatalogen.", required = true)
	private String dokumentTypeId;
	@ApiModelProperty(example = exampleBrevdata, notes = "Brevdata fra felles mastermal. XML'en kan være utfylt med data eller ikke.", required = true)
	private String brevdata;
}
