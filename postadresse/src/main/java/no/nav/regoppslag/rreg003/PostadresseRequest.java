package no.nav.regoppslag.rreg003;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Getter
@Builder
@AllArgsConstructor
@Setter
@NoArgsConstructor
public class PostadresseRequest {

	@Schema(example = "889640782", description = "Fnr eller org nr som brukes som oppslagsnøkkel mot PDL eller Ereg.", requiredMode = REQUIRED)
	private String ident;

	@Schema(allowableValues = {"fortrolig", "strengt_fortrolig", "strengt_fortrolig_utland"}, description = "beskyttelsesgrad som angir om en persons adresser skal skjermes for innsyn")
	private Set<String> filtrerAdressebeskyttelse = new HashSet<>();
}
