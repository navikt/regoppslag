package no.nav.regoppslag.consumer.pdl.to;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Value;

import java.util.List;

@Value
public class PDLHentPersonResponse {

	List<PDLError> errors;
	PDLHentPerson data;

	@Value
	@AllArgsConstructor
	public static class PDLHentPerson {
		HentPerson hentPerson;
	}
}
