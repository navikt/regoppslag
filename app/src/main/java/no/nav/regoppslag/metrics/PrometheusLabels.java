package no.nav.regoppslag.metrics;

/**
 * @author Jakob A. Libak, NAV.
 */
public class PrometheusLabels {
    public static final String LABEL_TECHNICAL_EXCEPTION = "technical";
    public static final String LABEL_FUNCTIONAL_EXCEPTION = "functional";
    public static final String LABEL_TMOT501_FAILURE_UPDATE = "tmot501_failure_update";

	public static final String LABEL_EXCEPTION_CAUSE = "cause";
	public static final String LABEL_EXCEPTION_MESSAGE = "message";
    public static final String LABEL_PROCESS = "process";
    public static final String LABEL_PROCESS_CODE = "process_code";
    public static final String LABEL_PROCESS_CALLED = "process_called";
    public static final String LABEL_PROCESS_TITLE = "process_title";
    
    public static final String LABEL_HANDLER = "handler";
    public static final String LABEL_EVENT = "event";
    public static final String LABEL_ERROR_TYPE = "error_type";
    public static final String LABEL_SED_DOK = "sed_dok";
    public static final String LABEL_DOKUMENTTYPEID = "dokumenttypeId";
    public static final String LABEL_DOKUMENTTYPE = "dokumenttype";
    
    public static final String AVSENDER_ORGANISASJON = "avsender_type_organisasjon";
    public static final String AVSENDER_PERSON = "avsender_type_person";
    public static final String DOCUMENT_WITH_VEDLEGG = "document_with_vedlegg";
}
