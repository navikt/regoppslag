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
	
	private static final String exampleBrevdata = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><brevdata><NAVFelles xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"  xmlns:f=\"http://nav.no/dok/brevdata/felles/v1/NAVFelles\" xmlns:test=\"test\"><test:mottaker xsi:type=\"f:Person\" f:berik=\"true\"><f:id>20096828390</f:id><f:typeKode>PERSON</f:typeKode></test:mottaker><test:signerendeSaksbehandler><f:navAnsatt f:berik=\"true\"><ansattId>Z991006</ansattId></f:navAnsatt><f:navEnhet f:berik=\"true\"><enhetsId>0136</enhetsId></f:navEnhet></test:signerendeSaksbehandler><test:signerendeBeslutter><f:navAnsatt f:berik=\"true\"><ansattId>Z991006</ansattId></f:navAnsatt><f:navEnhet f:berik=\"true\"><enhetsId>0136</enhetsId></f:navEnhet></test:signerendeBeslutter><test:behandlendeEnhet><f:navEnhet f:berik=\"true\"><enhetsId>0136</enhetsId></f:navEnhet></test:behandlendeEnhet><test:kontaktinformasjon><f:postadresse f:berik=\"true\"><f:enhetsId>0136</f:enhetsId></f:postadresse><f:besoksadresse f:berik=\"true\"><f:enhetsId>0136</f:enhetsId></f:besoksadresse></test:kontaktinformasjon></NAVFelles></brevdata>";
	
	@ApiModelProperty(example = "I000003", notes = "Benyttes for å hente tilknyttede Språkinfo fra Dokumentkatalogen.", required = true)
	private String dokumentTypeId;
	@ApiModelProperty(example = exampleBrevdata, notes = "Brevdata fra felles mastermal. XML'en kan være utfylt med data eller ikke.", required = true)
	private String brevdata;
}
