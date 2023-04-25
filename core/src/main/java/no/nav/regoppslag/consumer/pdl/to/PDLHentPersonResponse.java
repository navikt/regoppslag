package no.nav.regoppslag.consumer.pdl.to;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Data
@Builder
public class PDLHentPersonResponse {

	private List<PDLError> errors;
	private PDLHentPerson data;

	@Setter
	@Getter
	@ToString
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class PDLHentPerson {
		private HentPerson hentPerson;
	}
}
