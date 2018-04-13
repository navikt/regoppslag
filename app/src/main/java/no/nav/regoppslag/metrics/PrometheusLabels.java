package no.nav.regoppslag.metrics;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class PrometheusLabels {
    public static final String LABEL_TECHNICAL_EXCEPTION = "technical";
    public static final String LABEL_FUNCTIONAL_EXCEPTION = "functional";
	
	public static final String LABEL_EXCEPTION_NAME = "exception_name";
    public static final String LABEL_PROCESS = "process";
	public static final String LABEL_PROCESS_CODE = "process_called";
    public static final String LABEL_TYPE = "type";
	public static final String LABEL_PROCESS_CALLED = "process_title";
	public static final String LABEL_CONSUMER_NAME = "consumer_name";
    
    public static final String LABEL_EVENT = "event";
    public static final String LABEL_ERROR_TYPE = "error_type";
    
    public static final String SERVICE_CODE_TREG001="TREG001";
    public static final String SERVICE_CODE_TREG002="TREG002";
    
    //Cache
	public static final String LABEL_CACHE_NAME = "name";
	public static final String LABEL_CACHE_OPERATION = "operation";
	public static final String CACHE_MISS = "cacheMiss";
	public static final String CACHE_HIT = "cacheHit";
	
	public static final String MOTTAKERTYPE = "mottakerType";
	public static final String PLUGIN = "plugin";
	public static final String CONTROLLER = "controller";
	public static final String PERSONV3 = "personV3";
	public static final String ORGANISASJONV4 = "OrganisasjonV4";
	public static final String ORGENHETKONTAKTV1 = "OrganisasjonEnhetKontaktinformasjonV1";
	public static final String LDAP = "ldap";
	
}
