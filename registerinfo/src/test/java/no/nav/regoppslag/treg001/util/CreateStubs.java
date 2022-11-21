package no.nav.regoppslag.treg001.util;

import no.nav.dokkat.api.tkat020.v4.SpraakInfoToV4;

import java.util.ArrayList;
import java.util.List;

public class CreateStubs {
	public static List<SpraakInfoToV4> createTkatResponse(List<String> langs) {
		List<SpraakInfoToV4> list = new ArrayList<>();
		langs.forEach(lang -> {
			SpraakInfoToV4 spraakInfoToV4 = new SpraakInfoToV4();
			spraakInfoToV4.setSpraaklag(lang);
			list.add(spraakInfoToV4);
		});
		return list;
	}

}
