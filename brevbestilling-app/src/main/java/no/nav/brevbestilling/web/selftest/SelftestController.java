package no.nav.brevbestilling.web.selftest;

import lombok.extern.slf4j.Slf4j;
import no.nav.brevbestilling.web.selftest.support.SelftestResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Controller
@Slf4j
public class SelftestController {
	// Application is up and running
	private static final String APPLICATION_LIVENESS = "Application is alive!";
	private static final String APPLICATION_READY = "Application is ready!";

	@Value("${applicationName}")
	private String applicationName;
	@Value("${applicationVersion}")
	private String applicationVersion;
	@Value("${bootstrapVersion}")
	private String bootstrapVersion;

	@Inject
	public SelftestController() {
	}

	/**
	 * Thymeleaf view
	 */
	@RequestMapping(value = "/internal/selftest", produces = MediaType.ALL_VALUE)
	public String selftest(@RequestParam(value = "status", required = false) String status,
						   HttpServletResponse httpServletResponse, Model model) throws IOException {
		SelftestResponse response = performSelftest();

		if (status != null && response.isError()) {
			httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		model.addAttribute("selftest", response);
		model.addAttribute("bootstrapVersion", bootstrapVersion);
		model.addAttribute("applicationVersion", applicationVersion);
		return "selftest";
	}

	/**
	 * Rest JSON view
	 */
	@ResponseBody
	@RequestMapping(value = "/internal/selftest", produces = MediaType.APPLICATION_JSON_VALUE)
	public SelftestResponse selftest() {
		return performSelftest();
	}

	/**
	 * Rest Nais Liveness contract
	 */
	@ResponseBody
	@RequestMapping(value = "/isAlive", produces = MediaType.TEXT_PLAIN_VALUE)
	public String isAlive() {
		return APPLICATION_LIVENESS;
	}

	/**
	 * Rest Nais Readyness contract
	 */
	@ResponseBody
	@RequestMapping(value = "/isReady", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity isReady() {
		SelftestResponse response = performSelftest();
		if (response.isError()) {
			return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(APPLICATION_READY, HttpStatus.OK);
	}

	private SelftestResponse performSelftest() {
		SelftestResponse response = new SelftestResponse();
		response.setApplication(applicationName);
		response.setVersion(applicationVersion);
		response.setNode(getServerAddress());
		return response;
	}


	private String getServerAddress() {
		try {
			return InetAddress.getLocalHost().getCanonicalHostName();
		} catch (UnknownHostException e) {
			log.debug("Unable to get serveradress", e);
			return "N/A";
		}
	}
}
