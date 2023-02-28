package no.nav.regoppslag;

import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Import;

@Import(Application.class)
public class TestApplication {
	public static void main(String[] args) {
		SpringApplication.run(TestApplication.class, args);
	}
}
