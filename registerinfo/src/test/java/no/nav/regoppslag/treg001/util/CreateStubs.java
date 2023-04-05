package no.nav.regoppslag.treg001.util;

import no.nav.dokmet.api.tkat020.SpraakInfoTo;

import java.util.ArrayList;
import java.util.List;

public class CreateStubs {
	public static List<SpraakInfoTo> createTkatResponse(List<String> langs) {
		List<SpraakInfoTo> list = new ArrayList<>();
		langs.forEach(lang -> {
			SpraakInfoTo SpraakInfoTo = new SpraakInfoTo();
			SpraakInfoTo.setSpraaklag(lang);
			list.add(SpraakInfoTo);
		});
		return list;
	}

}
