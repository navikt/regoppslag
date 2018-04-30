package no.nav.regoppslag.nais.checkcore;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@Slf4j
@Getter
public abstract class AbstractDependencyCheck {
	protected final DependencyType type;
	protected final List<Importance> importance;
	protected final String name;
	protected String address;

	public AbstractDependencyCheck(DependencyType type, Importance importance, String name) {
		this.type = type;
		this.name = name;
		this.importance = new ArrayList<>();
		this.importance.add(importance);
	}

	public AbstractDependencyCheck(DependencyType type, List<Importance> importance, String name) {
		this.type = type;
		this.importance = importance;
		this.name = name;
	}


	public AbstractDependencyCheck(DependencyType type, Importance importance, String name, String address) {
		this.type = type;
		this.name = name;
		this.importance = new ArrayList<>();
		this.importance.add(importance);
		this.address = address;
	}

	public AbstractDependencyCheck(DependencyType type, List<Importance> importance, String name, String address) {
		this.type = type;
		this.name = name;
		this.importance = importance;
		this.address = address;
	}

	protected abstract void doCheck();

	public Callable<DependencyCheckResult> check(Authentication authentication) {
		return () -> {
			SecurityContextHolder.getContext().setAuthentication(authentication);
			DependencyCheckResult.DependencyCheckResultBuilder builder = DependencyCheckResult.builder()
					.type(getType())
					.endpoint(getName())
					.importance(getImportance())
					.address(getAddress());
			doCheck();
			return builder.result(Result.OK).build();
		};
	}
	

}
