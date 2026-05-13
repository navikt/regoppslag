package no.nav.regoppslag.treg001;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respons med brevdata beriket med registerdata")
public class KompletterBrevdataResponse {

	@Schema(description = "Brevdata påført data fra berikerplugins", example = "<ns:brevdata>...</ns:brevdata>")
	private String brevdata;
}
