package no.nav.regoppslag.consumer.pdl.to;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Data
public class PDLHentPersonResponse {

	private List<PDLError> errors;
	private PDLHentPerson data;

	@Setter
	@Getter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class PDLHentPerson {
		private HentPerson hentPerson;
	}
}
