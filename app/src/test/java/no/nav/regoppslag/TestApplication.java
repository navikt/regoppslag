package no.nav.regoppslag;

import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Import;

@Import({
		Application.class,
})
public class TestApplication {
	public static void main(String[] args) {
		System.out.println("Vil du ha mer logging fra appen her? Fiks det i logback-testapp.xml da vel! :)");
		SpringApplication.run(TestApplication.class, args);
	}
}
