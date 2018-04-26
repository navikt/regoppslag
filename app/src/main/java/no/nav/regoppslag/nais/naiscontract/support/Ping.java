package no.nav.regoppslag.nais.naiscontract.support;

/**
 * Created by T133804 on 15.08.2017.
 */
public class Ping {
	
	public enum Type {
		Soap("Soap WebService"),
		Redis("Redis cache");
		
		private String beskrivelse;
		
		Type(String beskrivelse) {
			this.beskrivelse = beskrivelse;
		}
		
	}
	
	
}

