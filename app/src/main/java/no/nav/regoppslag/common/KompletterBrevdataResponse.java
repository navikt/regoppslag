package no.nav.regoppslag.common;

import io.swagger.annotations.ApiModelProperty;
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
public class KompletterBrevdataResponse {
	
	@ApiModelProperty(notes = "Brevdata påført data fra berikerplugins")
	private String brevdata;
}
