package no.nav.brevbestilling.web.selftest.support;

/**
 * Created by T133804 on 15.08.2017.
 */
public class Ping {
	private String name;
	private String beskrivelse;
	private String address;
	private Type type;
	private Runnable pinger;
	
	public enum Type {
		Queue("Jms Queue"),
		RemoteQueue("Remote Queuemanager Queue"),
		Soap("Soap WebService"),
		Rest("Rest"),
		Datasource("Oracle datasource"),
		Other("");
		
		private String beskrivelse;
		
		Type(String beskrivelse) {
			this.beskrivelse = beskrivelse;
		}
		
		public String getBeskrivelse() {
			return beskrivelse;
		}
		
	}
	
	public Ping(Type type, String name, String beskrivelse, String address, Runnable pinger) {
		this.type = type;
		this.name = name;
		this.beskrivelse = beskrivelse;
		this.address = address;
		this.pinger = pinger;
	}
	
	public Ping(Type type, String name, String address, Runnable pinger) {
		this.type = type;
		this.name = name;
		this.beskrivelse = type.getBeskrivelse();
		this.address = address;
		this.pinger = pinger;
	}
	
	public Type getType() {
		return type;
	}
	
	public String getName() {
		return name;
	}
	
	public String getBeskrivelse() {
		return beskrivelse;
	}
	
	public String getAddress() {
		return address;
	}
	
	public Runnable getPinger() {
		return pinger;
	}
}

